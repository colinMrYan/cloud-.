package com.inspur.emmcloud;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.StateBarColor;

public class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		StateBarColor.changeStateBarColor(this);

	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (!AppUtils.isAppOnForeground(getApplicationContext())) {
			// app 进入后台
			((MyApplication) getApplicationContext()).setIsActive(false);
			// 全局变量isActive = false 记录当前已经进入后台
			((MyApplication)getApplicationContext()).sendFrozenWSMsg();
			((MyApplication)getApplicationContext()).clearNotification();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (!((MyApplication) getApplicationContext()).getIsActive()) {
			((MyApplication) getApplicationContext()).setIsActive(true);
			if (((MyApplication) getApplicationContext())
					.isIndexActivityRunning()) {
				((MyApplication)getApplicationContext()).sendActivedWSMsg();
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


	//修改本地字体方案预留
//	@Override
//	protected void attachBaseContext(Context newBase) {
//		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//
//	}


}
