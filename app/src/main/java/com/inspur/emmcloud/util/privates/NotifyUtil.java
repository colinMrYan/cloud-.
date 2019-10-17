package com.inspur.emmcloud.util.privates;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.ui.chat.ChannelVoiceCommunicationActivity;

public class NotifyUtil {
    private NotificationManager notificationManager;
    private NotificationChannel mChannel;
    private NotificationCompat.Builder builder;
    private Context context;
    private String NotificationChannelId = "NotificationChannelId";
    private String NotificationChannelName = "NotificationChannelName";

    public NotifyUtil(Context context) {
        this.context = context;
    }

    public void setNotification(String title, String content, Class<?> cls) {

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent();
        intent.setClass(context, ChannelVoiceCommunicationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_IS_FROM_SMALL_WINDOW, true);
        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_COMMUNICATION_STATE,
                VoiceCommunicationUtils.getInstance().getLayoutState());
//        intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_TIME, Long.parseLong(
//                TimeUtils.getChronometerSeconds(SuspensionWindowManagerUtils.getInstance().getChronometer().getText().toString())));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
        builder.setSmallIcon(AppUtils.getAppIconRes(context));//设置通知提示图标
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), AppUtils.getAppIconRes(context)));//设置图标
        builder.setContentTitle(title);//设置标题
        builder.setContentText(content);//设置文本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setShowWhen(true);
        }
        builder.setDefaults(Notification.DEFAULT_LIGHTS);//消息提示模式
        builder.setOnlyAlertOnce(true);
        builder.setContentIntent(pendingIntent);

        notificationManager.notify(10000, builder.build());
    }

}
