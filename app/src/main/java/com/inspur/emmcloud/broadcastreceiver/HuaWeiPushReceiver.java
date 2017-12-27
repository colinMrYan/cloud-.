package com.inspur.emmcloud.broadcastreceiver;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.huawei.hms.support.api.push.PushReceiver;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.ECMShortcutBadgeNumberManagerUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.PreferencesUtils;

/**
 * Created by yufuchang on 2017/6/20.
 */
public class HuaWeiPushReceiver extends PushReceiver {
    /**
     * 连接上华为服务时会调用,可以获取token值
     *
     * @param context
     * @param token
     * @param extras
     */
    @Override
    public void onToken(Context context, String token, Bundle extras) {
        PreferencesUtils.putString(context, "huawei_push_token", token);
        WebSocketPush.getInstance(context).start();
    }

    /**
     * 透传消息的回调方法
     *
     * @param context
     * @param msg
     * @param bundle
     * @return
     */
    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        if(ECMShortcutBadgeNumberManagerUtils.isHasBadge(msg)){
            ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(context,getDesktopBadgeNumber(msg));
        }
        return false;
    }

    /**
     * 获取桌面badge的数字
     * @param msg
     * @return
     */
    private int getDesktopBadgeNumber(byte[] msg) {
        int badageNumber = 0;
        try {
            String message = new String(msg,"UTF-8");
            badageNumber = JSONUtils.getInt(message,"badge",0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return badageNumber;
    }

    /**
     * 自定义的消息的回调方法，自定义消息的字数应该是没有限制，目前测试到160字
     * 通知文本的内容有字数限制80字
     *
     * @param context
     * @param event
     * @param extras
     */
    @Override
    public void onEvent(Context context, PushReceiver.Event event, Bundle extras) {
        super.onEvent(context, event, extras);
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
        }
    }

    /**
     * 连接状态的回调方法
     *
     * @param context
     * @param pushState
     */
    @Override
    public void onPushState(Context context, boolean pushState) {

    }

}
