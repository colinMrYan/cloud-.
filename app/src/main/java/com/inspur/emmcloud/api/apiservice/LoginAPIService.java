/**
 * 
 * LoginAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.LoginAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:34:43
 */
package com.inspur.emmcloud.api.apiservice;

import org.xutils.x;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.bean.GetBoolenResult;
import com.inspur.emmcloud.bean.GetLoginResult;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.GetRegisterCheckResult;
import com.inspur.emmcloud.bean.GetSignoutResult;
import com.inspur.emmcloud.bean.GetUpdatePwdBySMSCodeBean;
import com.inspur.emmcloud.bean.GetWebSocketUrlResult;
import com.inspur.emmcloud.util.OauthCallBack;
import com.inspur.emmcloud.util.OauthUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.UriUtils;

/**
 * com.inspur.emmcloud.api.apiservice.LoginAPIService create at 2016年11月8日
 * 下午2:34:43
 */
public class LoginAPIService {
	private Context context;
	private APIInterface apiInterface;
	private String baseUrl = "https://emm.inspur.com/api?";

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
		String completeUrl = "https://id.inspur.com/oauth2.0/token";
		RequestParams params = new RequestParams(completeUrl);
		params.addParameter("grant_type", "password");
		params.addParameter("username", userName);
		params.addParameter("password", password);
		params.addParameter("client_id", "com.inspur.ecm.client.android");
		params.addParameter("client_secret",
				"6b3c48dc-2e56-440c-84fb-f35be37480e8");
		x.http().post(params, new APICallback() {

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
		String completeUrl = "https://id.inspur.com/oauth2.0/token";
		String refreshToken = PreferencesUtils.getString(context,
				"refreshToken", "");
		RequestParams params = new RequestParams(completeUrl);
		params.addParameter("client_id", "com.inspur.ecm.client.android");
		params.addParameter("client_secret",
				"6b3c48dc-2e56-440c-84fb-f35be37480e8");
		params.addParameter("refresh_token", refreshToken);
		params.addParameter("grant_type", "refresh_token");
		x.http().post(params, new APICallback() {

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninSuccess(new GetLoginResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninFail(error);
			}

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninFail("");
			}

		});

	}

	/**
	 * 注销登录
	 */
	public void signout() {
		String module = "sign";
		String method = "signout";
		String completeUrl = "https://emm.inspur.com/api?module=" + module
				+ "&method=" + method;
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().post(params, new APICallback() {

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnSignoutSuccess(new GetSignoutResult(arg0));

			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnSignoutFail(error);
			}

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				apiInterface.returnSignoutFail(new String(""));
			}

		});
	}

	/**
	 * 短信登录-发送短信
	 *
	 * @param mobile
	 */
	public void reqLoginSMS(String mobile) {
		String completeUrl = "https://id.inspur.com/api/v1/passcode?phone="
				+ mobile;
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback() {

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
		String module = "register";
		String method = "verify_smscode";
		String completeUrl = baseUrl + "module=" + module + "&method=" + method;
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("mobile", mobile);
		params.addParameter("sms", sms);
		x.http().post(params, new APICallback() {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				apiInterface.returnReisterSMSCheckFail("");
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
				apiInterface.returnReisterSMSCheckFail(error);
			}
		});
		
	}
	
	
	/**
	 * 获取个人信息 得到当前用户的登录信息
	 */
	public void getMyInfo() {
		final String completeUrl = "https://id.inspur.com/oauth2.0/profile";
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback() {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						// TODO Auto-generated method stub
						getMyInfo();
					}
				}, context).refreshTocken(completeUrl);
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
				apiInterface.returnMyInfoFail(error);
			}
		});
	}
	
	/**
	 * 获取websocket的连接url
	 */
	public void getWebsocketUrl() {
		final String completeUrl = UriUtils.getHttpApiUri("settings/socket");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback() {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						getWebsocketUrl();
					}
				}, context).refreshTocken(completeUrl);
			}
			
			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface
				.returnWebSocketUrlSuccess(new GetWebSocketUrlResult(
						arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnWebSocketUrlFail(error);
			}
		});
	}
	
	
	/**
	 * 上传认证token信息
	 * 
	 * @param info
	 */
	public void uploadAuthorizationInfo(int type, String requestUrl,
			String oldToken) {
		String enterpriseCode = UriUtils.tanent;
		String account = PreferencesUtils.getString(context, "userName", "");
		final String completeUrl = "https://ecm.inspur.com/" + enterpriseCode
				+ "/tlog";
		String accessToken = PreferencesUtils.getString(context, "accessToken",
				"");
		String refreshToken = PreferencesUtils.getString(context,
				"refreshToken", "");
		String logInfo = "";
		switch (type) {
		case 0: // login
			logInfo = "Android client signed-in as account: " + account
					+ ". Got access token: " + accessToken
					+ ", refresh token: " + refreshToken;
			break;
		case 1:// api return 401
			logInfo = "Android client got code 401 when requesting "
					+ requestUrl + ". Current account: " + account
					+ ". Got access token: " + accessToken
					+ ", refresh token: " + refreshToken;
			break;
		case 2:// token refresh success
			logInfo = "Android client refreshed success whith token:"
					+ oldToken + ". Current account: " + account
					+ ". New access token: " + accessToken
					+ ", refresh token: " + refreshToken;
			break;
		case 3:// token refresh fail
			logInfo = "Android client refreshed failed whith token: "
					+ refreshToken + ". Current account: " + account;

			break;
		default:
			break;
		}
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.setBodyContent(logInfo);
		params.setAsJsonContent(true);
		x.http().post(params, new APICallback() {

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	
	/**
	 * 修改密码
	 * 
	 * @param calendarId
	 * @param title
	 */
	public void changePsd(final String oldpsd, final String newpsd) {
		final String completeUrl  = UriUtils.getChangePsd();
//		if (PreferencesUtils.getBoolean(context, "hasPassword")
//				&& !StringUtils.isEmpty(oldpsd)) {
//			completeUrl = UriUtils.getChangePsd() + "?old="
//					+ StringUtils.utf8Encode(oldpsd, "UTF-8") + "&new="
//					+ StringUtils.utf8Encode(newpsd, "UTF-8");
//		} else {
//			completeUrl = UriUtils.getChangePsd() + "?new="
//					+ StringUtils.utf8Encode(newpsd, "UTF-8");
//		}
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.addQueryStringParameter("old", oldpsd);
		params.addQueryStringParameter("new", newpsd);
		params.setAsJsonContent(true);
		params.addHeader("Content-Type", "application/json");
		x.http().request(HttpMethod.PUT, params, new APICallback() {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						// TODO Auto-generated method stub
						changePsd(oldpsd, newpsd);
					}
				}, context).refreshTocken(completeUrl);
			}
			
			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnModifyPsdSuccess();
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnModifyPsdFail(error);
			}
		});
	}
	
	
	/**
	 * 通过短信验证码更新密码
	 * @param userPhone
	 * @param smsCode
	 * @param newPwd
	 */
	public void updatePwdBySMSCode(final String smsCode,final String newPwd){
		final String completeUrl = UriUtils.getChangePsd(); 
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.addQueryStringParameter("passcode", smsCode);
		params.addQueryStringParameter("new", newPwd);
		params.setAsJsonContent(true);
		params.addHeader("Content-Type", "application/json");
		x.http().request(HttpMethod.PUT, params, new APICallback() {
			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {
					@Override
					public void execute() {
						updatePwdBySMSCode( smsCode, newPwd);
					}
				}, context).refreshTocken(completeUrl);
			}
			
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnUpdatePwdBySMSCodeSuccess(new GetUpdatePwdBySMSCodeBean(arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnUpdatePwdBySMSCodeFail(error);
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
