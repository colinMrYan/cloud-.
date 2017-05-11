package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.GetUpgradeResult;
import com.inspur.emmcloud.ui.app.AppUpgradeNotifyActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;

/**
 * Created by Administrator on 2017/5/10.
 */

public class AppUpgradeService extends Service{
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtils.jasonDebug("AppUpgradeService-------onStartCommand");
		if (NetUtils.isNetworkConnected(this, false)) {
			AppAPIService apiService = new AppAPIService(this);
			apiService.setAPIInterface(new Webservice());
			apiService.checkUpgrade();
		}
		return super.onStartCommand(intent,flags,startId);
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private class Webservice extends APIInterfaceInstance{
		@Override
		public void returnUpgradeSuccess(GetUpgradeResult getUpgradeResult) {
			// TODO Auto-generated method stub
			if (AppUtils.isAppOnForeground(AppUpgradeService.this)) {
				handleUpgrade(getUpgradeResult);
			}
		}

		@Override
		public void returnUpgradeFail(String error) {
			// TODO Auto-generated method stub
			onDestroy();
		}

	}

	private void handleUpgrade(GetUpgradeResult getUpgradeResult) {
		// TODO Auto-generated method stub
		int upgradeCode = getUpgradeResult.getUpgradeCode();
		if (upgradeCode != 0){
			Intent intent = new Intent(this, AppUpgradeNotifyActivity.class);
			intent.putExtra("getUpgradeResult",getUpgradeResult);
			AppUpgradeService.this.startActivity(intent);
		}
		onDestroy();
	}
}
