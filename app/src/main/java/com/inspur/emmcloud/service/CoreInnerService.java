package com.inspur.emmcloud.service;

/**
 * Created by Administrator on 2017/3/28.
 */

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

/**
 * 给 API >= 18 的平台上用的灰色保活手段
 */
public class CoreInnerService extends Service {

	private final static int GRAY_SERVICE_ID = 1001;
	private Handler handler;
	private Runnable runnable;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startForeground(GRAY_SERVICE_ID, new Notification());
		stopForeground(true);
		stopSelf();
		return Service.START_REDELIVER_INTENT;
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}


}
