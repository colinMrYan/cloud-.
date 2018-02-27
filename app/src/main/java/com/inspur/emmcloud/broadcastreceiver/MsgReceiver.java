package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.bean.chat.MsgRobot;
import com.inspur.emmcloud.util.common.JSONUtils;

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
			Message msg = new Message();
			jsonObject = new JSONObject(intent.getStringExtra("push"));
			String messageVersion = JSONUtils.getString(jsonObject,"message","0");
			if (messageVersion.equals("1.0")){
				MsgRobot pushMsg = new MsgRobot(jsonObject);
				msg.obj = pushMsg;
				msg.arg1 = 1;
			}else {
				Msg pushMsg = new Msg(jsonObject);
				msg.obj = pushMsg;
				msg.arg1 = 0;
			}
			msg.what = 1;
			handler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
