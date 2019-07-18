package com.inspur.emmcloud.util.privates;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.util.AppUtils;

/**
 * Created by libaochao on 2019/3/18.
 */

public class UpgradeNotificationUtils {
    NotificationManager notificationManager;
    private NotificationChannel mChannel;
    private Context context;
    private NotificationCompat.Builder builder;
    private int notificationId = 10000;
    private String NotificationChannelId = "NotificationChannelId";
    private String NotificationChannelName = "NotificationChannelName";

    public UpgradeNotificationUtils(Context context, int id) {
        this.context = context;
        notificationId = id;

    }


//    public void initNotification(){
//        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
//        builder = new NotificationCompat.Builder(context,NotificationChannelId);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            mChannel = new NotificationChannel(NotificationChannelId, NotificationChannelName, NotificationManager.IMPORTANCE_LOW);
//            mChannel.setSound(null, null);
//            mChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
//            notificationManager.createNotificationChannel(mChannel);
//        }
//        builder.setTicker("");//设置信息提示
//        builder.setSmallIcon(AppUtils.getAppIconRes(context));//设置通知提示图标
//        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), AppUtils.getAppIconRes(context)));//设置图标
//        builder.setContentTitle(AppUtils.getAppName(context));//设置标题
//        builder.setContentText(context.getResources().getString(R.string.app_update_prepare));//设置文本
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            builder.setShowWhen(true);
//        }
//
//        ///< 仅仅响一次
//        builder.setOnlyAlertOnce(true);
//        //builder.setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE);
//        notificationManager.notify(notificationId, builder.build());
//    }


    /**
     * 初始化更新
     */
    public void initNotification() {
        notificationManager = (NotificationManager) context.getSystemService
                (context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(context, NotificationChannelId);
        builder.setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(NotificationChannelId, NotificationChannelName, NotificationManager.IMPORTANCE_LOW);
            mChannel.setDescription("");
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(false);
            mChannel.setVibrationPattern(new long[]{0});
            mChannel.setSound(null, null);
            notificationManager.createNotificationChannel(mChannel);
        }
        builder.setTicker("");//设置信息提示
        builder.setSmallIcon(AppUtils.getAppIconRes(context));//设置通知提示图标
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), AppUtils.getAppIconRes(context)));//设置图标
        builder.setContentTitle(AppUtils.getAppName(context));//设置标题
        builder.setContentText(context.getResources().getString(R.string.app_update_prepare));//设置文本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setShowWhen(true);
        }
        //builder.setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE);//消息提示模式
        builder.setOnlyAlertOnce(true);
        builder.setVibrate(new long[]{0});
        builder.setSound(null);
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * 更新通知栏信息
     */
    public void updateNotification(String appSizeData, boolean isOngoing) {
        builder.setOngoing(isOngoing);
        builder.setContentText(appSizeData);
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * 删除通知栏信息
     */
    public void deleteNotification() {
        notificationManager.cancel(notificationId);
    }
}
