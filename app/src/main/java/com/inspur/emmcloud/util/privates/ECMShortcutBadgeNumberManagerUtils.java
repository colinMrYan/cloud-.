package com.inspur.emmcloud.util.privates;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.cache.AppExceptionCacheUtils;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by yufuchang on 2017/12/5.
 */

public class ECMShortcutBadgeNumberManagerUtils {

    private static final String NOTIFICATION_CHANNEL = "com.inspur.emmcloud.channel";
//    /**
//     * 设置桌面角标
//     * @param context
//     */
//    public static void setDesktopBadgeNumber(Context context,int count,Intent intent) {
//        if(intent != null){
//            String miuiVersionString = getSystemProperty("ro.miui.ui.version.name");
//            int miuiVersionNum = (StringUtils.isBlank(miuiVersionString))? -1 : Integer.parseInt(miuiVersionString.substring(1));
//            if(miuiVersionNum >= 6){
//                setMIUIV6PlusBadge(context,count,intent);
//                return;
//            }
//        }
//        ShortcutBadger.applyCount(context,count);
//    }

    /**
     * 判断byte里是否含有badge
     *
     * @param msg
     * @return
     */
    public static boolean isHasBadge(byte[] msg) {
        if (msg == null) {
            return false;
        }
        try {
            String message = new String(msg, "UTF-8");
            JSONObject jsonObject = new JSONObject(message);
            return jsonObject.has("badge");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断字符串是否含有badge
     *
     * @param msg
     * @return
     */
    public static boolean isHasBadge(String msg) {
        if (StringUtils.isBlank(msg)) {
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(msg);
            return jsonObject.has("badge");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置桌面角标
     *
     * @param context
     */
    public static void setDesktopBadgeNumber(Context context, int count) {
        if (!AppUtils.GetChangShang().toLowerCase().startsWith(Constant.XIAOMI_FLAG)) {
            try {
                ShortcutBadger.applyCount(context, count);
            } catch (Exception e) {
                AppExceptionCacheUtils.saveAppException(context, Constant.APP_EXCEPTION_LEVEL, "Desktop badge count", e.getMessage(), -1);
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置小米MIUI6以及以上的桌面角标
     *
     * @param context
     * @param mCount
     */
    public static void setMIUIV6PlusBadge(final Context context, final int mCount, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent clickIntent = new Intent(Intent.ACTION_VIEW);
        clickIntent.setData(Uri.parse("inspur-ecc-native://meeting"));
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        clickIntent.putExtras(intent.getExtras());
        PendingIntent clickPendingIntent = PendingIntent.getActivity(context, 0, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification =new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL)
                .setContentTitle("测试notification")
                .setContentIntent(clickPendingIntent)
                .setAutoCancel(true)
                .setContentText("This is content text")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel(notificationManager);
        }
        try {
            Field field = notification.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notification);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
            method.invoke(extraNotification, mCount);
        } catch (Exception e) {
            LogUtils.YfcDebug("小米推送异常："+e.getMessage());
        }
        notificationManager.notify(0, notification);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void setupNotificationChannel(NotificationManager mNotificationManager) {
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, "Cloud+ Sample",
                NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager.createNotificationChannel(channel);
    }
}
