package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.service.AppUpgradeService;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.IntentUtils;

/**
 * 关于页面 com.inspur.emmcloud.ui.AboutActivity
 *
 * @author Jason Chen; create at 2016年8月23日 下午2:53:14
 */
public class AboutActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		((MyApplication) getApplicationContext()).addActivity(this);
		((TextView) findViewById(R.id.app_version_text))
				.setText(getString(R.string.app_name)
						+ AppUtils.getVersion(this));
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.back_layout:
				finish();
				break;
			case R.id.newfun_layout:
				Bundle bundle = new Bundle();
				bundle.putString("from", "about");
				IntentUtils.startActivity(AboutActivity.this, GuideActivity.class,
						bundle);
				break;
			case R.id.protocol_layout:
				IntentUtils.startActivity(AboutActivity.this,
						ServiceTermActivity.class);
				break;
			case R.id.check_update_layout:
				startUpgradeServcie();
				break;
			default:
				break;
		}
	}

	private void startUpgradeServcie() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), AppUpgradeService.class);
		intent.putExtra("isManualCheck", true);
		startService(intent);

	}


}
