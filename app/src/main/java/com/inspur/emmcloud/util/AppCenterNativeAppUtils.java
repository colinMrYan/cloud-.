package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.EditText;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.MyDialog;

import org.xutils.common.Callback;

import java.io.File;


/**
 * Created by Administrator on 2017/4/21.
 */

public class AppCenterNativeAppUtils {

	private Activity context;
	private App app;
	private LoadingDialog loadingDlg;
	private MyDialog passwordInputDlg;

	public void InstallOrOpen(Activity context, App app) {
		this.context = context;
		this.app = app;
		if (AppUtils.isAppInstalled(context, app.getPackageName())) {
			if (app.getPackageName().equals("cn.knowhowsoft.khmap5")) {
				getApprovalPassword();
			} else {
				openNativeApp();
			}
		} else {
			installApp();
		}
	}

	/**
	 * 打开其他第三方原生应用
	 */
	public void openNativeApp() {
		try {
			Intent intent;
			String packageName = app.getPackageName();
			String mainActivityName = app.getMainActivityName();
			LogUtils.jasonDebug("packageName=" + packageName);
			LogUtils.jasonDebug("mainActivityName=" + mainActivityName);
			if (!StringUtils.isBlank(packageName) && !StringUtils.isBlank(mainActivityName)) {
				intent = new Intent(Intent.ACTION_MAIN);
				ComponentName cn = new ComponentName(packageName, mainActivityName);
				intent.setComponent(cn);
				String myInfo = PreferencesUtils.getString(context, "myInfo");
				GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
				intent.putExtra("USERNAME", getMyInfoResult.getCode());
				String password = PreferencesByUserAndTanentUtils.getString(context, "approvalPassword");
				intent.putExtra("PASSWORD", password);
				LogUtils.jasonDebug("USERNAME=" + getMyInfoResult.getCode());
				LogUtils.jasonDebug("PASSWORD=" + password);
			} else {
				PackageManager packageManager = context.getPackageManager();
				intent = packageManager.getLaunchIntentForPackage(packageName);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			ToastUtils.show(context, "应用未安装");
		}

	}

	/**
	 * 获取审批的密码
	 */
	private void getApprovalPassword() {
		String password = PreferencesByUserAndTanentUtils.getString(context, "approvalPassword");
		if (StringUtils.isBlank(password)) {
			showPasswordInputDlg();
		} else {
			veriryPassword(password);
		}
	}

	/**
	 * 弹出密码输入框
	 */
	private void showPasswordInputDlg() {
		passwordInputDlg = new MyDialog(context,
				R.layout.dialog_my_app_approval_password_input);
		passwordInputDlg.setCancelable(false);
		final EditText inputEdit = (EditText) passwordInputDlg.findViewById(R.id.edit);
		(passwordInputDlg.findViewById(R.id.ok_btn)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String password = inputEdit.getText().toString();
				if (StringUtils.isBlank(password)) {
					ToastUtils.show(context, R.string.please_input_password);
				}else if(password.length()<6){
					ToastUtils.show(context, R.string.modify_input_password);
				}else{
					try {
						String encodePassword = EncryptUtils.encodeApprovalPassword(password);
						veriryPassword(encodePassword);
					} catch (Exception e) {
						e.printStackTrace();
						ToastUtils.show(context, R.string.app_password_encrypt_error);
					}
				}
			}
		});

		(passwordInputDlg.findViewById(R.id.cancel_btn)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				passwordInputDlg.dismiss();
			}
		});
		passwordInputDlg.show();
	}

	private void veriryPassword(String password) {
		if (NetUtils.isNetworkConnected(context)) {
			loadingDlg = new LoadingDialog(context);
			loadingDlg.show();
			AppAPIService apiService = new AppAPIService(context);
			apiService.setAPIInterface(new WebService());
			String myInfo = PreferencesUtils.getString(context, "myInfo");
			GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
			apiService.veriryApprovalPassword(getMyInfoResult.getCode(), password);
		}
	}

	public void installApp() {
		if (NetUtils.isNetworkConnected(context)) {
			loadingDlg = new LoadingDialog(context);
			loadingDlg.show();
			loadingDlg.setText("");
			final String downloadStr = context.getString(R.string.app_download);
			Callback.ProgressCallback<File> progressCallback = new Callback.ProgressCallback<File>() {
				@Override
				public void onWaiting() {

				}

				@Override
				public void onStarted() {
					loadingDlg.setText(downloadStr + "%0");
				}

				@Override
				public void onLoading(long total, long current, boolean b) {
					if (loadingDlg != null && loadingDlg.isShowing()) {
						if (total == 0) {
							total = 1;
						}
						loadingDlg.setHint(downloadStr + "100%");
						int progress = (int) ((current * 100) / total);
						loadingDlg.setText(downloadStr + progress + "%");
					}
				}

				@Override
				public void onSuccess(File file) {
					if (loadingDlg != null && loadingDlg.isShowing()) {
						loadingDlg.dismiss();
					}
					AppUtils.openAPKFile(context, file);
				}

				@Override
				public void onError(Throwable throwable, boolean b) {
					if (loadingDlg != null && loadingDlg.isShowing()) {
						loadingDlg.dismiss();
					}
					ToastUtils.show(context, R.string.download_fail);
				}

				@Override
				public void onCancelled(CancelledException e) {

				}

				@Override
				public void onFinished() {

				}
			};
			new DownLoaderUtils().startDownLoad(app.getInstallUri(), MyAppConfig.LOCAL_DOWNLOAD_PATH + app.getAppID() + ".apk", progressCallback);
		}
	}

	private class WebService extends APIInterfaceInstance {
		@Override
		public void returnVeriryApprovalPasswordSuccess(String password) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			if (passwordInputDlg != null && passwordInputDlg.isShowing()) {
				passwordInputDlg.dismiss();
			}
			PreferencesByUserAndTanentUtils.putString(context, "approvalPassword", password);
			openNativeApp();
		}

		@Override
		public void returnVeriryApprovalPasswordFail(String error) {
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			ToastUtils.show(context, R.string.app_password_error);
			PreferencesByUserAndTanentUtils.putString(context, "approvalPassword", "");
			if (passwordInputDlg == null) {
				showPasswordInputDlg();
			} else if (!passwordInputDlg.isShowing()) {
				passwordInputDlg.show();
			}

		}
	}
}
