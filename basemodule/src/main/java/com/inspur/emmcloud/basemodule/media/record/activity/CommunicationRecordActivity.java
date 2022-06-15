package com.inspur.emmcloud.basemodule.media.record.activity;

import android.os.Environment;

import com.gyf.barlibrary.BarHide;
import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.ui.BaseFragmentActivity;
import com.inspur.emmcloud.basemodule.ui.NotSupportLand;
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
    private boolean granted; // 是否获取权限

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

    }

    // 设置全屏
    private void setWindows() {
        ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).fullScreen(true).transparentNavigationBar().init();
    }
}
