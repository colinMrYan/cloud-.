package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.MyApplication;

/**
 * 
 * 广播接收器会接收到广播信息
 *
 */
public class ActionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		((MyApplication)context.getApplicationContext()).initPush();
	}

}
