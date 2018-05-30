/**
 * 
 * ContactAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.ContactAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:32:19
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.CloudHttpMethod;
import com.inspur.emmcloud.api.HttpUtils;
import com.inspur.emmcloud.bean.chat.GetAllRobotsResult;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.xutils.common.Callback;
import org.xutils.ex.HttpException;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

/**
 * com.inspur.emmcloud.api.apiservice.ContactAPIService
 * create at 2016年11月8日 下午2:32:19
 */
public class ContactAPIService {
	private Context context;
	private APIInterface apiInterface;
	public ContactAPIService(Context context) {
		this.context = context;
	}

	public void setAPIInterface(APIInterface apiInterface) {
		this.apiInterface = apiInterface;
	}
	
	
	/**
	 * 获取所有机器人信息
	 */
	public void getAllRobotInfo(){
		final String completeUrl = APIUri.getAllBotInfo();
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						getAllRobotInfo();
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
			public void callbackSuccess(String arg0) {
				apiInterface.returnAllRobotsSuccess(new GetAllRobotsResult(arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnAllRobotsFail(error,responseCode);
			}
		});
	}
	
	/**
	 * 通过id获取机器人信息
	 * @param id
	 */
	public void getRobotInfoById(final String id){
		final String completeUrl = APIUri.getBotInfoById()+id;
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						getRobotInfoById(id);
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
			public void callbackSuccess(String arg0) {
				apiInterface.returnRobotByIdSuccess(new Robot(arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnRobotByIdFail(error,responseCode);
			}
		});
	}

	public void getContactUserList(long lastQuetyTime){
		String url = APIUri.getContactUserUrl();
		RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
		if (lastQuetyTime != 0) {
			params.addParameter("lastQueryTime", lastQuetyTime);
		}
		x.http().post(params, new Callback.CommonCallback<byte[]>() {
			@Override
			public void onSuccess(byte[] bytes) {
				//LogUtils.jasonDebug("getContactUserList:"+new String(bytes));
				apiInterface.returnContactUserListSuccess(bytes);
			}

			@Override
			public void onError(Throwable arg0, boolean b) {

				apiInterface.returnContactUserListFail("",-1);
String error = "";
				if (arg0 instanceof TimeoutException || arg0 instanceof SocketTimeoutException) {
					error = "time out";
				} else if (arg0 instanceof UnknownHostException) {
					error = "time out";
				} else if (arg0 instanceof HttpException) {
					HttpException httpEx = (HttpException) arg0;
					error = httpEx.getResult();
				} else {
					error = arg0.toString();
				}
				LogUtils.jasonDebug("onError-----------------------------error="+error);
			}

			@Override
			public void onCancelled(CancelledException e) {

			}

			@Override
			public void onFinished() {

			}
		});
	}

	public void getContactOrgList(long lastQuetyTime){
		String url = APIUri.getContactOrgUrl();
		RequestParams params =  MyApplication.getInstance().getHttpRequestParams(url);
		if (lastQuetyTime != 0) {
			params.addParameter("lastQueryTime", lastQuetyTime);
		}
		x.http().post(params, new Callback.CommonCallback<byte[]>() {
			@Override
			public void onSuccess(byte[] bytes) {
			//	LogUtils.jasonDebug("getContactOrgList:"+new String(bytes));
				apiInterface.returnContactOrgListSuccess(bytes);
			}

			@Override
			public void onError(Throwable throwable, boolean b) {
				apiInterface.returnContactOrgListFail("",-1);
			}

			@Override
			public void onCancelled(CancelledException e) {

			}

			@Override
			public void onFinished() {

			}
		});
	}



}
