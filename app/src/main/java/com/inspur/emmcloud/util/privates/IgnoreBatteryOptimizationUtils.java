package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import static android.content.Context.POWER_SERVICE;


/**
 * Created by libaochao on 2018/12/17.
 */

public class IgnoreBatteryOptimizationUtils {

    public static void ignoreBatteryOptimization(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PowerManager powerManager = (PowerManager) activity.getSystemService(POWER_SERVICE);
            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if (!hasIgnored) {
                Intent intent = new Intent( Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData( Uri.parse("package:" + activity.getPackageName()));
               activity.startActivity(intent);
            }
        }
    }
}
