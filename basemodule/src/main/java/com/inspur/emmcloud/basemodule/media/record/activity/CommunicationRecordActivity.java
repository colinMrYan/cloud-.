package com.inspur.emmcloud.basemodule.media.record.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Environment;
import android.view.View;

import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.record.VideoRecordConfig;
import com.inspur.emmcloud.basemodule.media.record.basic.VideoKitResult;
import com.inspur.emmcloud.basemodule.media.record.interfaces.IVideoRecordKit;
import com.inspur.emmcloud.basemodule.media.record.view.VideoRecordView;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.ui.NotSupportLand;
import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;

import java.util.List;

/**
 * Date：2022/6/13
 * Author：wang zhen
 * Description
 */
public class CommunicationRecordActivity extends BaseFragmentActivity implements NotSupportLand {
    public static final int REQUEST_CODE_IMAGE_EDIT = 1001;
    public static final int REQUEST_CODE_VIDEO_EDIT = 1002;
    public static final String FILE_PATH = "FILE_PATH";
    public static final String FILE_TYPE = "FILE_TYPE"; // 1标识图片，2表示视频
    private boolean granted; // 是否获取权限
    private VideoRecordView mVideoRecordLayout;
    private String photoFilePath; // 图片路径

    @Override
    public void onCreate() {
        hasPermission();
    }

    // 检查权限
    private void hasPermission() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            ToastUtils.show(this, R.string.baselib_sd_not_exist);
            finish();
        }
        String[] permissions = new String[]{Permissions.CAMERA, Permissions.RECORD_AUDIO, Permissions.READ_EXTERNAL_STORAGE,
                Permissions.WRITE_EXTERNAL_STORAGE};
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, permissions, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                granted = true;
                setContentView(R.layout.activity_communication_record);
                setWindows();
                initView();
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(CommunicationRecordActivity.this, PermissionRequestManagerUtils.getInstance().getPermissionToast(CommunicationRecordActivity.this, permissions));
                finish();
            }
        });
    }

    // 初始化view
    private void initView() {
        mVideoRecordLayout = findViewById(R.id.video_record_layout);
        VideoRecordConfig recordConfig = VideoRecordConfig.getInstance();
        mVideoRecordLayout.setConfig(recordConfig);

//        mVideoRecordLayout.getTitleBar().setOnBackClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        // 录制监听
        mVideoRecordLayout.setOnRecordListener(new IVideoRecordKit.OnRecordListener() {
            @Override
            public void onRecordCanceled() {
                finish();
            }

            @Override
            public void onRecordCompleted(VideoKitResult result) {
                if (result.errorCode == 0) {
//                    startEditActivity(result);
                } else {
                    ToastUtils.show("record video failed. error code:" + result.errorCode + ",desc msg:" + result.descMsg);
                }
            }
        });
    }

    // 设置全屏
    private void setWindows() {
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).fullScreen(true).transparentNavigationBar().init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mVideoRecordLayout.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVideoRecordLayout.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoRecordLayout.release();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVideoRecordLayout.screenOrientationChange();
    }

    @Override
    public void onBackPressed() {
        mVideoRecordLayout.backPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE_EDIT) {
            // 拍照
            if (resultCode == RESULT_OK) {
                photoFilePath = data.getStringExtra(IMGEditActivity.OUT_FILE_PATH);
                Intent intent = new Intent();
                intent.putExtra(FILE_PATH, photoFilePath);
                intent.putExtra(FILE_TYPE, 1);
                setResult(RESULT_OK, intent);
                finish();
            }
        }else if (requestCode == REQUEST_CODE_VIDEO_EDIT){
            // 视频
            if (resultCode == RESULT_OK) {

            }
        }
    }
}
