package com.inspur.emmcloud.basemodule.push;

import android.content.Context;
import android.content.Intent;

import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class JPushMessageExtendReceiver extends JPushMessageReceiver {
    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        super.onMessage(context, customMessage);
    }

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageArrived(context, notificationMessage);
    }

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageOpened(context, notificationMessage);
    }

    @Override
    public void onMultiActionClicked(Context context, Intent intent) {
        super.onMultiActionClicked(context, intent);
    }
}
