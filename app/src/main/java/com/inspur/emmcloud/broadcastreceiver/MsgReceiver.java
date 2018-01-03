package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.inspur.emmcloud.bean.chat.Msg;

import org.json.JSONObject;

public class MsgReceiver extends BroadcastReceiver{

	private static final String ACTION_NAME = "com.inspur.msg";
	private Handler handler;
	public static MsgReceiver instance;

	public MsgReceiver(){

	}

	public MsgReceiver(Context context,Handler handler) {
		this.handler = handler;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(intent.getStringExtra("push"));
			Msg pushMsg = new Msg(jsonObject);
			Message msg = new Message();
			msg.what = 1;
			msg.obj = pushMsg;
			handler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
