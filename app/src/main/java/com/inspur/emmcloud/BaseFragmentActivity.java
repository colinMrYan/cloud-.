package com.inspur.emmcloud;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.inspur.emmcloud.service.AppUpgradeService;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.StateBarColor;

public class BaseFragmentActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		StateBarColor.changeStateBarColor(this);
	}

	//修改字体方案预留
//	@Override
//	protected void attachBaseContext(Context newBase) {
//		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (!AppUtils.isAppOnForeground(getApplicationContext())) {
			// app 进入后台
			((MyApplication) getApplicationContext()).setIsActive(false);
			// 全局变量isActive = false 记录当前已经进入后台
			((MyApplication)getApplicationContext()).sendFrozenWSMsg();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (!((MyApplication) getApplicationContext()).getIsActive()) {
			((MyApplication) getApplicationContext()).setIsActive(true);
			((MyApplication)getApplicationContext()).clearNotification();
			if (((MyApplication) getApplicationContext())
					.isIndexActivityRunning()) {
				((MyApplication)getApplicationContext()).sendActivedWSMsg();
				startUpgradeServcie();
			}
		}


	}
	
	@Override  
	public Resources getResources() {  
	    Resources res = super.getResources();    
	    Configuration config=new Configuration();    
	    config.setToDefaults();    
	    res.updateConfiguration(config,res.getDisplayMetrics() );  
	    return res;  
	}

	private void startUpgradeServcie(){
		LogUtils.jasonDebug("startUpgradeServcie--------");
		Intent intent = new Intent();
		intent.setClass(this, AppUpgradeService.class);
		startService(intent);
	}

}
