package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.inspur.emmcloud.MyApplication;

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
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		((MyApplication)getApplication()).stopWebSocket();
	}


}
