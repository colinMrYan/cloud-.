/**
 *
 * LoginAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.LoginAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:34:43
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.GetBoolenResult;
import com.inspur.emmcloud.bean.GetLoginResult;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.GetRegisterCheckResult;
import com.inspur.emmcloud.bean.GetUpdatePwdBySMSCodeBean;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.OauthUtils;
import com.inspur.emmcloud.util.PreferencesUtils;

import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * com.inspur.emmcloud.api.apiservice.LoginAPIService create at 2016年11月8日
 * 下午2:34:43
 */
public class LoginAPIService {
	private Context context;
	private APIInterface apiInterface;

	public LoginAPIService(Context context) {
		this.context = context;
	}

	public void setAPIInterface(APIInterface apiInterface) {
		this.apiInterface = apiInterface;
	}

	/**
	 * 登录
	 *
	 * @param userName
	 * @param password
	 */
	public void OauthSignin(String userName, String password) {
		String completeUrl = APIUri.getOauthSigninUrl();
		RequestParams params = new RequestParams(completeUrl);
		params.addParameter("grant_type", "password");
		params.addParameter("username", userName);
		params.addParameter("password", password);
		params.addParameter("client_id", "com.inspur.ecm.client.android");
		params.addParameter("client_secret",
				"6b3c48dc-2e56-440c-84fb-f35be37480e8");
		x.http().post(params, new APICallback(context,completeUrl) {

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninSuccess(new GetLoginResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninFail(error, responseCode);
			}

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninFail(new String(""), 500);
			}

		});
	}

	/**
	 * 刷新token
	 */
	public void refreshToken() {
		String completeUrl = APIUri.getOauthSigninUrl();
		String refreshToken = PreferencesUtils.getString(context,
				"refreshToken", "");
		RequestParams params = new RequestParams(completeUrl);
		params.addParameter("client_id", "com.inspur.ecm.client.android");
		params.addParameter("client_secret",
				"6b3c48dc-2e56-440c-84fb-f35be37480e8");
		params.addParameter("refresh_token", refreshToken);
		params.addParameter("grant_type", "refresh_token");
		x.http().post(params, new APICallback(context,completeUrl) {

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninSuccess(new GetLoginResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninFail(error,responseCode);
			}

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninFail("",-1);
			}

		});

	}

	/**
	 * 短信登录-发送短信
	 *
	 * @param mobile
	 */
	public void reqLoginSMS(String mobile) {
		String completeUrl = APIUri.getReqLoginSMSUrl(mobile);
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				apiInterface.returnReqLoginSMSFail(
						context.getString(R.string.net_request_failed), 500);

			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface
						.returnReqLoginSMSSuccess(new GetBoolenResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnReqLoginSMSFail(error, responseCode);
			}
		});

	}


	/**
	 * 验证短信验证码
	 *
	 * @param mobile
	 * @param sms
	 */
	public void SMSRegisterCheck(String mobile, String sms) {
		String completeUrl =APIUri.getSMSRegisterCheckUrl();
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("mobile", mobile);
		params.addParameter("sms", sms);
		x.http().post(params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				apiInterface.returnReisterSMSCheckFail("",-1);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface
						.returnReisterSMSCheckSuccess(new GetRegisterCheckResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnReisterSMSCheckFail(error,responseCode);
			}
		});

	}


	/**
	 * 获取个人信息 得到当前用户的登录信息
	 */
	public void getMyInfo() {
		final String completeUrl = APIUri.getMyInfoUrl();
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						// TODO Auto-generated method stub
						getMyInfo();
					}

					@Override
					public void executeFailCallback() {
						callbackFail("", -1);
					}
				}, context).refreshToken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnMyInfoSuccess(new GetMyInfoResult(
						arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnMyInfoFail(error,responseCode);
			}
		});
	}

	/**
	 * 修改密码
	 *
	 * @param oldpsd
	 * @param newpsd
	 */
	public void changePsd(final String oldpsd, final String newpsd) {
		final String completeUrl  = APIUri.getChangePsdUrl();
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.addQueryStringParameter("old", oldpsd);
		params.addQueryStringParameter("new", newpsd);
		params.setAsJsonContent(true);
		params.addHeader("Content-Type", "application/json");
		x.http().request(HttpMethod.PUT, params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						// TODO Auto-generated method stub
						changePsd(oldpsd, newpsd);
					}

					@Override
					public void executeFailCallback() {
						callbackFail("", -1);
					}
				}, context).refreshToken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnModifyPsdSuccess();
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnModifyPsdFail(error,responseCode);
			}
		});
	}


	/**
	 * 通过短信验证码更新密码
	 * @param smsCode
	 * @param newPwd
	 */
	public void updatePwdBySMSCode(final String smsCode,final String newPwd){
		final String completeUrl = APIUri.getChangePsdUrl();
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.addQueryStringParameter("passcode", smsCode);
		params.addQueryStringParameter("new", newPwd);
		params.setAsJsonContent(true);
		params.addHeader("Content-Type", "application/json");
		x.http().request(HttpMethod.PUT, params, new APICallback(context,completeUrl) {
			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {
					@Override
					public void reExecute() {
						updatePwdBySMSCode( smsCode, newPwd);
					}

					@Override
					public void executeFailCallback() {
						callbackFail("", -1);
					}
				}, context).refreshToken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnUpdatePwdBySMSCodeSuccess(new GetUpdatePwdBySMSCodeBean(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnUpdatePwdBySMSCodeFail(error,responseCode);
			}
		});
	}


//	/**
//	 * 暂时没用上的接口
//	 * 
//	 * @param mobile
//	 * @param username
//	 * @param userpsd
//	 * @param registerId
//	 * @param xx
//	 */
//	public void uploadSMSRegInfo(String mobile, String username,
//			String userpsd, String registerId, String xx) {
//
//		String module = "register";
//		String method = "register";
//		RequestParams params = new RequestParams();
//		params.put("mobile", mobile);
//		params.put("user_name", username);
//		params.put("user_psd", userpsd);
//		params.put("register_id", registerId);
//		params.put("xx", xx);
//		String completeUrl = baseUrl + "module=" + module + "&method=" + method;
//		AsyncHttpResponseHandler asyncHttpResponseHandler = new AsyncHttpResponseHandler() {
//
//			@Override
//			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
//
//			}
//
//			@Override
//			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
//					Throwable arg3) {
//
//			}
//
//			@Override
//			public void onTokenExpire() {
//
//			}
//		};
//		asyncHttpResponseHandler.setIsDebug(LogUtils.isDebug);
//		client.post(completeUrl, params, asyncHttpResponseHandler);
//	}

//	/**
//	 * 手机短信注册，传入手机号码，验证是否已经注册，如果已经注册返回-1，未注册返回1
//	 * 
//	 * @param mobile
//	 */
//	public void SMSRegister(String mobile) {
//		String module = "register";
//		String method = "get_smscode";
//		RequestParams params = new RequestParams();
//		params.put("mobile", mobile);
//		String completeUrl = baseUrl + "module=" + module + "&method=" + method;
//		AsyncHttpResponseHandler asyncHttpResponseHandler = new AsyncHttpResponseHandler() {
//
//			@Override
//			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
//				apiInterface.returnRegisterSMSSuccess(new GetRegisterResult(
//						new String(arg2)));
//			}
//
//			@Override
//			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
//					Throwable arg3) {
//				apiInterface.returnRegisterSMSFail(new String(arg2));
//			}
//
//			@Override
//			public void onTokenExpire() {
//				apiInterface.returnRegisterSMSFail(context
//						.getString(R.string.net_request_failed));
//			}
//		};
//		asyncHttpResponseHandler.setIsDebug(LogUtils.isDebug);
//		client.post(completeUrl, params, asyncHttpResponseHandler);
//	}

}
