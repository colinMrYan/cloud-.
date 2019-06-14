package com.inspur.emmcloud.web.plugin.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by chenmch on 2017/7/19.
 */

public class BroadcastService extends ImpPlugin {
    private BroadcastReceiver receiver;
    private boolean isRunInBackgroud = false;
    private JSONObject paramsObject;


    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("open".equals(action)) {
            sendBroadcast(paramsObject);
        } else if ("receive".equals(action)) {
            this.paramsObject = paramsObject;
            registerReceiver(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    /**
     * 发送广播
     *
     * @param paramsObject
     */
    private void sendBroadcast(JSONObject paramsObject) {
        String action = JSONUtils.getString(paramsObject, "action", "");
        JSONArray extraArray = JSONUtils.getJSONArray(paramsObject, "extra", new JSONArray());
        Intent intent = new Intent(action);
        for (int i = 0; i < extraArray.length(); i++) {
            JSONObject object = JSONUtils.getJSONObject(extraArray, i, new JSONObject());
            String extraName = JSONUtils.getString(object, "name", null);
            String extraValue = JSONUtils.getString(object, "value", null);
            if (!StringUtils.isBlank(extraName) && !StringUtils.isBlank(extraValue)) {
                intent.putExtra(extraName, extraValue);
            }
        }
        getFragmentContext().sendBroadcast(intent);

    }

    /**
     * 注册接收广播
     *
     * @param paramsObject
     */
    private void registerReceiver(JSONObject paramsObject) {
        String action = JSONUtils.getString(paramsObject, "action", "");
        isRunInBackgroud = JSONUtils.getBoolean(paramsObject, "isRunInBackgroud", false);
        final String callback = JSONUtils.getString(paramsObject, "callback", null);
        if (StringUtils.isBlank(callback) && StringUtils.isBlank(action)) {
            return;
        }
        if (receiver != null) {
            getFragmentContext().unregisterReceiver(receiver);
            receiver = null;
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                JSONObject object = new JSONObject();
                for (String key : bundle.keySet()) {
                    try {
                        object.put(key, bundle.get(key));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                String result = object.toString();
                LogUtils.YfcDebug("result=" + result);
                BroadcastService.this.jsCallback(callback, result);
                getFragmentContext().unregisterReceiver(receiver);
                receiver = null;

            }
        };
        IntentFilter filter = new IntentFilter(action);
        getFragmentContext().registerReceiver(receiver, filter);

    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    @Override
    public void onActivityResume() {
        if (receiver != null && !isRunInBackgroud && paramsObject != null) {
            registerReceiver(paramsObject);
        }
    }

    @Override
    public void onActivityPause() {
        if (receiver != null && !isRunInBackgroud) {
            getFragmentContext().unregisterReceiver(receiver);
        }

    }

    @Override
    public void onDestroy() {
        if (receiver != null) {
            getFragmentContext().unregisterReceiver(receiver);
            receiver = null;
        }
    }
}

