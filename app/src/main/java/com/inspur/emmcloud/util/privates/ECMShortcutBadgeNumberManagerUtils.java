package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.cache.AppExceptionCacheUtils;

import org.json.JSONObject;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by yufuchang on 2017/12/5.
 */

public class ECMShortcutBadgeNumberManagerUtils {

    private static final String NOTIFICATION_CHANNEL = "com.inspur.emmcloud.channel_1";
    private static final String NOTIFICATION_CHANNEL_GROUP_ID = "com.inspur.emmcloud.group";
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

//    /**
//     * 设置小米MIUI6以及以上的桌面角标
//     * 连同下面setupNotificationChannel方法，可以实现对小米桌面角标的设置在mi6和mi9上测试过，测试时发现钉钉在后台或杀死在mi9上都不能接收到推送
//     * 结果：功能可以实现
//     * 存在问题
//     * 1，发两次自定义通知才可以设置角标数字
//     * 2，必须依赖通知才能展示角标
//     * @param context
//     * @param mCount
//     */
//    public static void setMIUIV6PlusBadge(final Context context, final int mCount, Intent intent) {
//        NotificationManager notificationManager = (NotificationManager) context
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent clickIntent = new Intent(Intent.ACTION_VIEW);
//        clickIntent.setData(Uri.parse("inspur-ecc-native://meeting"));
//        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        clickIntent.putExtras(intent.getExtras());
//        PendingIntent clickPendingIntent = PendingIntent.getActivity(context, 0, clickIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            setupNotificationChannel(notificationManager);
//        }
//        Notification notification =new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL)
//                .setContentTitle("测试notification")
//                .setContentIntent(clickPendingIntent)
//                .setAutoCancel(true)
//                .setContentText("This is content text")
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.drawable.ic_launcher)
//                .build();
//
//        try {
//            Field field = notification.getClass().getDeclaredField("extraNotification");
//            Object extraNotification = field.get(notification);
//            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
//            method.setAccessible(true);
//            method.invoke(extraNotification, mCount);
//        } catch (Exception e) {
//            LogUtils.YfcDebug("小米推送异常："+e.getMessage());
//        }
//        notificationManager.notify(0, notification);
//    }
//
//    @TargetApi(Build.VERSION_CODES.O)
//    private static void setupNotificationChannel(NotificationManager mNotificationManager) {
////        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
////        intent.putExtra(Settings.EXTRA_CHANNEL_ID, NOTIFICATION_CHANNEL);
////        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
////        context.startActivity(intent);
//        mNotificationManager.createNotificationChannelGroup(new NotificationChannelGroup(NOTIFICATION_CHANNEL_GROUP_ID, NOTIFICATION_CHANNEL_GROUP_ID));
//        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, "CloudPlus",
//                NotificationManager.IMPORTANCE_HIGH);
//        channel.setBypassDnd(true);
//        channel.setShowBadge(true);
//        channel.setGroup(NOTIFICATION_CHANNEL_GROUP_ID);
//        mNotificationManager.createNotificationChannel(channel);
//    }
}
