package com.inspur.emmcloud.setting.ui.setting;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.NotificationSetUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationSettingActivity extends BaseActivity {
    @BindView(R2.id.switch_setting_notification)
    SwitchCompat notificationSwitch;

    private LoadingDialog loadingDlg;
    private static final int REQUEST_SETTING_NOTIFICATION = 1;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationSwitch.setChecked(getSwitchOpen());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_mine_setting_notification;
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {    //代表上升沿 先检测
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        showNotificationDlg(isChecked);
                    } else {
                        PreferencesByUserAndTanentUtils.putBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, true);
                        switchPush();
                        notificationSwitch.setChecked(true);
                    }
                } else {
                    if (NotificationSetUtils.isNotificationEnabled(NotificationSettingActivity.this)) {
                        showNotificationCloseDlg();
                    } else {
                        PreferencesByUserAndTanentUtils.putBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, false);
                        notificationSwitch.setChecked(false);
                    }
                }
            }
        });
    }

    /**
     * 弹出注销提示框
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showNotificationDlg(boolean isChecked) {
        if (!NotificationSetUtils.isNotificationEnabled(NotificationSettingActivity.this)) {
            PreferencesByUserAndTanentUtils.putBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, false);
            notificationSwitch.setChecked(false);
            new CustomDialog.MessageDialogBuilder(NotificationSettingActivity.this)
                    .setCancelable(false)
                    .setMessage(getString(R.string.notification_switch_open_setting))
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            NotificationSetUtils.openNotificationSetting(NotificationSettingActivity.this);
                        }
                    })
                    .show();
        } else {
            PreferencesByUserAndTanentUtils.putBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, isChecked);
            PushManagerUtils.getInstance().startPush();
            notificationSwitch.setChecked(isChecked);
        }
    }

    /**
     * 开关push并向服务器发出信号
     */
    private void switchPush() {
        boolean switchFlag = PreferencesByUserAndTanentUtils.getBoolean(this,
                Constant.PUSH_SWITCH_FLAG, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setPushStatus(NotificationSetUtils.isNotificationEnabled(this) && switchFlag);
        } else {
            setPushStatus(switchFlag);
        }
    }

    private void setPushStatus(boolean openPush) {
        if (openPush) {
            PushManagerUtils.getInstance().startPush();
            PushManagerUtils.getInstance().registerPushId2Emm();
        } else {
            PushManagerUtils.getInstance().stopPush();
            PushManagerUtils.getInstance().unregisterPushId2Emm();
        }
    }

    private void showNotificationCloseDlg() {
        new CustomDialog.MessageDialogBuilder(NotificationSettingActivity.this)
                .setMessage(R.string.notification_switch_cant_recive)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notificationSwitch.setChecked(true);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        notificationSwitch.setChecked(false);
                        PreferencesByUserAndTanentUtils.putBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, false);
                        switchPush();
                    }
                })
                .show();
    }

    private boolean getSwitchOpen() {
        boolean isOpen = PreferencesByUserAndTanentUtils.getBoolean(NotificationSettingActivity.this, Constant.PUSH_SWITCH_FLAG, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !NotificationSetUtils.isNotificationEnabled(this)) {
            isOpen = false;
        }
        return isOpen;
    }

    protected void batteryWhiteListRemind(final Context context) {
//        batteryDialogIsShow = PreferencesUtils.getBoolean(context, Constant.BATTERY_WHITE_LIST_STATE, true);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            try {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
                if (!hasIgnored) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    startActivity(intent);
                } else {
                    ToastUtils.show(R.string.power_save_alert);
                }
            } catch (Exception e) {
                e.printStackTrace();
                PreferencesUtils.putBoolean(context, Constant.BATTERY_WHITE_LIST_STATE, false);
            }
        }
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();
        }
        // 通知指南
        if (i == R.id.notification_guide_layout) {
            Bundle bundle = new Bundle();
            // 替换其它链接
            bundle.putString("uri", "https://emmcloud.yuque.com/docs/share/ab914054-3409-4548-a89d-4565bd551967?#");
            bundle.putString("appName", "通知设置指南");
            bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, true);
            ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
            return;
        }
        // 系统通知设置
        if (i == R.id.notification_system_setting_layout) {
            gotoNotificationSetting(this);
            return;
        }
        // 省电模式
        if (i == R.id.notification_power_saving_layout) {
            batteryWhiteListRemind(this);
        }
    }


    public static void gotoNotificationSetting(Activity activity) {
        ApplicationInfo appInfo = activity.getApplicationInfo();
        String pkg = activity.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, pkg);
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, uid);
                //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                intent.putExtra("app_package", pkg);
                intent.putExtra("app_uid", uid);
                activity.startActivityForResult(intent, REQUEST_SETTING_NOTIFICATION);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, REQUEST_SETTING_NOTIFICATION);
            } else {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                activity.startActivityForResult(intent, REQUEST_SETTING_NOTIFICATION);
            }
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivityForResult(intent, REQUEST_SETTING_NOTIFICATION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
