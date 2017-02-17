package com.inspur.emmcloud.broadcastreceiver;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.MsgCacheUtil;
import com.inspur.emmcloud.util.ToastUtils;

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
		// TODO Auto-generated method stub
		String action = intent.getAction(); 
		if(action.equals(ACTION_NAME)){
			LogUtils.debug("yfcLog", intent.getStringExtra("push"));
		}
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(intent.getStringExtra("push"));
			Msg pushMsg = new Msg(jsonObject);
			Message msg = new Message();
			msg.what = 1;
			msg.obj = pushMsg;
			handler.sendMessage(msg);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
