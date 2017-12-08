/**
 * MineAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.MineAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:34:55
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.GetBindingDeviceResult;
import com.inspur.emmcloud.bean.GetBoolenResult;
import com.inspur.emmcloud.bean.GetCardPackageListResult;
import com.inspur.emmcloud.bean.GetDeviceLogResult;
import com.inspur.emmcloud.bean.GetLanguageResult;
import com.inspur.emmcloud.bean.GetMDMStateResult;
import com.inspur.emmcloud.bean.GetUploadMyHeadResult;
import com.inspur.emmcloud.bean.UserProfileInfoBean;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.OauthUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.UriUtils;

import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;


/**
 * com.inspur.emmcloud.api.apiservice.MineAPIService create at 2016年11月8日
 * 下午2:34:55
 */
public class MineAPIService {
	private Context context;
	private APIInterface apiInterface;
	private String baseUrl = "https://emm.inspur.com/api?";

	public MineAPIService(Context context) {
		this.context = context;
	}

	public void setAPIInterface(APIInterface apiInterface) {
		this.apiInterface = apiInterface;
	}

	/**
	 * 修改用户头像
	 *
	 * @param
	 */
	public void updateUserHead(final String filePath) {
		String module = "user";
		String method = "update_head";
		final String completeUrl = baseUrl + "module=" + module + "&method="
				+ method;

		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		File file = new File(filePath);
		params.setMultipart(true);// 有上传文件时使用multipart表单, 否则上传原始文件流.
		params.addBodyParameter("head", file);
		x.http().post(params, new APICallback(context, completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {
					@Override
					public void reExecute() {
						updateUserHead(filePath);
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
				apiInterface
						.returnUploadMyHeadSuccess(new GetUploadMyHeadResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnUploadMyHeadFail(error,responseCode);
			}
		});

	}

	/**
	 * 修改用户信息
	 *
	 * @param key
	 * @param value
	 */
	public void modifyUserInfo(final String key, final String value) {

		String module = "user";
		String method = "update_baseinfo";

		final String completeUrl = baseUrl + "module=" + module + "&method="
				+ method;

		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("key", key);
		params.addParameter("value", value);
		x.http().post(params, new APICallback(context, completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						modifyUserInfo(key, value);
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
				apiInterface.returnModifyUserInfoSucces(new GetBoolenResult(
						arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnModifyUserInfoFail(error,responseCode);

			}
		});

	}

	/**
	 * 上传反馈和建议接口
	 *
	 * @param content
	 * @param contact
	 * @param userName
	 */
	public void uploadFeedback(final String content, final String contact,
							   final String userName) {
		final String completeUrl = "http://u.inspur.com/analytics/RestFulServiceForIMP.ashx?resource=Feedback&method=AddECMFeedback";
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		String AppBaseID = context.getPackageName();
		String AppVersion = AppUtils.getVersion(context);
		String Organization = PreferencesUtils
				.getString(context, "orgName", "");
		String UUID = AppUtils.getMyUUID(context);
		params.addParameter("Content", content);
		params.addParameter("AppBaseID", AppBaseID);
		params.addParameter("AppVersion", AppVersion);
		params.addParameter("System", "android");
		params.addParameter("SystemVersion", android.os.Build.VERSION.RELEASE);
		params.addParameter("UserName", userName);
		params.addParameter("Organization", Organization);
		params.addParameter("Contact", contact);
		params.addParameter("Email", "");
		params.addParameter("Telephone", "");
		params.addParameter("UUID", UUID);
		x.http().post(params, new APICallback(context, completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub

			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub

			}
		});
	}

	/**
	 * 获取卡包信息
	 */
	public void getCardPackageList() {

		final String completeUrl = UriUtils.getHttpApiUri("wallet");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context, completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						// TODO Auto-generated method stub
						getCardPackageList();
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
				apiInterface
						.returnCardPackageListSuccess(new GetCardPackageListResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnCardPackageListFail(error,responseCode);
			}
		});
	}

	/**
	 * 获取语言
	 */
	public void getLanguage() {

		final String completeUrl = UriUtils.getHttpApiUri("settings/lang");
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context, completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						getLanguage();
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
				apiInterface.returnLanguageSuccess(new GetLanguageResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnLanguageFail(error,responseCode);
			}
		});
	}

	/**
	 * 获取我的信息
	 */
	public void getUserProfileInfo(){
		final String completeUrl = APIUri.getUserProfileUrl();
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context,completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnUserProfileSuccess(new UserProfileInfoBean(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnUserProfileFail(error,responseCode);
			}

			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						getUserProfileInfo();
					}

					@Override
					public void executeFailCallback() {
						callbackFail("", -1);
					}
				}, context).refreshToken(completeUrl);
			}
		});
	}


	/**
	 * 获取当前绑定设备列表
	 */
	public void getBindingDeviceList() {
		final String completeUrl = APIUri.getBindingDevicesUrl();
		RequestParams params =
				((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context, completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						getBindingDeviceList();
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
				apiInterface
						.returnBindingDeviceListSuccess(new GetBindingDeviceResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnBindingDeviceListFail(error,responseCode);
			}
		});
	}


	/**
	 * 获取当前绑定设备列表
	 */
	public void getDeviceLogList(final String udid) {
		final String completeUrl = APIUri.getDeviceLogUrl();
		RequestParams params =
				((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.addParameter("udid",udid);
		x.http().post(params, new APICallback(context, completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						getDeviceLogList(udid);
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
				apiInterface
						.returnDeviceLogListSuccess(new GetDeviceLogResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnDeviceLogListFail(error,responseCode);
			}
		});
	}

	/**
	 * 解绑设备
	 * @param udid
	 */
	public void unBindDevice(final String udid){
		final String completeUrl = APIUri.getUnBindDeviceUrl();
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("udid",udid);
		x.http().post(params, new APICallback(context,completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnUnBindDeviceSuccess();
			}

			@Override
			public void callbackFail(String error, int responseCode) {
					apiInterface.returnUnBindDeviceFail(error,responseCode);
			}

			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						unBindDevice(udid);
					}

					@Override
					public void executeFailCallback() {
						callbackFail("", -1);
					}
				}, context).refreshToken(completeUrl);
			}
		});
	}

	/**
	 * 获取是否启动MDM
	 */
	public void getMDMState(){
		final String completeUrl = APIUri.getMDMStateUrl();
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context,completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnMDMStateSuccess(new GetMDMStateResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnMDMStateFail(error,responseCode);
			}

			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						getMDMState();
					}

					@Override
					public void executeFailCallback() {
						callbackFail("", -1);
					}
				}, context).refreshToken(completeUrl);
			}
		});
	}

}
