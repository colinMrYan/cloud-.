package com.inspur.emmcloud.util;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.GetLoginResult;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.mdm.MDM;
import com.inspur.mdm.MDMListener;
import com.inspur.mdm.utils.MDMUtils;

/**
 * 登录公共类
 * 
 * @author Administrator
 *
 */
public class LoginUtils extends APIInterfaceInstance {
	private static final int LOGIN_SUCCESS = 0;
	private static final int LOGIN_FAIL = 1;
	private static final int GET_LANGUAGE_SUCCESS = 3;
	private static LoginAPIService apiServices;
	private Activity activity;
	private Handler handler;
	private boolean isLogin = false;
	private boolean isSMSLogin = false;
	private Handler loginUtilsHandler;
	private LanguageUtils languageUtils;
	private GetLoginResult getLoginResult;
	private LoadingDialog loadingDialog;

	public LoginUtils(Activity activity, Handler handler) {
		this.handler = handler;
		this.activity = activity;
		loadingDialog = new LoadingDialog(activity);
		apiServices = new LoginAPIService(activity);
		apiServices.setAPIInterface(LoginUtils.this);
		handMessage();
	}

	/**
	 *
	 */
	private void handMessage() {
		// TODO Auto-generated method stub
		loginUtilsHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case GET_LANGUAGE_SUCCESS:
					// handler.sendEmptyMessage(LOGIN_SUCCESS);
					startMDM();
					break;
				case LOGIN_SUCCESS:
					if (loadingDialog != null && loadingDialog.isShowing()) {
						loadingDialog.dismiss();
					}
					handler.sendEmptyMessage(LOGIN_SUCCESS);
					break;
				case LOGIN_FAIL:
					if (loadingDialog != null && loadingDialog.isShowing()) {
						loadingDialog.dismiss();
					}
					handler.sendEmptyMessage(LOGIN_FAIL);
					break;
				default:
					break;
				}
			}

		};
	}

	// 开始进行设备管理检查
	private void startMDM() {
		// TODO Auto-generated method stub
		((MyApplication) activity.getApplicationContext()).setAccessToken("");
		String userName = PreferencesUtils.getString(activity, "userRealName",
				"");
		String myInfo = PreferencesUtils.getString(activity, "myInfo", "");
		GetMyInfoResult myInfoObj = new GetMyInfoResult(myInfo);
		String tanentId = myInfoObj.getEnterpriseId();
		LogUtils.jasonDebug("tanentId=" + tanentId);
		String userCode = ((MyApplication) activity.getApplicationContext())
				.getUid();
		MDM mdm = new MDM(activity, tanentId, userCode, userName);
		mdm.addOnMDMListener(new MDMListener() {

			@Override
			public void MDMStatusPass() {
				// TODO Auto-generated method stub
				saveLoginInfo();
				loginUtilsHandler.sendEmptyMessage(LOGIN_SUCCESS);
			}

			@Override
			public void MDMStatusNoPass() {
				// TODO Auto-generated method stub
				clearLoginInfo();
				loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
			}

			@Override
			public void dimissExternalLoadingDlg() {
				// TODO Auto-generated method stub
				if (loadingDialog != null && loadingDialog.isShowing()) {
					loadingDialog.dismiss();
				}
			}
		});
		mdm.deviceCheck();
	}

	// 登录
	public void login(String userName, String password) {
		if (NetUtils.isNetworkConnected(activity)) {
			loadingDialog.show();
			clearLoginInfo();
			apiServices.OauthSignin(userName, password);
		}else {
			loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
		}
		
	}

	// 登录
	public void login(String userName, String password, boolean isSMSLogin) {
		this.isSMSLogin = isSMSLogin;
		login(userName, password);
	}

	/**
	 * 获取基本信息
	 */
	public void getMyInfo() {
		apiServices.getMyInfo();
	}

	/**
	 * 清空登录信息
	 */
	private void clearLoginInfo() {
		PreferencesUtils.putString(activity, "accessToken", "");
		PreferencesUtils.putString(activity, "refreshToken", "");
		PreferencesUtils.putInt(activity, "keepAlive", 0);
		PreferencesUtils.putString(activity, "tokenType", "");
		PreferencesUtils.putInt(activity, "expiresIn", 0);
	}

	private void saveLoginInfo() {
		String accessToken = getLoginResult.getAccessToken();
		String refreshToken = getLoginResult.getRefreshToken();
		int keepAlive = getLoginResult.getKeepAlive();
		String tokenType = getLoginResult.getTokenType();
		int expiresIn = getLoginResult.getExpiresIn();
		((MyApplication) activity.getApplicationContext())
				.setAccessToken(accessToken);
		PreferencesUtils.putString(activity, "accessToken", accessToken);
		PreferencesUtils.putString(activity, "refreshToken", refreshToken);
		PreferencesUtils.putInt(activity, "keepAlive", keepAlive);
		PreferencesUtils.putString(activity, "tokenType", tokenType);
		PreferencesUtils.putInt(activity, "expiresIn", expiresIn);
	}

	@Override
	public void returnOauthSigninSuccess(GetLoginResult getLoginResult) {
		// TODO Auto-generated method stub
		this.getLoginResult = getLoginResult;
		String accessToken = getLoginResult.getAccessToken();
		((MyApplication) activity.getApplicationContext())
				.setAccessToken(accessToken);
		isLogin = true;
		getMyInfo();
	}

	@Override
	public void returnOauthSigninFail(String error, int errorCode) {
		// TODO Auto-generated method stub
		if (errorCode == 400) {
			if (!isSMSLogin) {
				ToastUtils.show(activity, R.string.invaliad_account_or_pwd);
			} else {
				ToastUtils.show(activity, R.string.code_verification_failure);
			}
			
		} else {
			WebServiceMiddleUtils.hand(activity, error);
		}
		
		loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
	}

	@Override
	public void returnMyInfoSuccess(GetMyInfoResult getMyInfoResult) {
		// TODO Auto-generated method stub
		String myInfo = getMyInfoResult.getResponse();
		String name = getMyInfoResult.getName();
		PreferencesUtils.putString(activity, "userRealName", name);
		PreferencesUtils.putString(activity, "userID", getMyInfoResult.getID());
		PreferencesUtils.putString(activity, "myInfo", myInfo);
		PreferencesUtils.putBoolean(activity, "hasPassword",
				getMyInfoResult.getHasPassord());
		((MyApplication) activity.getApplicationContext()).initTanent();
		((MyApplication) activity.getApplicationContext())
				.setUid(getMyInfoResult.getID());
		if (isLogin) {
			apiServices.uploadAuthorizationInfo(0, null, null);
			isLogin = false;
		}
		if (handler != null) {
			languageUtils = new LanguageUtils(activity, loginUtilsHandler);
			languageUtils.getServerSupportLanguage();
		}
	}

	@Override
	public void returnMyInfoFail(String error) {
		// TODO Auto-generated method stub
		clearLoginInfo();
		loginUtilsHandler.sendEmptyMessage(LOGIN_FAIL);
		WebServiceMiddleUtils.hand(activity, error);
	}

}
