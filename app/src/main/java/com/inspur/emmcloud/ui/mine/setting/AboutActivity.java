package com.inspur.emmcloud.ui.mine.setting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.UpgradeUtils;
import com.inspur.emmcloud.widget.dialogs.ActionSheetDialog;

/**
 * 关于页面 com.inspur.emmcloud.ui.AboutActivity
 *
 * @author Jason Chen; create at 2016年8月23日 下午2:53:14
 */
public class AboutActivity extends BaseActivity {
	private static final int NO_NEED_UPGRADE = 10;
	private static final int UPGRADE_FAIL = 11;
	private static final int DONOT_UPGRADE = 12;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		String version = AppUtils.getVersion(this).replace("beta","b");
		((TextView) findViewById(R.id.app_version_text))
				.setText(AppUtils.getAppName(this)+"  "
						+ version);

		ImageView appIconImg = (ImageView)findViewById(R.id.about_app_icon_img);
		ImageDisplayUtils.getInstance().displayImage(appIconImg,"drawable://"+AppUtils.getAppIconRes(MyApplication.getInstance()),R.drawable.ic_launcher);
		findViewById(R.id.protocol_layout).setVisibility(AppUtils.isAppVersionStandard()?View.VISIBLE:View.GONE);
		findViewById(R.id.invite_friends_layout).setVisibility(AppUtils.isAppVersionStandard()?View.VISIBLE:View.GONE);
		showSysInfo();
		handMessage();
	}

	/**
	 * 显示系统信息
	 */
	private void showSysInfo() {
		findViewById(R.id.about_app_icon_img).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				new ActionSheetDialog.ActionListSheetBuilder(AboutActivity.this)
//						.setTitle(getString(R.string.current_system)+"-->"+ (StringUtils.isBlank(enterpriseName)?getString(R.string.cluster_default):enterpriseName))
						.addItem("idm-->"+ MyApplication.getInstance().getCloudId())
//						.addItem("ecm-->"+ MyApplication.getInstance().getClusterEcm())
						.addItem("emm-->"+ MyApplication.getInstance().getClusterEmm())
						.addItem("ecm.chat-->"+MyApplication.getInstance().getClusterChat())
						.addItem("ecm.schedule-->"+MyApplication.getInstance().getClusterSchedule())
						.addItem("ecm.distribution-->"+MyApplication.getInstance().getClusterDistribution())
						.addItem("ecm.news-->"+MyApplication.getInstance().getClusterNews())
						.addItem("ecm.cloud-drive-->"+MyApplication.getInstance().getClusterCloudDrive())
						.addItem("ecm.storage.legacy-->"+MyApplication.getInstance().getClusterStorageLegacy())
						.addItem("ecm.client-registry-->"+MyApplication.getInstance().getClusterClientRegistry())
						.addItem("ClientId-->"+ PreferencesByUserAndTanentUtils.getString(AboutActivity.this, Constant.PREF_CLIENTID, ""))
//						.addItem("DeviceId-->"+ AppUtils.getMyUUID(MyApplication.getInstance()))
						.addItem("DeviceToken-->"+ AppUtils.getPushId(MyApplication.getInstance()))
						.build()
						.show();
				return false;
			}
		});
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
				UpgradeUtils upgradeUtils = new UpgradeUtils(AboutActivity.this,
						handler,true);
				upgradeUtils.checkUpdate(true);
				break;
			case R.id.invite_friends_layout:
				IntentUtils.startActivity(AboutActivity.this,RecommendAppActivity.class);
				break;
			default:
				break;
		}
	}

	private void handMessage() {
		// TODO Auto-generated method stub
		handler = new Handler() {

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
