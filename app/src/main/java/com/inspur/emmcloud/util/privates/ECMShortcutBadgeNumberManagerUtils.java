package com.inspur.emmcloud.util.privates;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.cache.AppExceptionCacheUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.jpush.android.api.JPushInterface;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by yufuchang on 2017/12/5.
 */

public class ECMShortcutBadgeNumberManagerUtils {

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
     * @param msg
     * @return
     */
    public static boolean isHasBadge(byte[] msg){
        if(msg == null){
            return false;
        }
        try {
            String message = new String(msg,"UTF-8");
            JSONObject jsonObject = new JSONObject(message);
            return jsonObject.has("badge");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断字符串是否含有badge
     * @param msg
     * @return
     */
    public static boolean isHasBadge(String msg){
        if(StringUtils.isBlank(msg)){
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
     * @param context
     */
    public static void setDesktopBadgeNumber(Context context,int count) {
        if(!AppUtils.GetChangShang().toLowerCase().startsWith(Constant.XIAOMI_FLAG)){
            try {
                ShortcutBadger.applyCount(context,count);
            }catch (Exception e){
                AppExceptionCacheUtils.saveAppException(context,Constant.APP_EXCEPTION_BADGE,"","DesktopBadge Count Error",500);
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取系统配置
     * @param propName
     * @return
     */
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (Exception ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    /**
     * 设置小米MIUI6以及以上的桌面角标
     * @param context
     * @param mCount
     */
    private static void setMIUIV6PlusBadge(Context context, int mCount,Intent intent){
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(intent.getExtras().getString(JPushInterface.EXTRA_NOTIFICATION_TITLE))
                .setContentText(intent.getExtras().getString(JPushInterface.EXTRA_ALERT))
                .setAutoCancel(true).setPriority(Notification.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_VIBRATE).setSmallIcon(R.drawable.ic_launcher);
        Intent clickIntent = new Intent("com.inspur.emmcloud.openjpush");
        clickIntent.putExtras(intent.getExtras());
        PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(clickPendingIntent);
        Notification notification = builder.build();
        notificationManager.cancelAll();
        try {
            Field field = notification.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notification);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
            method.invoke(extraNotification, mCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        notificationManager.notify(mCount,notification);
    }

    /**
     * 创建并展示一个通知
     * @param context
     * @param notifyId
     * @param title
     * @param content
     */
    public static void createAndShowNotification(Context context,int notifyId,String title,String content) {
        //全局通知管理者，通过获取系统服务获取
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //通知栏构造器,创建通知栏样式
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        // 将来意图，用于点击通知之后的操作,内部的new intent()可用于跳转等操作
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 1, new Intent(), notification.flags);
        //设置通知栏标题
        mBuilder.setContentTitle(title)
                //设置通知栏显示内容
                .setContentText(content)
                //设置通知栏点击意图
                .setContentIntent(mPendingIntent)
                //通知首次出现在通知栏，带上升动画效果的
//                .setTicker("测试通知来啦")
                //通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setWhen(System.currentTimeMillis())
                //设置该通知优先级
                .setPriority(Notification.PRIORITY_DEFAULT)
                //设置这个标志当用户单击面板就可以让通知将自动取消
                .setAutoCancel(true)
                //使用当前的用户默认设置
                .setDefaults(Notification.DEFAULT_VIBRATE)
                //设置通知小ICON(应用默认图标)
                .setSmallIcon(R.drawable.ic_launcher);
        mNotificationManager.notify(notifyId, mBuilder.build());
    }
}
