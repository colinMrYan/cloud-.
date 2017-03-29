package com.inspur.emmcloud.ui.mine.setting;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.bean.GetSignoutResult;
import com.inspur.emmcloud.bean.Language;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.DataCleanManager;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import cn.jpush.android.api.JPushInterface;

public class SettingActivity extends BaseActivity {

	private LoadingDialog loadingDlg;
	private static final int DATA_CLEAR_SUCCESS = 0;
	private Handler handler;
	private TextView languageText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.activity_setting);
		((MyApplication) getApplicationContext()).addActivity(this);

		loadingDlg = new LoadingDialog(this);
		languageText = (TextView) findViewById(R.id.msg_languagechg_result_text);
		setLanguage();
		handMessage();

	}

	/**
	 * 设置显示app语言
	 */
	private void setLanguage() {
		// TODO Auto-generated method stub
		String languageName = PreferencesUtils.getString(
				getApplicationContext(), UriUtils.tanent+"language", "");
		String languageJson = PreferencesUtils
				.getString(this, UriUtils.tanent+"appLanguageObj");
		if (languageJson != null && !languageName.equals("followSys")) {
			Language language = new Language(languageJson);
			languageText.setText(new Language(languageJson).getLabel());
			setLanguageFlagImg(language);
		} else {
			languageText.setText(getString(R.string.follow_system));
		}
	}

	/**
	 * 语言设置国旗
	 *
	 * @param language
	 */
	private void setLanguageFlagImg(Language language) {
		// TODO Auto-generated method stub
		String iso = language.getIso();
		iso = iso.replace("-", "_");
		iso = iso.toLowerCase();
		int id = getResources().getIdentifier(iso, "drawable", getApplicationContext().getPackageName());
		((ImageView)findViewById(R.id.msg_language_flag_img)).setImageResource(id);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void handMessage() {
		// TODO Auto-generated method stub
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				ToastUtils.show(getApplicationContext(),
						R.string.data_clear_success);
				// 通知消息页面重新创建群组头像
				Intent intent = new Intent("message_notify");
				intent.putExtra("command", "creat_group_icon");
				sendBroadcast(intent);

			}

		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		// 屏蔽语言设置
		// if (resultCode == RESULT_OK && data.hasExtra("position")) {
		// int languageIndex = data.getExtras().getInt("position");
		// ((TextView) findViewById(R.id.msg_languagechg_result_text))
		// .setText(PreferencesUtils.getString(getApplicationContext(),
		// "nowlanguage","跟随系统"));
		// }

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.back_layout:

			finish();
			break;
		case R.id.signout_layout:
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if (which == -1) {
						signout();
					}
				}
			};
			EasyDialog.showDialog(SettingActivity.this,
					getString(R.string.prompt),
					getString(R.string.if_confirm_signout),
					getString(R.string.ok), getString(R.string.cancel),
					dialogClickListener, true);

			break;
		case R.id.account_security_layout:
			// ToastUtils.show(getApplicationContext(),
			// R.string.function_not_implemented);
			// intent.setClass(SettingActivity.this,
			// AccountSecurityActivity.class);
			// startActivity(intent);
			break;
		case R.id.msg_notify_layout:
			// ToastUtils.show(getApplicationContext(),
			// R.string.function_not_implemented);
			break;
		case R.id.about_layout:
			intent.setClass(SettingActivity.this, AboutActivity.class);
			startActivity(intent);
			break;

		case R.id.msg_languagechg_layout:
			// intent.setClass(SettingActivity.this,
			// LanguageChangeActivity.class);
			// startActivityForResult(intent, LANGUAGE_OK);
			IntentUtils.startActivity(SettingActivity.this,
					LanguageChangeActivity.class);

			// Resources resources =getResources();
			// DisplayMetrics dm =resources.getDisplayMetrics();
			// Configuration config =resources.getConfiguration();
			// // 应用用户选择语言
			// config.locale = Locale.US;
			// resources.updateConfiguration(config, dm);
			//
			// Intent intentLog = new Intent(this, IndexActivity.class);
			//
			// intentLog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
			// | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			//
			// startActivity(intentLog);
			break;
		case R.id.clear_cache_layout:

			showClearCacheDlg();
			break;
		default:
			break;
		}
	}

	/**
	 * 弹出清除缓存选项提示框
	 */
	private void showClearCacheDlg() {
		// TODO Auto-generated method stub
		float radio = 0.900f;
		final MyDialog clearCacheDlg = new MyDialog(SettingActivity.this,
				R.layout.dialog_four_item, R.style.userhead_dialog_bg, radio);
		TextView clearImgAndFileText = (TextView) clearCacheDlg
				.findViewById(R.id.text1);
		// String imgAndFileSize = DataCleanManager
		// .getAllCacheSize(SettingActivity.this);
		clearImgAndFileText
				.setText(getString(R.string.settings_clean_imgae_attachment));
		TextView clearWebCacheText = (TextView) clearCacheDlg
				.findViewById(R.id.text2);
		clearWebCacheText.setText(getString(R.string.settings_clean_web));
		TextView clearAllCacheText = (TextView) clearCacheDlg
				.findViewById(R.id.text3);
		clearAllCacheText.setText(getString(R.string.settings_clean_all));
		TextView cancelText = (TextView) clearCacheDlg.findViewById(R.id.text4);
		cancelText.setText(getString(R.string.button_cancel));
		clearImgAndFileText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						String msgCachePath = Environment
								.getExternalStorageDirectory()
								+ "/IMP-Cloud/download/";
						String imgCachePath = MyAppConfig.LOCAL_CACHE_PATH;
						DataCleanManager.cleanApplicationData(
								SettingActivity.this, msgCachePath,
								imgCachePath);
						ImageDisplayUtils imageDisplayUtils = new ImageDisplayUtils(
								getApplicationContext(),
								R.drawable.icon_photo_default);
						imageDisplayUtils.clearCache();
						handler.sendEmptyMessage(DATA_CLEAR_SUCCESS);
					}
				}).start();
				clearCacheDlg.dismiss();
			}
		});
		clearWebCacheText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearCacheDlg.dismiss();
				DataCleanManager.cleanWebViewCache(SettingActivity.this);
				ToastUtils.show(getApplicationContext(),
						R.string.data_clear_success);
			}
		});
		clearAllCacheText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearCacheDlg.dismiss();
				showClearCacheWarningDlg();
			}

		});
		cancelText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearCacheDlg.dismiss();
			}
		});
		clearCacheDlg.show();
	}

	/**
	 * 弹出清除全部缓存提示框
	 */
	private void showClearCacheWarningDlg() {
		// TODO Auto-generated method stub
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (which == -1) {
					DataCleanManager.cleanWebViewCache(SettingActivity.this);
					((MyApplication) getApplicationContext()).deleteAllDb();
					String msgCachePath = Environment
							.getExternalStorageDirectory()
							+ "/IMP-Cloud/download/";
					String imgCachePath = MyAppConfig.LOCAL_CACHE_PATH;
					DataCleanManager.cleanApplicationData(SettingActivity.this,
							msgCachePath, imgCachePath);
					ImageDisplayUtils imageDisplayUtils = new ImageDisplayUtils(
							getApplicationContext(),
							R.drawable.icon_photo_default);
					imageDisplayUtils.clearCache();
					ToastUtils.show(getApplicationContext(),
							R.string.data_clear_success);
					((MyApplication) getApplicationContext()).exit();
					Intent intentLog = new Intent(SettingActivity.this,
							IndexActivity.class);
					intentLog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intentLog);
				}
			}
		};
		EasyDialog.showDialog(SettingActivity.this, getString(R.string.prompt),
				getString(R.string.my_setting_tips_quit),
				getString(R.string.ok), getString(R.string.cancel),
				dialogClickListener, true);
	}

	private void signout() {
		// TODO Auto-generated method stub
		if (((MyApplication) getApplicationContext()).getWebSocketPush() != null) {
			((MyApplication) getApplicationContext()).getWebSocketPush()
			.webSocketSignout();
		}
		NotificationManager nm =(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();
		JPushInterface.stopPush(getApplicationContext());
		PreferencesUtils.putString(SettingActivity.this, "tokenType", "");
		PreferencesUtils.putString(SettingActivity.this, "accessToken", "");
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		this.finish();
	}

	private class Webservice extends APIInterfaceInstance {

		@Override
		public void returnSignoutSuccess(GetSignoutResult getSignoutResult) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			signout();
		}

		@Override
		public void returnSignoutFail(String error) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			signout();
			WebServiceMiddleUtils.hand(SettingActivity.this, error);
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (handler != null) {
			handler = null;
		}
	}

}
