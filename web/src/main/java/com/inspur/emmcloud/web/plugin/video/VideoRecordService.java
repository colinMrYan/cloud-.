package com.inspur.emmcloud.web.plugin.video;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.ui.ImpFragment;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 短视频录制
 */
public class VideoRecordService extends ImpPlugin {

    @Override
    public void execute(String action, JSONObject paramsObject) {
        startRecordVideo(paramsObject);
    }

    private void startRecordVideo(final JSONObject paramsObject) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getActivity(), Permissions.CAMERA,
                    new PermissionRequestCallback() {
                        @Override
                        public void onPermissionRequestSuccess(List<String> permissions) {
                            Uri fileUri = null;
                            String fileName = paramsObject.optString("id");
                            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                            try {
                                fileUri = FileProvider.getUriForFile(getActivity(),
                                        getActivity().getPackageName() + ".provider", createMediaFile(fileName));//这是正确的写法

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

                            getActivity().startActivityForResult(intent, ImpFragment.REQUEST_CODE_RECORD_VIDEO);
                        }

                        @Override
                        public void onPermissionRequestFail(List<String> permissions) {

                        }
                    });
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
        return null;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImpFragment.REQUEST_CODE_RECORD_VIDEO && resultCode == 0) {
            Toast.makeText(getActivity(), "Video saved to:\n" +
                    data.getData(), Toast.LENGTH_LONG).show();
        }
    }
}
