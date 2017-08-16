package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.CalendarEvent;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.work.calendar.CalEventAddActivity;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * <p>
 * 如果不定义这个 Receiver，则： 1) 默认用户会打开主界面 2) 接收不到自定义消息
 */
public class JpushReceiver extends BroadcastReceiver {
    private static final String TAG = "JPush";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        LogUtils.debug(TAG, "[MyReceiver] onReceive - " + intent.getAction()
                + ", extras: " + printBundle(bundle));
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle
                    .getString(JPushInterface.EXTRA_REGISTRATION_ID);
            LogUtils.debug(TAG, "[MyReceiver] 接收Registration Id : " + regId);
            PreferencesUtils.putString(context, "JpushRegId", regId);
            ((MyApplication) context.getApplicationContext()).startWebSocket();
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
                .getAction())) {
            LogUtils.debug(TAG,
                    "[MyReceiver] 接收到推送下来的自定义消息: "
                            + bundle.getString(JPushInterface.EXTRA_MESSAGE));

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
                .getAction())) {
            int notifactionId = bundle
                    .getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            LogUtils.debug(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
                .getAction())) {
            LogUtils.debug(TAG, "[MyReceiver] 用户点击打开了通知");
            LogUtils.debug(TAG, "extra=" + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //点击打开通知进入应用时清空所有的通知
            ((MyApplication) context.getApplicationContext()).clearNotification();
            //点击通知进入时判断当前是否已登录
            if (!((MyApplication) context.getApplicationContext()).isHaveLogin()) {
                loginApp(context);
                return;
            }
            //如果应用正在前台运行，不处理点击通知的动作
            if (((MyApplication) context.getApplicationContext()).getIsActive()) {
                return;
            }
            openNotifycation(context,bundle);
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent
                .getAction())) {
            LogUtils.debug(TAG,
                    "[MyReceiver] 用户收到到RICH PUSH CALLBACK: "
                            + bundle.getString(JPushInterface.EXTRA_EXTRA));
        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent
                .getAction())) {
            boolean connected = intent.getBooleanExtra(
                    JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            Log.w(TAG, "[MyReceiver]" + intent.getAction()
                    + " connected state change to " + connected);
        } else {
            LogUtils.debug(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
    }

    /**
     * 登录应用
     *
     * @param context
     */
    private void loginApp(Context context) {
        Intent loginIntent = new Intent(context, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(loginIntent);
    }

    /**
     * 打开通知相应的界面
     * @param context
     * @param bundle
     */
    private  void openNotifycation(Context context,Bundle bundle){
        openIndexActivity(context);
        String extra = "";
        if (bundle.containsKey(JPushInterface.EXTRA_EXTRA)) {
            extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
        }
        if (!StringUtils.isBlank(extra)) {
            try {
                JSONObject extraObj = new JSONObject(extra);
                //日历提醒的通知
                if (extraObj.has("calEvent")) {
                    openCalEvent(context, extraObj);
                } else if (extraObj.has("action")) {//用scheme打开相应的页面
                    openScheme(context, extraObj);
                } else if (extraObj.has("channel")) {
                    openChannel(context, extraObj);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开主tab页
     *
     * @param context
     */
    private void openIndexActivity(Context context) {
        Intent indexIntent = new Intent(context, IndexActivity.class);
        if (!((MyApplication) context.getApplicationContext()).isIndexActivityRunning()) {
            indexIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(indexIntent);
        } else if (!((MyApplication) context.getApplicationContext()).getIsActive()) {
            indexIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(indexIntent);
        }
    }

    /**
     * 打开日历详情
     *
     * @param context
     * @param extraObj
     */
    private void openCalEvent(Context context, JSONObject extraObj) throws Exception {
        String json = extraObj.getString("calEvent");
        JSONObject calEventObj = new JSONObject(json);
        CalendarEvent calendarEvent = new CalendarEvent(calEventObj);
        Intent intent = new Intent(context, CalEventAddActivity.class);
        intent.putExtra("calEvent", calendarEvent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 打开scheme
     *
     * @param context
     * @param extraObj
     */
    private void openScheme(Context context, JSONObject extraObj) throws Exception {
        JSONObject actionObj = extraObj.getJSONObject("action");
        String type = actionObj.getString("type");
        if (type.equals("open-url")) {
            String scheme = actionObj.getString("url");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(scheme));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * 打开频道聊天页面
     *
     * @param context
     * @param extraObj
     */
    private void openChannel(Context context, JSONObject extraObj) {
        String cid = JSONUtils.getString(extraObj, "channel", "");
        if (!StringUtils.isBlank(cid)) {
            Intent intent = new Intent(context, ChannelActivity.class);
            intent.putExtra("get_new_msg", true);
            intent.putExtra("cid", cid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
                if (bundle.getString(JPushInterface.EXTRA_EXTRA).isEmpty()) {
                    Log.i(TAG, "This message has no Extra data");
                    continue;
                }

                try {
                    JSONObject json = new JSONObject(
                            bundle.getString(JPushInterface.EXTRA_EXTRA));
                    Iterator<String> it = json.keys();

                    while (it.hasNext()) {
                        String myKey = it.next().toString();
                        sb.append("\nkey:" + key + ", value: [" + myKey + " - "
                                + json.optString(myKey) + "]");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Get message extra JSON error!");
                }

            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }

}
