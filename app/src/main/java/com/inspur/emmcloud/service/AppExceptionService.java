package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.inspur.emmcloud.util.UploadExceptionUtils;

/**
 * Created by Administrator on 2017/5/3.
 */

public class AppExceptionService extends Service {
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		new UploadExceptionUtils(this).upload();
		return super.onStartCommand(intent,flags,startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
