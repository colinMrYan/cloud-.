package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import org.json.JSONObject;

public class MsgReceiver extends BroadcastReceiver {

    public static MsgReceiver instance;
    //private static final String ACTION_NAME = "com.inspur.msg";
    private Handler handler;

    public MsgReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(intent.getStringExtra("push"));
            Message msg = new Message();
            msg.what = 1;
            msg.obj = jsonObject;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
