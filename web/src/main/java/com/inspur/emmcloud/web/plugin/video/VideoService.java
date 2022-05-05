package com.inspur.emmcloud.web.plugin.video;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.plugin.filetransfer.FilePathUtils;
import com.inspur.emmcloud.web.ui.ImpFragment;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * 短视频录制
 */
public class VideoService extends ImpPlugin {

    private String successCb, failCb;
    private String recordVideoFilePath;
    private boolean returnBase64;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");

        if (action.equals("recordVideo")) {
            startRecordVideo(paramsObject);
        } else if (action.equals("playVideo")) {
            JSONObject optionObj = paramsObject.optJSONObject("options");
            String path = optionObj.optString("path");
            if (StringUtils.isBlank(path)) {
                return;
            }
            //识别真实路径
            path = FilePathUtils.getRealPath(path);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String type = "video/*";
                Uri uri;
                if (path.startsWith("http")) {
                    String extension = MimeTypeMap.getFileExtensionFromUrl(path);
                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    uri = Uri.parse(path);
                } else {
                    File file = new File(path);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        uri = FileProvider.getUriForFile(getFragmentContext(), getFragmentContext().getPackageName() + ".provider", file);
                    } else {
                        uri = Uri.fromFile(file);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                }

                intent.setDataAndType(uri, type);
                getFragmentContext().startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    private void startRecordVideo(final JSONObject paramsObject) {
        try {
            final JSONObject optionsObj = paramsObject.getJSONObject("options");

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getActivity(), Permissions.CAMERA,
                        new PermissionRequestCallback() {
                            @Override
                            public void onPermissionRequestSuccess(List<String> permissions) {
                                Uri fileUri = null;
                                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                String fileName = optionsObj.optString("id");
                                returnBase64 = optionsObj.optBoolean("isReturnBase64");
                                try {
                                    File file = createMediaFile(fileName);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        fileUri = FileProvider.getUriForFile(getFragmentContext(),
                                                getFragmentContext().getPackageName() + ".provider", file);
                                    } else {
                                        String path = FilePathUtils.BASE_PATH + "/video/";
                                        // Constants.video_url 是一个常量，代表存放视频的文件夹
                                        File mediaStorageDir = new File(path);
                                        fileUri = Uri.fromFile(mediaStorageDir);
                                    }

                                } catch (IOException e) {
                                    try {
                                        JSONObject json = new JSONObject();
                                        json.put("errorMessage", "文件操作异常");
                                        jsCallback(failCb, json);
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                    e.printStackTrace();
                                }
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, TextUtils.isEmpty(optionsObj.optString("time"))? 600 : optionsObj.optString("time"));

                                if (getImpCallBackInterface() != null) {
                                    getImpCallBackInterface().onStartActivityForResult(intent, ImpFragment.REQUEST_CODE_RECORD_VIDEO);
                                }
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
                String path = FilePathUtils.BASE_PATH + "/video/";
                // Constants.video_url 是一个常量，代表存放视频的文件夹
                File mediaStorageDir = new File(path);
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.e("TAG", "文件夹创建失败");
                        try {
                            JSONObject json = new JSONObject();
                            json.put("errorMessage", "文件夹创建失败");
                            jsCallback(failCb, json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                recordVideoFilePath = mediaStorageDir + File.separator + fileName + suffix;
                return mediaFile;
            }
        }
        try {
            JSONObject json = new JSONObject();
            json.put("errorMessage", "文件操作失败");
            jsCallback(failCb, json);
        } catch (JSONException e) {
            e.printStackTrace();
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

    private void uploadShortVideo(String localSource) {
        JSONObject json = new JSONObject();
        try {
            json.put("status", 1);
            JSONObject result = new JSONObject();
            result.put("base64", decodeVideoToBase64(localSource));
            result.put("value", localSource);
            json.put("result", result);
            jsCallback(successCb, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsCallback(successCb, json);
    }

    private String decodeVideoToBase64(String localPath){
        String base64 = "";
            try {
                File file = new File(localPath);
                FileInputStream inputFile = new FileInputStream(file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count = 0;
                while((count = inputFile.read(buffer)) >= 0){
                    baos.write(buffer, 0, count);//读取输入流并写入输出字节流中
                }
                inputFile.close();//关闭文件输入流
                base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            } catch (Exception e) {
                return null;
            }
        return base64;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImpFragment.REQUEST_CODE_RECORD_VIDEO) {
            if (data != null && data.getData() != null) {
                try {
                    if (returnBase64) {
                        uploadShortVideo(FilePathUtils.SDCARD_PREFIX + recordVideoFilePath);
                    } else {
                        JSONObject json = new JSONObject();
                        json.put("path", FilePathUtils.SDCARD_PREFIX + recordVideoFilePath);
                        jsCallback(successCb, json);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    JSONObject json = new JSONObject();
                    json.put("errorMessage", getFragmentContext().getString(R.string.web_video_record_fail));
                    jsCallback(failCb, json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
