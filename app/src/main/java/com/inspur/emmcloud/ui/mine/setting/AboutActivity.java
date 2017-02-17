package com.inspur.emmcloud.ui.mine.setting;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UpgradeUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 关于页面 com.inspur.emmcloud.ui.AboutActivity
 * 
 * @author Jason Chen; create at 2016年8月23日 下午2:53:14
 */
public class AboutActivity extends BaseActivity {
	private static final int NO_NEED_UPGRADE = 10;
	private static final int UPGRADE_FAIL = 11;
	private static final int DONOT_UPGRADE = 12;
	private Handler hander;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		((MyApplication) getApplicationContext()).addActivity(this);
		((TextView) findViewById(R.id.app_version_text))
				.setText(getString(R.string.app_name)
						+ AppUtils.getVersion(this));
		handMessage();
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;
		// case R.id.welcome_layout:
		// ToastUtils.show(getApplicationContext(),
		// R.string.function_not_implemented);
		// break;
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
			UpgradeUtils upgradeUtils = new UpgradeUtils(AboutActivity.this,
					hander);
			upgradeUtils.checkUpdate(false);
			break;
		default:
			break;
		}
	}

	private void handMessage() {
		// TODO Auto-generated method stub
		hander = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case NO_NEED_UPGRADE:
					ToastUtils.show(getApplicationContext(), R.string.app_is_lastest_version);
					break;
				case UPGRADE_FAIL:
					ToastUtils.show(getApplicationContext(), R.string.check_update_fail);
					break;
				case DONOT_UPGRADE:
					break;
				default:
					break;
				}
			}

		};
	}

}
