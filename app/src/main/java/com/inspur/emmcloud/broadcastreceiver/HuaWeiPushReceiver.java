package com.inspur.emmcloud.broadcastreceiver;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.huawei.hms.support.api.push.PushReceiver;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;

import org.json.JSONArray;
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
        if(((MyApplication)context.getApplicationContext()).isIndexActivityRunning() ){
            WebSocketPush.getInstance(context).start();
        }
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
        super.onEvent(context, event, extras);
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancelAll();
            if (((MyApplication)context.getApplicationContext()).isHaveLogin()){
               if(NetUtils.isNetworkConnected(context,false)){
                    String content = extras.getString
                            (BOUND_KEY.pushMsgKey);
                    try {
                        JSONArray array = new JSONArray(content);
                        JSONObject msgObj = array.getJSONObject(0);
                        JSONObject actionObj = msgObj.getJSONObject("action");
                        String type = actionObj.getString("type");
                        Intent targetIntent = null;
                        if (type.equals("open-url")){
                            String scheme = actionObj.getString("url");
                            targetIntent =new Intent(Intent.ACTION_VIEW);
                            targetIntent.setData(Uri.parse(scheme));
                            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        if (targetIntent != null ){
                            LogUtils.jasonDebug("start---target-------------");
                            context.startActivity(targetIntent);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }else {
                Intent loginIntent = new Intent(context, LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(loginIntent);
            }


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
        try {
            String content = "---------The current push status： " + (pushState ? "Connected" :
                    "Disconnected");
            LogUtils.YfcDebug(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
