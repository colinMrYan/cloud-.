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
import com.inspur.emmcloud.api.CloudHttpMethod;
import com.inspur.emmcloud.api.HttpUtils;
import com.inspur.emmcloud.bean.appcenter.GetRegisterCheckResult;
import com.inspur.emmcloud.bean.login.GetLoginResult;
import com.inspur.emmcloud.bean.login.GetUpdatePwdBySMSCodeBean;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.json.JSONObject;
import org.xutils.http.RequestParams;

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
		HttpUtils.request(context, CloudHttpMethod.POST,params, new APICallback(context,completeUrl) {

			@Override
			public void callbackSuccess(byte[] arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninSuccess(new GetLoginResult(new String(arg0)));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninFail(error, responseCode);
			}

			@Override
			public void callbackTokenExpire(long requestTime) {
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
		String refreshToken = MyApplication.getInstance().getRefreshToken();
		RequestParams params = new RequestParams(completeUrl);
		params.setConnectTimeout(30000);
		params.addParameter("client_id", "com.inspur.ecm.client.android");
		params.addParameter("client_secret",
				"6b3c48dc-2e56-440c-84fb-f35be37480e8");
		params.addParameter("refresh_token", refreshToken);
		params.addParameter("grant_type", "refresh_token");
		HttpUtils.request(context,CloudHttpMethod.POST,params, new APICallback(context,completeUrl) {

			@Override
			public void callbackSuccess(byte[] arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninSuccess(new GetLoginResult(new String(arg0)));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnOauthSigninFail(error,responseCode);
			}

			@Override
			public void callbackTokenExpire(long requestTime) {
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
		HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				// TODO Auto-generated method stub
				apiInterface.returnReqLoginSMSFail(
						context.getString(R.string.net_request_failed), 500);

			}

			@Override
			public void callbackSuccess(byte[] arg0) {
				// TODO Auto-generated method stub
				apiInterface
						.returnReqLoginSMSSuccess(new GetBoolenResult(new String(arg0)));
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
		HttpUtils.request(context,CloudHttpMethod.POST,params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				// TODO Auto-generated method stub
				apiInterface.returnReisterSMSCheckFail("",-1);
			}

			@Override
			public void callbackSuccess(byte[] arg0) {
				// TODO Auto-generated method stub
				apiInterface
						.returnReisterSMSCheckSuccess(new GetRegisterCheckResult(new String(arg0)));
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
		HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						getMyInfo();
					}

					@Override
					public void executeFailCallback() {
						callbackFail("", -1);
					}
				};
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}

			@Override
			public void callbackSuccess(byte[] arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnMyInfoSuccess(new GetMyInfoResult(new String(arg0)));
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
		HttpUtils.request(context,CloudHttpMethod.PUT, params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						changePsd(oldpsd, newpsd);
					}

					@Override
					public void executeFailCallback() {
						callbackFail("", -1);
					}
				};
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}

			@Override
			public void callbackSuccess(byte[] arg0) {
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
		HttpUtils.request(context,CloudHttpMethod.PUT, params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						updatePwdBySMSCode( smsCode, newPwd);
					}

					@Override
					public void executeFailCallback() {
						callbackFail("", -1);
					}
				};
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}

			@Override
			public void callbackSuccess(byte[] arg0) {
				apiInterface.returnUpdatePwdBySMSCodeSuccess(new GetUpdatePwdBySMSCodeBean(new String(arg0)));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnUpdatePwdBySMSCodeFail(error,responseCode);
			}
		});
	}

	public void faceLoginGS(final String bitmapBase64,final String token ){
		final String completeUrl = "https://emm.inspur.com/app/imp/v6.0/Connect/FaceLogin";
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		JSONObject object = new JSONObject();
		try {
			object.put("token",token);
			object.put("face",bitmapBase64);
		}catch (Exception e){
			e.printStackTrace();
		}
		params.setBodyContent(object.toString());
		params.setAsJsonContent(true);
		HttpUtils.request(context,CloudHttpMethod.POST,params, new APICallback(context,completeUrl) {
			@Override
			public void callbackSuccess(byte[] arg0) {
				apiInterface.returnFaceLoginGSSuccess();
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnFaceLoginGSFail(error, responseCode);
			}

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						faceLoginGS(bitmapBase64, token);
					}

					@Override
					public void executeFailCallback() {
						callbackFail("", -1);
					}
				};
				OauthUtils.getInstance().refreshToken(
						oauthCallBack, requestTime);
			}

		});

	}
}
