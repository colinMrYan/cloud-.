package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.StringUtils;

import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by yufuchang on 2017/12/20.
 */

public class OpenJpushReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
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
        openNotifycation(context, bundle);
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
    private void openNotifycation(final Context context, Bundle bundle) {
        String extra = "";
        if (bundle.containsKey(JPushInterface.EXTRA_EXTRA)) {
            extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
        }
        if (!StringUtils.isBlank(extra)) {
            try {
                final JSONObject extraObj = new JSONObject(extra);
                //日历提醒的通知
                if (extraObj.has("calEvent")) {
                    String json = extraObj.getString("calEvent");
                    JSONObject actionObj = new JSONObject();
                    actionObj.put("url","ecc-calendar-jpush://");
                    actionObj.put("type","open-url");
                    actionObj.put("content",json);
                    JSONObject obj = new JSONObject();
                    obj.put("action",actionObj);
                    openScheme(context, obj);

                } else if (extraObj.has("action")) {//用scheme打开相应的页面
                    openScheme(context, extraObj);
                } else if (extraObj.has("channel")) {
                    String cid = JSONUtils.getString(extraObj, "channel", "");
                    if (!StringUtils.isBlank(cid)){
                        JSONObject actionObj = new JSONObject();
                        actionObj.put("url","ecc-channel://"+cid);
                        actionObj.put("type","open-url");
                        JSONObject obj = new JSONObject();
                        obj.put("action",actionObj);
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
                if (extraObj.has("content")){
                    String content = extraObj.getString("content");
                    intent.putExtra("content",content);
                }
                MyApplication.getInstance().setOpenNotification(true);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
