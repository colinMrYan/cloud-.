package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ClientIDUtils;
import com.inspur.emmcloud.util.privates.ECMShortcutBadgeNumberManagerUtils;
import com.inspur.emmcloud.util.privates.ECMTransparentUtils;
import com.inspur.emmcloud.util.privates.PushManagerUtils;

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

    /**
     * 打印所有的 intent extra 数据
     *
     * @param bundle
     * @return
     */
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

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        LogUtils.debug(TAG, "[MyReceiver] onReceive - " + intent.getAction()
                + ", extras: " + printBundle(bundle));
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle
                    .getString(JPushInterface.EXTRA_REGISTRATION_ID);
            LogUtils.debug(TAG, "[MyReceiver] 接收Registration Id : " + regId);
            PushManagerUtils.setPushFlag(context, Constant.JPUSH_FLAG);
            PreferencesUtils.putString(context, Constant.JPUSH_REGISTER_ID, regId);
            PushManagerUtils.getInstance().registerPushId2Emm();
            new ClientIDUtils(context).upload();
            WebSocketPush.getInstance().startWebSocket();
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
                .getAction())) {
            ECMTransparentUtils.handleTransparentMsg(context, bundle.getString(JPushInterface.EXTRA_MESSAGE));
            LogUtils.debug(TAG,
                    "[MyReceiver] 接收到推送下来的自定义消息: "
                            + bundle.getString(JPushInterface.EXTRA_MESSAGE));

            return;
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
                .getAction())) {
            int notifactionId = bundle
                    .getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            LogUtils.debug(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
            String extraMessage = bundle.getString(JPushInterface.EXTRA_EXTRA);
            if (!AppUtils.GetChangShang().toLowerCase().startsWith(Constant.XIAOMI_FLAG) && JSONUtils.isJsonObjStringHasKey(extraMessage, "badge")) {
                ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(context, JSONUtils.getInt(extraMessage, "badge", 0));
            }
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
            openNotification(context, bundle);
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
     *
     * @param context
     * @param bundle
     */
    private void openNotification(final Context context, Bundle bundle) {
        String extra = "";
        if (bundle.containsKey(JPushInterface.EXTRA_EXTRA)) {
            extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
        }
        if (!StringUtils.isBlank(extra)) {
            try {
                final JSONObject extraObj = new JSONObject(extra);
//                //日历提醒的通知
//                if (extraObj.has("schedule")) {
//                    String type = JSONUtils.getString(extraObj,"type","");
//                    Intent intent = new Intent();
//                    if (type.equals(Schedule.TYPE_CALENDAR)){
//                        JSONObject scheduleObj = JSONUtils.getJSONObject(extraObj,"schedule",new JSONObject());
//                        Schedule schedule = new Schedule(scheduleObj);
//                        intent.setClass(context,CalendarAddActivity.class);
//                        intent.putExtra(CalendarAddActivity.EXTRA_SCHEDULE_CALENDAR_EVENT, schedule);
//                        context.startActivity(intent);
//                    }else if(type.equals(Schedule.TYPE_MEETING)){
//                        JSONObject meetingObj = JSONUtils.getJSONObject(extraObj,"schedule",new JSONObject());
//                        Meeting meeting = new Meeting(meetingObj);
//                        intent.setClass(context,MeetingDetailActivity.class);
//                        intent.putExtra(MeetingDetailActivity.EXTRA_MEETING_ENTITY, meeting);
//                        context.startActivity(intent);
//                    }else{
//
//                    }
//                } else
                    if (extraObj.has("action")) {//用scheme打开相应的页面
                    openScheme(context, extraObj);
                } else if (extraObj.has("channel")) {
                    String cid = JSONUtils.getString(extraObj, "channel", "");
                    if (!StringUtils.isBlank(cid)) {
                        JSONObject actionObj = new JSONObject();
                        actionObj.put("url", "ecc-channel://" + cid);
                        actionObj.put("type", "open-url");
                        JSONObject obj = new JSONObject();
                        obj.put("action", actionObj);
                        openScheme(context, obj);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 打开scheme
     *
     * @param context
     * @param extraObj
     */
    private void openScheme(Context context, JSONObject extraObj) {
        try {
            JSONObject actionObj = extraObj.getJSONObject("action");
            String type = actionObj.getString("type");
            if (type.equals("open-url")) {
                String scheme = actionObj.getString("url");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(scheme));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (extraObj.has("content")) {
                    String content = extraObj.getString("content");
                    intent.putExtra("content", content);
                }
                MyApplication.getInstance().setOpenNotification(true);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
