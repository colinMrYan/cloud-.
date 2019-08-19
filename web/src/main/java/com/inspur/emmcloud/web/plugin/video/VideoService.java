package com.inspur.emmcloud.web.plugin.video;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.ui.ImpFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 短视频录制
 */
public class VideoService extends ImpPlugin {

    private String successCb, failCb;
    private String uploadUrl;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");

        if (action.equals("recordVideo")) {
            startRecordVideo(paramsObject);
        } else if (action.equals("playVideo")) {
            JSONObject optionObj = paramsObject.optJSONObject("options");
            String path = optionObj.optString("path");
            Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
            intent.putExtra("path", path);
            getActivity().startActivity(intent);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    private void startRecordVideo(final JSONObject paramsObject) {
        try {
            final JSONObject optionsObj = paramsObject.getJSONObject("options");
            uploadUrl = optionsObj.optString("url");

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getActivity(), Permissions.CAMERA,
                        new PermissionRequestCallback() {
                            @Override
                            public void onPermissionRequestSuccess(List<String> permissions) {
                                Uri fileUri = null;
                                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                String fileName = optionsObj.optString("id");
                                try {
                                    fileUri = FileProvider.getUriForFile(getActivity(),
                                            getActivity().getPackageName() + ".provider", createMediaFile(fileName));//这是正确的写法

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 600);

                                if (getImpCallBackInterface() != null) {
                                    getImpCallBackInterface().onStartActivityForResult(intent, ImpFragment.REQUEST_CODE_RECORD_VIDEO);
                                }
//                            getActivity().startActivityForResult(intent, ImpFragment.REQUEST_CODE_RECORD_VIDEO);
                            }

                            @Override
                            public void onPermissionRequestFail(List<String> permissions) {

                            }
                        });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private File createMediaFile(String fileName) throws IOException {
        if (AppUtils.isHasSDCard(getActivity())) {
            if ((Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))) {
                // 选择自己的文件夹
                String path = MyAppConfig.LOCAL_DOWNLOAD_PATH + "/video/";
                // Constants.video_url 是一个常量，代表存放视频的文件夹
                File mediaStorageDir = new File(path);
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.e("TAG", "文件夹创建失败");
                        return null;
                    }
                }

                // 如果id为空  文件根据当前的毫秒数给自己命名
                String timeStamp = String.valueOf(System.currentTimeMillis());
                timeStamp = timeStamp.substring(7);
                if (StringUtils.isBlank(fileName)) {
                    fileName = "V" + timeStamp;
                }
                String suffix = ".mp4";
                File mediaFile = new File(mediaStorageDir + File.separator + fileName + suffix);
                return mediaFile;
            }
        }
        return null;
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return null;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImpFragment.REQUEST_CODE_RECORD_VIDEO) {
            Toast.makeText(getActivity(), "Video saved to:\n" +
                    data.getData(), Toast.LENGTH_LONG).show();
            Log.d("zhang", "onActivityResult: " + data.getData());
            //上传文件
            File file = FileUtils.uri2File(getFragmentContext(), data.getData());
            uploadFile(file.getPath());
        }
    }

    private void uploadFile(final String filePath) {
        final String completeUrl = uploadUrl;
        RequestParams params = BaseApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        File file = new File(filePath);
        params.setMultipart(true);// 有上传文件时使用multipart表单, 否则上传原始文件流.
        params.addBodyParameter("video", file);
        final LoadingDialog loadingDlg = new LoadingDialog(getFragmentContext());
        loadingDlg.setText("上传中。。。");
        loadingDlg.show();
        HttpUtils.request(getFragmentContext(), CloudHttpMethod.POST, params, new BaseModuleAPICallback(getFragmentContext(), completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {

                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                        LoadingDialog.dimissDlg(loadingDlg);
                        jsCallback(failCb);
                    }
                };
//                refreshToken(
//                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                ToastUtils.show("上传成功");
                LoadingDialog.dimissDlg(loadingDlg);
                JSONObject json = new JSONObject();
                try {
                    json.put("path", filePath);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsCallback(successCb, json.toString());
//                apiInterface
//                        .returnUploadMyHeadSuccess(new GetUploadMyHeadResult(new String(arg0)), filePath);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                ToastUtils.show("上传失败");
                LoadingDialog.dimissDlg(loadingDlg);
                jsCallback(failCb);
//                apiInterface.returnUploadMyHeadFail(error, responseCode);
            }
        });
    }
}
