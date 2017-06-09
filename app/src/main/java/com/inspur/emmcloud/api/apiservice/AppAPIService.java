/**
 *
 * MyAppAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.MyAppAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:31:55
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.GetAppTabAutoResult;
import com.inspur.emmcloud.bean.GetAppTabsResult;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.bean.GetDeviceCheckResult;
import com.inspur.emmcloud.bean.GetExceptionResult;
import com.inspur.emmcloud.bean.GetUpgradeResult;
import com.inspur.emmcloud.bean.ReactNativeUpdateBean;
import com.inspur.emmcloud.bean.SplashPageBean;
import com.inspur.emmcloud.callback.OauthCallBack;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.OauthUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;

import org.json.JSONObject;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;


/**
 * com.inspur.emmcloud.api.apiservice.MyAppAPIService create at 2016年11月8日
 * 下午2:31:55
 */
public class AppAPIService {
	private Context context;
	private APIInterface apiInterface;

	public AppAPIService(Context context) {
		this.context = context;

	}

	public void setAPIInterface(APIInterface apiInterface) {
		this.apiInterface = apiInterface;
	}

	/**
	 * 获取版本更新信息
	 * @param isManualCheck shi
	 */
	public void checkUpgrade(final boolean isManualCheck) {
		String completeUrl = APIUri.checkUpgrade();
		String clientVersion = AppUtils.getVersion(context);
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("clientVersion", clientVersion);
		if (((MyApplication) context.getApplicationContext()).isVersionDev()) {
			params.addParameter("clientType", "dev_android");
		} else {
			params.addParameter("clientType", "android");
		}

		x.http().post(params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				apiInterface.returnUpgradeFail(new String(""),isManualCheck,-1);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnUpgradeSuccess(new GetUpgradeResult(arg0),isManualCheck);
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnUpgradeFail(error,isManualCheck,responseCode);
			}
		});

	}

	/**
	 * 获取ClientId
	 * @param deviceId
	 * @param deviceName
	 */
	public void getClientId(final String deviceId, final String deviceName){
		final String completeUrl = APIUri.getClientId();
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("deviceId",deviceId);
		params.addParameter("deviceName",deviceName);
		x.http().post(params, new APICallback(context,completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnGetClientIdResultSuccess(new GetClientIdRsult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnGetClientIdResultFail(error,responseCode);
			}

			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {
					@Override
					public void reExecute() {
						getClientId(deviceId,deviceName);
					}
					@Override
					public void executeFailCallback() {
						callbackFail("",-1);
					}
				},context).refreshToken(completeUrl);
			}
		});
	}


	/**
	 * 获取ReactNative更新版本
	 * @param version
	 * @param lastCreationDate
	 */
	public void getReactNativeUpdate (final String version, final long lastCreationDate, final String clientId){
		final String completeUrl = APIUri.getReactNativeUpdate()+"version="+version+"&lastCreationDate=" + lastCreationDate
				+"&clientId="+clientId;
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context,completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnReactNativeUpdateSuccess(new ReactNativeUpdateBean(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnReactNativeUpdateFail(error,responseCode);
			}

			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {
					@Override
					public void reExecute() {
						getReactNativeUpdate(version,lastCreationDate,clientId);
					}
					@Override
					public void executeFailCallback() {
						callbackFail("",-1);
					}
				},context).refreshToken(completeUrl);
			}
		});
	}


	/**
	 * 回写ReactNative日志接口
	 * @param command
	 * @param version
	 * @param clientId
	 */
	public void sendBackReactNativeUpdateLog(final String command, final String version, final String clientId){
		final String completeUrl = APIUri.getClientLog()+"command=" + command+"&version="+version
				+"&clientId="+clientId;
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().request(HttpMethod.PUT,params, new APICallback(context,completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
				LogUtils.YfcDebug("回写日志成功返回结果："+arg0);
			}

			@Override
			public void callbackFail(String error, int responseCode) {
			}

			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {
					@Override
					public void reExecute() {
						sendBackReactNativeUpdateLog(command,version,clientId);
					}
					@Override
					public void executeFailCallback() {
						callbackFail("",-1);
					}
				},context).refreshToken(completeUrl);
			}
		});
	}



	/**
	 * 异常上传
	 *
	 * @param exception
	 */
	public void uploadException(final JSONObject exception) {
		final String completeUrl =APIUri.uploadException();
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.setAsJsonContent(true);
		params.setBodyContent(exception.toString());
		x.http().post(params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				apiInterface.returnUploadExceptionFail(new String(""),-1);
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface
						.returnUploadExceptionSuccess(new GetExceptionResult(
								arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnUploadExceptionFail(error,responseCode);
			}
		});
	}

	/**
	 * 获取显示tab页的接口
	 */
	public void getAppTabs(){
		final String completeUrl = APIUri.getAppTabs();
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().request(HttpMethod.GET, params, new APICallback(context,completeUrl) {
			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						getAppTabs();
					}
					@Override
					public void executeFailCallback() {
						callbackFail("",-1);
					}
				}, context).refreshToken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnGetAppTabsSuccess(new GetAppTabsResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnAddAppFail(error,responseCode);
			}
		});
	}


	/**
	 * 获取显示tab页的接口
	 */
	public void getAppNewTabs(final String version, final String clientId){
		final String completeUrl = APIUri.getAppNewTabs()+"?version="+version+"&clientId="+clientId;
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().request(HttpMethod.GET, params, new APICallback(context,completeUrl) {
			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						getAppNewTabs(version,clientId);
					}
					@Override
					public void executeFailCallback() {
						callbackFail("",-1);
					}
				}, context).refreshToken(completeUrl);
			}

			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnAppTabAutoSuccess(new GetAppTabAutoResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnAppTabAutoFail(error,responseCode);
			}
		});
	}


	/**
	 * 手机应用PV信息（web应用）
	 * @param collectInfo
	 */
	public void uploadPVCollect(String collectInfo){
		String  completeUrl = "http://u.inspur.com/analytics/api/ECMPV/Post";
		RequestParams params = new RequestParams(completeUrl);
		params.setBodyContent(collectInfo);
		params.setAsJsonContent(true);
		x.http().request(HttpMethod.POST, params, new APICallback(context,completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnUploadCollectSuccess();
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnUploadCollectFail(error,responseCode);
			}

			@Override
			public void callbackTokenExpire() {

			}
		});

	}

	/**
	 * 验证行政审批密码
	 * @param password
	 */
	public void veriryApprovalPassword(String userName, final String password){
		String  completeUrl = "https://emm.inspur.com/proxy/shenpi/langchao.ecgap.inportal/login/CheckLoginDB.aspx?";
		RequestParams params = new RequestParams(completeUrl);
		params.addQueryStringParameter("userName",userName);
		params.addQueryStringParameter("userPass",password);
		x.http().request(HttpMethod.GET, params, new APICallback(context,completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
				if (arg0.equals("登录成功")){
					apiInterface.returnVeriryApprovalPasswordSuccess(password);
				}else{
					apiInterface.returnVeriryApprovalPasswordFail("",-1);
				}

			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnVeriryApprovalPasswordFail(error,responseCode);
			}

			@Override
			public void callbackTokenExpire() {
				apiInterface.returnVeriryApprovalPasswordFail("",-1);
			}
		});

	}

	/**
	 * 上传设备管理需要的一些信息
	 */
	public void uploadMDMInfo(){
		final String completeUrl = APIUri.getUploadMDMInfoUrl();
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		params.addParameter("udid",AppUtils.getMyUUID(context));
		String refreshToken = PreferencesUtils.getString(context, "refreshToken", "");
		params.addParameter("refresh_token",refreshToken);
		x.http().post(params, new APICallback(context,completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
			}

			@Override
			public void callbackFail(String error, int responseCode) {
			}

			@Override
			public void callbackTokenExpire() {

				new OauthUtils(new OauthCallBack() {
					@Override
					public void reExecute() {
						uploadMDMInfo();
					}

					@Override
					public void executeFailCallback() {
						callbackFail("",-1);
					}
				}, context).refreshToken(completeUrl);
			}
		});
	}

	/**
	 * 获取闪屏页信息
	 * 采用新式数据解析方法
	 * @param clientId
	 * @param versionCode
	 */
	public void getSplashPageInfo(final String clientId, final String versionCode) {
		final String completeUrl = APIUri.getSplashPageUrl() + "?version=" + versionCode + "&clientId=" + clientId;
		RequestParams params = ((MyApplication) context.getApplicationContext())
				.getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context, completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
				SplashPageBean splashPageBean = new SplashPageBean(arg0);
				if (splashPageBean.getCommand().equals("FORWARD")) {
					String splashPageInfoOld = PreferencesByUserAndTanentUtils.getString(context,"splash_page_info","");
					PreferencesByUserAndTanentUtils.putString(context,"splash_page_info_old",splashPageInfoOld);
					PreferencesByUserAndTanentUtils.putString(context, "splash_page_info", arg0);
				}
				apiInterface.returnSplashPageInfoSuccess(splashPageBean);
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnSplashPageInfoFail(error, responseCode);
			}

			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {
					@Override
					public void reExecute() {
						getSplashPageInfo(clientId, versionCode);
					}
					@Override
					public void executeFailCallback() {
						callbackFail("",-1);
					}
				}, context).refreshToken(completeUrl);
			}
		});
	}


	/**
	 * 设备检查
	 * @param tenantId
	 * @param userCode
	 */
	public void deviceCheck(String tenantId, String userCode) {
		// TODO Auto-generated method stub
		String baseUrl = "https://emm.inspur.com/api?";
		String module = "mdm";
		String method = "check_state";
		String completeUrl = baseUrl + "module=" + module + "&method=" + method;
		String uuid = AppUtils.getMyUUID(context);
		RequestParams params = new RequestParams(completeUrl);
		params.addBodyParameter("udid", uuid);
		params.addBodyParameter("tenant_id", tenantId);
		params.addBodyParameter("mdm_user_auth_type", "IDMUser");
		params.addBodyParameter("user_code", userCode);
		// params.addBodyParameter("app_mdm_id", "imp"); // 和ios约定的appid
		x.http().post(params, new APICallback(context,completeUrl) {
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnDeviceCheckSuccess(new GetDeviceCheckResult(
						arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnDeviceCheckFail(error,responseCode);
			}

			@Override
			public void callbackTokenExpire() {
				apiInterface.returnDeviceCheckFail("",-1);
			}
		});


	}


}
