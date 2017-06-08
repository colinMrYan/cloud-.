package com.inspur.emmcloud.ui.mdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.MDM.MDM;
import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.api.Res;

public class DeviceRegisterFailDetailActivity extends BaseActivity {
	private Bundle bundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(Res
				.getLayoutID("mdm_activity_device_register_fail_detail"));
		bundle = getIntent().getExtras().getBundle("bundle");
		String message = bundle.getString("message");
		((TextView) findViewById(Res.getWidgetID("reason_text")))
				.setText(message);
	}

	public void onClick(View v) {
		if (v.getId() == Res.getWidgetID("back_layout")) {
			onBackPressed();
		} else if (v.getId() == Res.getWidgetID("register_btn")) {
			Intent intent = new Intent();
			intent.setClass(this, ImpActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("appName", "设备注册");
			bundle.putString("function","mdm");
			bundle.putString("uri", "https://emm.inspur.com/mdm/loadForRegister?udid="+ AppUtils.getMyUUID(this));
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		new MDM().getMDMListener().MDMStatusNoPass();
	}

}
