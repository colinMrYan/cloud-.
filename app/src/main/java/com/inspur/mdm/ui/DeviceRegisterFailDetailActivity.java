package com.inspur.mdm.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.mdm.MDM;
import com.inspur.mdm.utils.MDMResUtils;

public class DeviceRegisterFailDetailActivity extends MDMBaseActivity {
	private Bundle bundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(MDMResUtils
				.getLayoutID("mdm_activity_device_register_fail_detail"));
		bundle = getIntent().getExtras().getBundle("bundle");
		String message = bundle.getString("message");
		((TextView) findViewById(MDMResUtils.getWidgetID("reason_text")))
				.setText(message);
	}

	public void onClick(View v) {
		Intent intent = new Intent();
		if (v.getId() == MDMResUtils.getWidgetID("back_layout")) {
			onBackPressed();
		} else if (v.getId() == MDMResUtils.getWidgetID("register_btn")) {
			intent.setClass(getApplicationContext(),
					DeviceRegisterActivity.class);
			intent.putExtra("bundle", bundle);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		new MDM().getMDMListener().MDMStatusNoPass();
	}

}
