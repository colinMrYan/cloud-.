package com.inspur.emmcloud;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.inspur.emmcloud.bean.SplashPageBean;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.service.AppExceptionService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.mine.setting.GuideActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LanguageUtils;
import com.inspur.emmcloud.util.LoginUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ResolutionUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UpgradeUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifImageView;


/**
 * 应用启动Activity
 *
 * @author Administrator
 */
public class MainActivity extends Activity { // 此处不能继承BaseActivity 推送会有问题

	private static final int LOGIN_SUCCESS = 0;
	private static final int LOGIN_FAIL = 1;
	private static final int GET_LANGUAGE_SUCCESS = 3;
	private static final int NO_NEED_UPGRADE = 10;
	private static final int UPGRADE_FAIL = 11;
	private static final int DONOT_UPGRADE = 12;
	private static final long SPLASH_PAGE_TIME = 2500;
	private Handler handler;
	private LanguageUtils languageUtils;
	private long activitySplashShowTime = 0;
	private Timer timer;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StateBarColor.changeStateBarColor(this);
		setContentView(R.layout.activity_main);
		init();
	}


	/**
	 * ea
	 * 初始化
	 */
	private void init() {
				/* 解决了在sd卡中第一次安装应用，进入到主页并切换到后台再打开会重新启动应用的bug */
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			finish();
			return;
		}
		activitySplashShowTime = System.currentTimeMillis();
		//进行app异常上传
		startUploadExceptionService();
		((MyApplication) getApplicationContext()).addActivity(this);
		// 检测分辨率、网络环境
		if (!ResolutionUtils.isFitResolution(MainActivity.this)) {
			showResolutionDialog();
		} else {
			initEnvironment();
		}

		showLastSplash();
	}

	/**
	 * 启动异常上传服务
	 */
	private void startUploadExceptionService() {
		Intent intent = new Intent();
		intent.setClass(this, AppExceptionService.class);
		startService(intent);
	}

//    /**
//     * 启动app版本升级检查服务
//     */
//    private void startUpgradeServcie() {
//        Intent intent = new Intent();
//        intent.setClass(getApplicationContext(), AppUpgradeService.class);
//        startService(intent);
//
//    }


	/**
	 * 显示分辨率不符合条件的提示框
	 **/
	private void showResolutionDialog() {
		// TODO Auto-generated method stub
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				finish();
			}
		};
		EasyDialog.showDialog(this, getString(R.string.prompt),
				getString(R.string.resolution_valiad), getString(R.string.ok),
				listener, false);
	}

	/**
	 * 初始化应用环境
	 */
	private void initEnvironment() {
		// TODO Auto-generated method stub
		Boolean isFirst = PreferencesUtils.getBoolean(MainActivity.this,
				"isFirst", true);
		// 当第一次进入应用，系统没有自动创建快捷方式时进行创建
		if (isFirst && !AppUtils.isHasShortCut(MainActivity.this)) {
			((MyApplication) getApplicationContext())
					.addShortCut(MainActivity.this);
		}
		handMessage();
		UpgradeUtils upgradeUtils = new UpgradeUtils(MainActivity.this,
				handler, false);
		upgradeUtils.checkUpdate(false);
//        getServerLanguage();
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.splash_skip_layout:
			case R.id.splash_skip_btn:
				if (timer != null) {
					timer.cancel();
					startApp();
				}
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
					case UPGRADE_FAIL:
					case NO_NEED_UPGRADE:
					case DONOT_UPGRADE:
						getServerLanguage();
						break;
					case LOGIN_SUCCESS:
					case LOGIN_FAIL:
					case GET_LANGUAGE_SUCCESS:
						setSplashShow();
						break;
					default:
						break;
				}
			}

		};
	}

	@Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(0, 0);
	}

	/**
	 * 显示跳过按钮
	 */
	public void showSkipButton() {
		(findViewById(R.id.splash_skip_btn)).setVisibility(View.VISIBLE);
		(findViewById(R.id.splash_skip_layout)).setVisibility(View.VISIBLE);
	}

	/**
	 * 获取服务端支持的语言
	 */
	private void getServerLanguage() {
		// TODO Auto-generated method stub
		String accessToken = PreferencesUtils.getString(MainActivity.this,
				"accessToken", "");
		String myInfo = PreferencesUtils.getString(getApplicationContext(),
				"myInfo", "");
		String languageJson = PreferencesUtils.getString(getApplicationContext(),
				UriUtils.tanent + "appLanguageObj");
		if (!StringUtils.isBlank(accessToken) && StringUtils.isBlank(myInfo)) {
			new LoginUtils(MainActivity.this, handler).getMyInfo();
		} else if (!StringUtils.isBlank(accessToken) && !StringUtils.isBlank(myInfo) && StringUtils.isBlank(languageJson)) {
			languageUtils = new LanguageUtils(MainActivity.this, handler);
			languageUtils.getServerSupportLanguage();
		} else {
			setSplashShow();
		}
	}


	/**
	 * 进入App
	 */
	private void setSplashShow() {
		// TODO Auto-generated method stub
		long betweenTime = System.currentTimeMillis() - activitySplashShowTime;
		long leftTime = SPLASH_PAGE_TIME - betweenTime;
		TimerTask task = new TimerTask() {
			public void run() {
				startApp();
			}
		};
		if (checkIfShowSplashPage() && (leftTime > 0)) {
			showSkipButton();
			timer = new Timer();
			timer.schedule(task, leftTime);
		} else {
			startApp();
		}
	}

	/**
	 * 开启应用
	 */
	private void startApp() {
		Boolean isFirst = PreferencesUtils.getBoolean(
				MainActivity.this, "isFirst", true);
		if (checkIfUpgraded() || isFirst) {
			IntentUtils.startActivity(MainActivity.this,
					GuideActivity.class, true);
		} else {
			String accessToken = PreferencesUtils.getString(MainActivity.this,
					"accessToken", "");
			if (!StringUtils.isBlank(accessToken)) {
				IntentUtils.startActivity(MainActivity.this, IndexActivity.class,
						true);
			}else {
				IntentUtils.startActivity(MainActivity.this, LoginActivity.class,
						true);
			}
		}
	}

	/**
	 * 检查是否有可以展示的图片
	 *
	 * @return
	 */
	private boolean checkIfShowSplashPage() {
		boolean flag = false;
		String splashInfo = PreferencesByUserAndTanentUtils.getString(MainActivity.this, "splash_page_info");
		if (!StringUtils.isBlank(splashInfo)) {
			SplashPageBean splashPageBeanLoacal = new SplashPageBean(splashInfo);
			SplashPageBean.PayloadBean.ResourceBean.DefaultBean defaultBean = splashPageBeanLoacal.getPayload()
					.getResource().getDefaultX();
			String splashImgPath = getSplashPagePath(defaultBean);
			long startTime = splashPageBeanLoacal.getPayload().getEffectiveDate();
			long endTime = splashPageBeanLoacal.getPayload().getExpireDate();
			long nowTime = System.currentTimeMillis();
			flag = FileUtils.isFileExist(splashImgPath) &&
					((nowTime > startTime) && (nowTime < endTime));
		}
		return flag;
	}


	/**
	 * 检测是否应用版本是否进行了升级
	 *
	 * @return
	 */
	private boolean checkIfUpgraded() {
		boolean ifUpgraded = false;
		String savedVersion = PreferencesUtils.getString(MainActivity.this,
				"previousVersion", "");
		String currentVersion = AppUtils.getVersion(MainActivity.this);
		if (TextUtils.isEmpty(savedVersion)) {
			ifUpgraded = false;
		} else {
			ifUpgraded = AppUtils
					.isAppHasUpgraded(savedVersion, currentVersion);
		}
		return ifUpgraded;
	}


	@Override
	public Resources getResources() {
		Resources res = super.getResources();
		Configuration config = new Configuration();
		config.setToDefaults();
		res.updateConfiguration(config, res.getDisplayMetrics());
		return res;
	}

	/**
	 * 展示最新splash   需要添加是否已过期的逻辑
	 */
	private void showLastSplash() {
		String splashInfo = PreferencesByUserAndTanentUtils.getString(MainActivity.this, "splash_page_info");
		if (!StringUtils.isBlank(splashInfo)) {
			SplashPageBean splashPageBeanLoacal = new SplashPageBean(splashInfo);
			SplashPageBean.PayloadBean.ResourceBean.DefaultBean defaultBean = splashPageBeanLoacal.getPayload()
					.getResource().getDefaultX();
			String splashPagePath = getSplashPagePath(defaultBean);
			long nowTime = System.currentTimeMillis();
			boolean shouldShow = ((nowTime > splashPageBeanLoacal.getPayload().getEffectiveDate())
					&& (nowTime < splashPageBeanLoacal.getPayload().getExpireDate()));
			if (shouldShow && !StringUtils.isBlank(splashPagePath)) {
				ImageLoader.getInstance().displayImage("file://" + splashPagePath, (GifImageView) findViewById(R.id.splash_img_top));
			} else {
				((GifImageView) findViewById(R.id.splash_img_top)).setVisibility(View.GONE);
			}
		}
	}


	/**
	 * 闪屏文件路径
	 *
	 * @param defaultBean
	 * @return
	 */
	private String getSplashPagePath(SplashPageBean.PayloadBean.ResourceBean.DefaultBean defaultBean) {
		String screenType = AppUtils.getScreenType(MainActivity.this);
		String name = "";
		if (screenType.equals("2k")) {
			name = MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
					((MyApplication) getApplication()).getUid(), "splash/" + defaultBean.getXxxhdpi());
		} else if (screenType.equals("xxxhdpi")) {
			name = MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
					((MyApplication) getApplication()).getUid(), "splash/" + defaultBean.getXxhdpi());
		} else if (screenType.equals("xxhdpi")) {
			name = MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
					((MyApplication) getApplication()).getUid(), "splash/" + defaultBean.getXhdpi());
		} else {
			name = MyAppConfig.getSplashPageImageShowPath(MainActivity.this,
					((MyApplication) getApplication()).getUid(), "splash/" + defaultBean.getHdpi());
		}
		return name;
	}
}
