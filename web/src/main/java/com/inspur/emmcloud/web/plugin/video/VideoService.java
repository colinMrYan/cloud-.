package com.inspur.emmcloud.web.plugin.video;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.MyDialog;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.UpLoaderUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.ui.ImpFragment;
import com.inspur.emmcloud.web.util.WebFormatUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 短视频录制
 */
public class VideoService extends ImpPlugin {

    private String successCb, failCb;
    private String uploadUrl;
    private static final int RECORD_VIDEO_DURATION_LIMIT = 600; //录制时间限制  10分钟
    //dialog 相关
    private static final int UPLOAD_SUCCESS = 1;
    private static final int UPLOAD_FAIL = 2;
    private static final int UPLOAD_LOADING = 3;
    ProgressBar downloadProgressBar;
    private String filePath;
    private Callback.Cancelable cancelable;
    private MyDialog progressDownloadDialog;
    private TextView ratioText;
    private TextView percentText;
    private String downloadPercent;
    private int progress;
    private long totalSize;
    private long downloadSize;
    @SuppressLint("HandlerLeak")
    private final Handler upgradeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPLOAD_LOADING:
                    downloadPercent = progress + "%";
                    String text = WebFormatUtil.setFormat(downloadSize) + "/"
                            + WebFormatUtil.setFormat(totalSize);
                    if (downloadProgressBar != null) {
                        downloadProgressBar.setProgress(progress);
                    }
                    if (ratioText != null) {
                        ratioText.setText(text);
                    }
                    if (percentText != null) {
                        percentText.setText(downloadPercent);
                    }
                    break;
                case UPLOAD_SUCCESS:
                    progressDownloadDialog.dismiss();
                    if (cancelable != null) {
                        cancelable.cancel();
                    }
                    JSONObject json = new JSONObject();
                    try {
                        json.put("path", filePath);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jsCallback(successCb, json.toString());
                    break;
                case UPLOAD_FAIL:
                    progressDownloadDialog.dismiss();
                    if (cancelable != null) {
                        cancelable.cancel();
                    }
                    jsCallback(failCb, "视频上传失败");
                    break;
            }
        }
    };

    @Override
    public void execute(String action, JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");

        if (action.equals("open")) {
            startRecordVideo(paramsObject);
        }

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

    private void startRecordVideo(final JSONObject paramsObject) {
        try {
            final JSONObject optionsObj = paramsObject.optJSONObject("options");
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
                                intent.putExtra(MediaStore.MediaColumns.WIDTH, 720);
                                intent.putExtra(MediaStore.MediaColumns.HEIGHT, 1280);
                                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, RECORD_VIDEO_DURATION_LIMIT); //最长时间10分钟

                                if (getImpCallBackInterface() != null) {
                                    getImpCallBackInterface().onStartActivityForResult(intent, ImpFragment.REQUEST_CODE_RECORD_VIDEO);
                                }
                            }

                            @Override
                            public void onPermissionRequestFail(List<String> permissions) {

                            }
                        });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImpFragment.REQUEST_CODE_RECORD_VIDEO) {
            //上传文件
            File file = FileUtils.uri2File(getFragmentContext(), data.getData());
            filePath = file.getPath();
            uploadFile();
        }
    }

    private void uploadFile() {
        if (StringUtils.isBlank(filePath)) return;
        final String completeUrl = uploadUrl;
        File file = new File(filePath);

        cancelable = UpLoaderUtils.uploadFile(completeUrl, "video", file, new Callback.ProgressCallback() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {    //下载中
                progress = (int) (((float) current / total) * 100);
                totalSize = total;
                downloadSize = current;
                upgradeHandler.sendEmptyMessage(UPLOAD_LOADING);
            }

            @Override
            public void onSuccess(Object o) {
                ToastUtils.show("上传成功");
                upgradeHandler.sendEmptyMessage(UPLOAD_SUCCESS);
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                ToastUtils.show("上传失败");
                jsCallback(failCb);
                upgradeHandler.sendEmptyMessage(UPLOAD_FAIL);
            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {

            }
        });

        showUploadDialog();
    }

    private void showUploadDialog() {
        progressDownloadDialog = new MyDialog(getFragmentContext(), R.layout.app_dialog_down_progress_one_button);
        progressDownloadDialog.setDimAmount(0.2f);
        progressDownloadDialog.setCancelable(false);
        progressDownloadDialog.setCanceledOnTouchOutside(false);
        ((TextView) progressDownloadDialog.findViewById(R.id.tv_permission_dialog_title)).
                setText(getFragmentContext().getString(R.string.app_name));
        ((TextView) progressDownloadDialog.findViewById(R.id.tv_downloading)).
                setText("上传中，请稍后。。。");
        downloadProgressBar = progressDownloadDialog.findViewById(R.id.progress_bar_apk_download);
        ratioText = progressDownloadDialog.findViewById(R.id.tv_num_progress);
        percentText = progressDownloadDialog.findViewById(R.id.tv_num_percent);
        progressDownloadDialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDownloadDialog.dismiss();
                if (cancelable != null) {
                    cancelable.cancel();
                }
            }
        });
        progressDownloadDialog.show();
    }

}
