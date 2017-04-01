package com.inspur.emmcloud.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/**
 * Created by Administrator on 2017/3/28.
 */

public class CoreService extends Service{

	private final static int GRAY_SERVICE_ID = 1001;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Build.VERSION.SDK_INT < 18) {
			startForeground(GRAY_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
		} else {
			Intent innerIntent = new Intent(this, CoreInnerService.class);
			startService(innerIntent);
			startForeground(GRAY_SERVICE_ID, new Notification());
		}
		return Service.START_REDELIVER_INTENT;
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}



}
