package com.inspur.emmcloud.util.privates;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.inspur.emmcloud.R;

/**
 * Created by libaochao on 2019/3/12.
 */

public class NotificationUtils {

    NotificationManager notificationManager;
    private NotificationChannel mChannel;
    private Context context;
    private NotificationCompat.Builder builder;
    private int notificationId = 10000;
    private String NotificationChannelId = "NotificationChannelId";
    private String NotificationChannelName = "NotificationChannelName";

    public NotificationUtils(Context context, int id) {
        this.context = context;
        notificationId = id;
        initNotification();
    }

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
            mChannel.enableVibration(true);
            notificationManager.createNotificationChannel(mChannel);
        }
        builder.setTicker("");//设置信息提示
        builder.setSmallIcon(R.drawable.ic_launcher);//设置通知提示图标
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));//设置图标
        builder.setContentTitle(context.getResources().getString(R.string.app_name));//设置标题
        builder.setContentText(context.getResources().getString(R.string.app_update_prepare));//设置文本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setShowWhen(true);
        }
        builder.setDefaults(Notification.DEFAULT_LIGHTS);//消息提示模式
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * 更新通知栏信息
     */
    public void updateNotification(String appSizeData) {
        builder.setContentText(appSizeData);
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * 删除通知栏信息
     */
    public void delectNotification() {
        notificationManager.cancel(notificationId);
    }


}
