package com.inspur.emmcloud.setting.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationSystemSettingGuideActivity extends BaseActivity {
    @BindView(R2.id.error_info)
    TextView alertView;
    private static final int REQUEST_SETTING_NOTIFICATION = 100;
    private static final int REQUEST_POWER_UPDATE = 200;
    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
    private PowerManager powerManager;
    // 打开省电模式后无法马上监听到状态变化,需要返回上一页
    private int mark = 0;
    private boolean ignoredPowerSaving = false;
    private boolean hasOpenNotification = false;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mark == 0) {
            refreshView(false);
        }
        mark = 0;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_mine_system_setting_guide_notification;
    }

    private void refreshView(boolean forceIgnorePowerState) {
        if (powerManager == null) powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        ignoredPowerSaving = true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ignoredPowerSaving = powerManager.isIgnoringBatteryOptimizations(getPackageName()) || forceIgnorePowerState;
        }
        hasOpenNotification = isNotificationEnabled(this);
        if (!ignoredPowerSaving || !hasOpenNotification) {
            alertView.setVisibility(View.VISIBLE);
        } else {
            alertView.setVisibility(View.GONE);
        }
        if (!hasOpenNotification && !ignoredPowerSaving) {
            alertView.setText(R.string.no_notification_and_power);
        } else if (!hasOpenNotification) {
            alertView.setText(R.string.no_notification);
        } else if (!ignoredPowerSaving) {
            alertView.setText(R.string.no_power_save);
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
            bundle.putString("uri", "http://emm.inspuronline.com:83/AppMessageConfigAndroid.html");
            bundle.putString("appName", "通知设置指南");
            bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, true);
            ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
            return;
        }
        // 系统通知设置
        if (i == R.id.notification_system_setting_layout) {
            gotoNotificationSetting();
            return;
        }
        // 省电模式
        if (i == R.id.notification_power_saving_layout) {
            batteryWhiteListRemind(this);
        }
    }

    protected void batteryWhiteListRemind(final Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            try {
                powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
                if (!hasIgnored) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    startActivityForResult(intent, REQUEST_POWER_UPDATE);
                } else {
                    ToastUtils.show(R.string.power_save_alert);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 进入系统通知设置界面
    public void gotoNotificationSetting() {
        String packageName = BaseApplication.getInstance().getPackageName();
        try {
            // 根据通知栏开启权限判断结果，判断是否需要提醒用户跳转系统通知管理页面
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
            }
            //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
            intent.putExtra("app_package", packageName);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            // 出现异常则跳转到应用设置界面
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", packageName, null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    // 确认系统通知状态
    public static boolean isNotificationEnabled(Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        return manager.areNotificationsEnabled();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == -1) {
            refreshView(true);
            mark = 1;
        }
    }
}
