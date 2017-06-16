package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.GetUpgradeResult;
import com.inspur.emmcloud.ui.app.AppUpgradeNotifyActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ToastUtils;

/**
 * Created by Administrator on 2017/5/10.
 */

public class AppUpgradeService extends Service{
	private static  final  int notUpdateInterval = 86400000;
	private Handler handler;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//是否人工检查是否有新版本，此时如果有更新一定要提示
		if (NetUtils.isNetworkConnected(this, false) && intent != null) {
			boolean isManualCheck = intent.getBooleanExtra("isManualCheck",false);
			handler = new Handler(Looper.getMainLooper());
			AppAPIService apiService = new AppAPIService(this);
			apiService.setAPIInterface(new Webservice());
			apiService.checkUpgrade(isManualCheck);
		}
		return super.onStartCommand(intent,flags,startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private class Webservice extends APIInterfaceInstance{
		@Override
		public void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult,boolean isManualCheck) {
			// TODO Auto-generated method stub
			if (AppUtils.isAppOnForeground(AppUpgradeService.this)) {
				handleUpgrade(getUpgradeResult,isManualCheck);
			}
		}

		@Override
		public void returnUpgradeFail(String error,boolean isManualCheck,int errorCode) {
			// TODO Auto-generated method stub
			if (isManualCheck){
				handler.post(new Runnable(){
					public void run(){
						ToastUtils.show(getApplicationContext(), R.string.check_update_fail);
					}
				});
			}
			onDestroy();
		}

	}

	private void handleUpgrade(GetUpgradeResult getUpgradeResult,boolean isManualCheck) {
		// TODO Auto-generated method stub
		int upgradeCode = getUpgradeResult.getUpgradeCode();
		long appNotUpdateTime = PreferencesUtils.getLong(getApplicationContext(),"appNotUpdateTime");
		LogUtils.jasonDebug("appNotUpdateTime="+appNotUpdateTime);
		if (isManualCheck && upgradeCode == 0){
			handler.post(new Runnable(){
				public void run(){
					ToastUtils.show(getApplicationContext(), R.string.app_is_lastest_version);
				}
			});
		}

		if (upgradeCode != 0 && (isManualCheck || appNotUpdateTime== -1 || (System.currentTimeMillis()-appNotUpdateTime>notUpdateInterval))){
			Intent intent = new Intent(this, AppUpgradeNotifyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("getUpgradeResult",getUpgradeResult);
			AppUpgradeService.this.startActivity(intent);
		}
		onDestroy();
	}
}
