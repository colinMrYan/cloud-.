package com.inspur.emmcloud.service;

import com.inspur.emmcloud.MyApplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WebSocketService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		((MyApplication)getApplication()).startWebSocket();
		flags = START_REDELIVER_INTENT;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		((MyApplication)getApplication()).stopWebSocket();
	}


}
