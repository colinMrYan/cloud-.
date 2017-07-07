package com.inspur.emmcloud.broadcastreceiver;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.huawei.hms.support.api.push.PushReceiver;
import com.inspur.emmcloud.bean.CalendarEvent;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.work.calendar.CalEventAddActivity;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/6/20.
 */

public class HuaWeiPushReceiver extends PushReceiver{
    private static final String TAG = "HuaWei PushReceiver";



    /**
     * 连接上华为服务时会调用,可以获取token值
     *
     * @param context
     * @param token
     * @param extras
     */
    @Override
    public void onToken(Context context, String token, Bundle extras) {
//        String belongId = extras.getString("belongId");
//        String content = "get token and belongId successful, token = " + token + ",belongId = " + belongId;
//        LogUtils.YfcDebug(content);
        PreferencesUtils.putString(context,"huawei_push_token",token);
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
        try {
            String content = "-------Receive a Push pass-by message： " + new String(msg, "UTF-8");
            LogUtils.YfcDebug(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
        LogUtils.YfcDebug("pushkey："+extras.getString(BOUND_KEY.pushMsgKey));
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            int notifyId = extras.getInt(BOUND_KEY.pushNotifyId, 0);
            if (0 != notifyId) {
                NotificationManager manager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(notifyId);
            }
            String content = "--------receive extented notification message: " + extras.getString
                    (BOUND_KEY.pushMsgKey);
            LogUtils.YfcDebug(content);
        }
        String accessToken = PreferencesUtils.getString(context,
                "accessToken", "");
        if(!StringUtils.isBlank(accessToken)){
            //此处以下
//            String extra = "";
//            if (extras.containsKey(JPushInterface.EXTRA_EXTRA)) {
//                extra = extras.getString(JPushInterface.EXTRA_EXTRA);
//            }
//            if (!StringUtils.isBlank(extra)) {
//                try {
//                    JSONObject extraObj = new JSONObject(extra);
//                    if (extraObj.has("calEvent")) {
//                        String json = extraObj.getString("calEvent");
//                        JSONObject calEventObj = new JSONObject(json);
//                        openCalEvent(context, calEventObj);
//                        return;
//                    }
//                } catch (JSONException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
            startIndexActivity(context);
        }else{
            statLoginActivity(context);
        }
        super.onEvent(context, event, extras);
    }

    /**
     * 打开应用首页
     * @param context
     */
    private void startIndexActivity(Context context) {
        Intent indexLogin = new Intent(context, IndexActivity.class);
        indexLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        indexLogin.putExtra("command","open_notification");
        context.startActivity(indexLogin);
    }

    /**
     * 打开登录
     * @param context
     */
    private void statLoginActivity(Context context) {
        Intent loginIntent = new Intent(context, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(loginIntent);
    }


    /**
     * 打开
     * @param context
     * @param jsonObject
     */
    private void openCalEvent(Context context, JSONObject jsonObject) {
        // TODO Auto-generated method stub
        CalendarEvent calendarEvent = new CalendarEvent(jsonObject);
        Intent intent = new Intent();
        intent.setClass(context, CalEventAddActivity.class);
        intent.putExtra("calEvent", calendarEvent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 连接状态的回调方法
     *
     * @param context
     * @param pushState
     */
    @Override
    public void onPushState(Context context, boolean pushState) {
        try {
            String content = "---------The current push status： " + (pushState ? "Connected" :
                    "Disconnected");
            LogUtils.YfcDebug(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
