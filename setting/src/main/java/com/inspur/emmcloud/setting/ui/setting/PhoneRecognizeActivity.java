package com.inspur.emmcloud.setting.ui.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhoneRecognizeActivity extends BaseActivity {
    /**
     * 请求悬浮窗权限
     */
    private static final int REQUEST_WINDOW_PERMISSION = 100;
    private static final int REQUEST_BACKGROUND_LOCKSCREEN_PERMISSION = 101;
    @BindView(R2.id.tv_header)
    TextView headerText;
    @BindView(R2.id.open_overlay_window)
    TextView overlayWindow;
    @BindView(R2.id.open_phone_read)
    TextView readCaller;
    @BindView(R2.id.introduction_view)
    TextView permissionIntroduction;


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        headerText.setText(R.string.setting_phone_recognize);
        permissionIntroduction.setText(R.string.permission_setting_phone_recognize);
        overlayWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndSetWindowPermission();
            }
        });
        readCaller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkReadPhonePermission();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_mine_phone_recognize_switch_activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WINDOW_PERMISSION) {
            if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this)) {
                showRequestBackGroundOrLockViewDialog();
            }
        }
        refreshView();
    }

    public void onClick(View v) {
        finish();
    }

    private void refreshView() {
        if (PermissionRequestManagerUtils.getInstance().isHasPermission(this, Permissions.READ_CALL_LOG)) {
            readCaller.setText(getString(R.string.permission_setting_close_phone_read));
            readCaller.setEnabled(false);
        } else {
            readCaller.setText(getString(R.string.permission_setting_open_phone_read));
            readCaller.setEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if ((Settings.canDrawOverlays(this) && AppUtils.canBackgroundStart(this) && AppUtils.canShowLockView(this))) {
                overlayWindow.setText(getString(R.string.permission_setting_close_phone_overlay));
                overlayWindow.setEnabled(false);
            } else {
                overlayWindow.setText(getString(R.string.permission_setting_open_phone_overlay));
                overlayWindow.setEnabled(true);
            }
        } else {
            overlayWindow.setText(getString(R.string.permission_setting_close_phone_overlay));
            overlayWindow.setEnabled(false);
        }
    }

    private void checkReadPhonePermission() {
        if (!PermissionRequestManagerUtils.getInstance().isHasPermission(this, Permissions.READ_CALL_LOG)) {
            PermissionRequestManagerUtils.getInstance().requestRuntimePermission(this, Permissions.READ_CALL_LOG, new PermissionRequestCallback() {
                @Override
                public void onPermissionRequestSuccess(List<String> permissions) {
                    refreshView();
                }

                @Override
                public void onPermissionRequestFail(List<String> permissions) {
                }

            });
        }
    }

    //打开悬浮窗权限显示来电识别
    private void checkAndSetWindowPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                showOverlayWindowPermissionDialog();
            } else if (!AppUtils.canBackgroundStart(this) || !AppUtils.canShowLockView(this)) {
                showRequestBackGroundOrLockViewDialog();
            }
        }
    }

    //打开悬浮窗权限
    private void showOverlayWindowPermissionDialog() {
        new CustomDialog.MessageDialogBuilder(PhoneRecognizeActivity.this)
                .setMessage(getString(com.inspur.emmcloud.basemodule.R.string.permission_grant_window_alert, AppUtils.getAppName(PhoneRecognizeActivity.this)))
                .setPositiveButton(com.inspur.emmcloud.basemodule.R.string.ok, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("package:" + getPackageName());
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                        startActivityForResult(intent, REQUEST_WINDOW_PERMISSION);
                    }
                }).setNegativeButton(com.inspur.emmcloud.basemodule.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    //小米手机打开锁屏显示和后台弹出权限
    private void showRequestBackGroundOrLockViewDialog() {
        if (!AppUtils.getIsXiaoMi()) {
            return;
        }
        boolean canBackgroundStart = AppUtils.canBackgroundStart(PhoneRecognizeActivity.this);
        boolean canShowLockView = AppUtils.canShowLockView(PhoneRecognizeActivity.this);
        if (canBackgroundStart && canShowLockView) {
            return;
        }
        int message = com.inspur.emmcloud.basemodule.R.string.permission_grant_background_start;
        if (!canBackgroundStart && !canShowLockView) {
            message = com.inspur.emmcloud.basemodule.R.string.permission_grant_window_alert_lockscreen_display;
        } else if (!canShowLockView) {
            message = com.inspur.emmcloud.basemodule.R.string.permission_grant_lockscreen_display;
        }
        new CustomDialog.MessageDialogBuilder(this)
                .setMessage(getString(message, AppUtils.getAppName(this)))
                .setPositiveButton(com.inspur.emmcloud.basemodule.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQUEST_BACKGROUND_LOCKSCREEN_PERMISSION);
                    }
                }).setNegativeButton(com.inspur.emmcloud.basemodule.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
}
