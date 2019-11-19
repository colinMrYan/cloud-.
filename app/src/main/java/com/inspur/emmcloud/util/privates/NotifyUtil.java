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
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.ui.chat.VoiceCommunicationActivity;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import static com.inspur.emmcloud.ui.chat.VoiceCommunicationActivity.COMMUNICATION_STATE_OVER;

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
        if (notificationManager != null) {
            notificationManager.cancel(10006);
        }
    }

    public static void sendNotifyMsg(Context context) {
        if (VoiceCommunicationManager.getInstance().getCommunicationState() != COMMUNICATION_STATE_OVER) {
            NotifyUtil notifyUtil = new NotifyUtil(context);
            String title = "";
            String content = "";
            Conversation conversation = ConversationCacheUtils.getConversation(BaseApplication.getInstance(), VoiceCommunicationManager.getInstance().getCloudPlusChannelId());
            String directOrGroupType = conversation.getType();
            if (directOrGroupType.equals(Conversation.TYPE_GROUP)) {  //群聊
                title = context.getResources().getString(R.string.voice_communication_notification_group_title);
                content = context.getResources().getString(R.string.voice_communication_notification_group_content);
            } else if (directOrGroupType.equals(Conversation.TYPE_DIRECT)) {    //单聊
                title = conversation.getShowName();
                content = context.getResources().getString(R.string.voice_communication_notification_content);
            } else {
                return;
            }
            notifyUtil.setNotification(title, content, VoiceCommunicationActivity.class);
        }
    }

    public void setNotification(String title, String content, Class<?> cls) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = null;
        try {
            intent = Intent.parseUri("ecc-cloudplus-cmd-voice-call://voice_call", Intent.URI_INTENT_SCHEME);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constant.VOICE_IS_FROM_SMALL_WINDOW, true);
            intent.putExtra(Constant.VOICE_COMMUNICATION_STATE,
                    VoiceCommunicationManager.getInstance().getCommunicationState());
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
            mChannel.enableVibration(false);
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
