package com.inspur.emmcloud.ui.mine.setting;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;

/**
 * 服务条款页面
 * @author Administrator
 *
 */
public class ServiceTermActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service_term);
		((MyApplication)getApplicationContext()).addActivity(ServiceTermActivity.this);
	}
	
	public void onBack(View v){
		finish();
	}
}
