package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.alibaba.fastjson.JSONObject;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PVCollectModelCacheUtils;

public class PVCollectService extends Service {
	
	private Handler handler;
	private Runnable runnable;
	private AppAPIService apiService;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		init();
		handler.post(runnable);
		return START_REDELIVER_INTENT;
	}
	
	private void init() {
		// TODO Auto-generated method stub
		if (handler == null) {
			handler = new Handler();
		}else {
			handler.removeCallbacks(runnable);
		}
		if (apiService == null) {
			apiService = new AppAPIService(getApplicationContext());
			apiService.setAPIInterface(new WebService());
		}
		if (runnable == null) {
			runnable = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
						String collectInfo = PVCollectModelCacheUtils.getCollectModelListJson(getApplicationContext());
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("userContent",collectInfo);
						jsonObject.put("","");
						if (collectInfo != null) {
							apiService.uploadPVCollect(collectInfo);
							return;
						}
					}
					continueToRun();
				}
			};
		}
	}

	private void continueToRun(){
		handler.postDelayed(runnable, 1800000);
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (handler != null) {
			handler.removeCallbacks(runnable);
			handler = null;
		}
		super.onDestroy();
	}
	
	private class WebService extends APIInterfaceInstance{

		@Override
		public void returnUploadCollectSuccess() {
			// TODO Auto-generated method stub
			PVCollectModelCacheUtils.deleteAllCollectModel(getApplicationContext());
			continueToRun();
		}

		@Override
		public void returnUploadCollectFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			 continueToRun();
		}
		
	}
}
