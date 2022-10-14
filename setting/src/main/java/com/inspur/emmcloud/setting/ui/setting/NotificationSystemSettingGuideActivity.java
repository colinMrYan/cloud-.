package com.inspur.emmcloud.setting.ui.setting;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        ignoredPowerSaving = false;
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

    // 确认系统通知状态
    public static boolean isNotificationEnabled(Context context) {
        AppOpsManager mAppOps = (AppOpsManager)
                context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        try {
            Class appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (int) opPostNotificationValue.get(Integer.class);
            return ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
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
