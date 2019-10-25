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

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.bean.chat.VoiceCommunicationJoinChannelInfoBean;
import com.inspur.emmcloud.ui.appcenter.volume.VolumeFileActivity;
import com.inspur.emmcloud.ui.chat.ChannelVoiceCommunicationActivity;

import java.util.List;

import static com.inspur.emmcloud.ui.chat.ChannelVoiceCommunicationActivity.COMMUNICATION_STATE_OVER;

public class NotifyUtil {
    private static NotificationManager notificationManager;
    private NotificationChannel mChannel;
    private NotificationCompat.Builder builder;
    private Context context;
    private String NotificationChannelId = "VoiceNotificationChannelId";
    private String NotificationChannelName = "VoiceNotificationChannelName";

    public NotifyUtil(Context context) {
        this.context = context;
    }

    public static void deleteNotify(Context context) {
        NotifyUtil notifyUtil = new NotifyUtil(context);
        if (notificationManager != null) {
            notificationManager.cancel(10006);
        }
    }

    public static void sendNotifyMsg(Context context) {
        if (VoiceCommunicationUtils.getInstance().getCommunicationState() != COMMUNICATION_STATE_OVER &&
                VoiceCommunicationUtils.getInstance().getCommunicationState() != -1) {
            NotifyUtil notifyUtil = new NotifyUtil(context);
            String title = "";
            String content = "";
            List<VoiceCommunicationJoinChannelInfoBean> voiceCommunicationMemberList =
                    VoiceCommunicationUtils.getInstance().getVoiceCommunicationMemberList();
            if (voiceCommunicationMemberList.size() > 2) {  //群聊
                title = context.getResources().getString(R.string.voice_communication_notification_group_title);
                content = context.getResources().getString(R.string.voice_communication_notification_group_content);
            } else if (voiceCommunicationMemberList.size() == 2) {    //单聊
                for (VoiceCommunicationJoinChannelInfoBean bean : voiceCommunicationMemberList) {
                    if (!bean.getUserId().equals(BaseApplication.getInstance().getUid())) {
                        title = bean.getUserName();
                    }
                }
                content = context.getResources().getString(R.string.voice_communication_notification_content);
            }
            notifyUtil.setNotification(title, content, ChannelVoiceCommunicationActivity.class);
        }
    }

    public static void sendTestNotify(Context context) {
        NotifyUtil notifyUtil = new NotifyUtil(context);
        notifyUtil.setNotification("测试title", "测试内容", VolumeFileActivity.class);
    }

    public void setNotification(String title, String content, Class<?> cls) {

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = null;
        try {
            intent = Intent.parseUri("ecc-cloudplus-cmd-voice-call://voice_call", Intent.URI_INTENT_SCHEME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_IS_FROM_SMALL_WINDOW, true);
            intent.putExtra(ChannelVoiceCommunicationActivity.VOICE_COMMUNICATION_STATE,
                    VoiceCommunicationUtils.getInstance().getLayoutState());
        } catch (Exception e) {
            e.printStackTrace();
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(context, NotificationChannelId);
        builder.setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(NotificationChannelId, NotificationChannelName, NotificationManager.IMPORTANCE_HIGH);
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
        builder.setAutoCancel(false);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(10006, builder.build());
    }

}
