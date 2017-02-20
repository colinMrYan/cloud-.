package com.inspur.emmcloud.service;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.CollectModel;
import com.inspur.emmcloud.util.CollectModelCacheUtils;
import com.inspur.emmcloud.util.NetUtils;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class CollectService extends Service {
	
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
		flags = START_REDELIVER_INTENT;
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void init() {
		// TODO Auto-generated method stub
		if (handler != null) {
			handler.removeCallbacks(runnable);
			handler = null;
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
						List<CollectModel> collectModelList = CollectModelCacheUtils.getCollectModelList(getApplicationContext());
						if (collectModelList.size()>0) {
							String collectInfo = JSON.toJSONString(collectModelList);
							apiService.uploadCollect(collectInfo);
							return;
						}
					}
					continueToRun();
				}
			};
		}
	}

	private void continueToRun(){
		handler.postDelayed(runnable, 30000);
	}

	
	private class WebService extends APIInterfaceInstance{

		@Override
		public void returnUploadCollectSuccess() {
			// TODO Auto-generated method stub
			CollectModelCacheUtils.deleteAllCollectModel(getApplicationContext());
			continueToRun();
		}

		@Override
		public void returnUploadCollectFail() {
			// TODO Auto-generated method stub
			 continueToRun();
		}
		
	}
}
