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
import com.inspur.emmcloud.bean.GetAppTabsResult;
import com.inspur.emmcloud.bean.GetExceptionResult;
import com.inspur.emmcloud.bean.GetUpgradeResult;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.OauthCallBack;
import com.inspur.emmcloud.util.OauthUtils;

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
	private String baseUrl = "https://emm.inspur.com/api?";

	public AppAPIService(Context context) {
		this.context = context;
		
	}

	public void setAPIInterface(APIInterface apiInterface) {
		this.apiInterface = apiInterface;
	}

	/**
	 * 获取版本更新信息
	 * 
	 */
	public void checkUpgrade() {
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

		x.http().post(params, new APICallback() {

			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				apiInterface.returnUpgradeFail(new String(""));
			}

			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnUpgradeSuccess(new GetUpgradeResult(arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnUpgradeFail(error);
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
		params.addParameter("data", exception);
		x.http().post(params, new APICallback() {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				apiInterface.returnUploadExceptionFail(new String(""));
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
				apiInterface.returnUploadExceptionFail(error);
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
		x.http().request(HttpMethod.GET, params, new APICallback() {
			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {
					
					@Override
					public void execute() {
						getAppTabs();
					}
				}, context).refreshTocken(completeUrl);
			}
			
			@Override
			public void callbackSuccess(String arg0) {
				LogUtils.debug("yfcLog", "返回内容："+arg0);
				apiInterface.returnGetAppTabsSuccess(new GetAppTabsResult(arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnAddAppFail(error);
			}
		});
	}
	
	public void uploadCollect(String collectInfo){
		apiInterface.returnUploadCollectSuccess();
	}
}
