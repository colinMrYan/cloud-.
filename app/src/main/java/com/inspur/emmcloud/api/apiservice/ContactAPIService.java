/**
 * 
 * ContactAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.ContactAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:32:19
 */
package com.inspur.emmcloud.api.apiservice;

import org.xutils.x;
import org.xutils.http.RequestParams;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.GetAllContactResult;
import com.inspur.emmcloud.bean.GetAllRobotsResult;
import com.inspur.emmcloud.bean.Robot;
import com.inspur.emmcloud.util.OauthCallBack;
import com.inspur.emmcloud.util.OauthUtils;
import com.inspur.emmcloud.util.StringUtils;

/**
 * com.inspur.emmcloud.api.apiservice.ContactAPIService
 * create at 2016年11月8日 下午2:32:19
 */
public class ContactAPIService {
	private Context context;
	private APIInterface apiInterface;
	private String baseUrl = "https://emm.inspur.com/api?";
	public ContactAPIService(Context context) {
		this.context = context;
	}

	public void setAPIInterface(APIInterface apiInterface) {
		this.apiInterface = apiInterface;
	}
	
	
	/**
	 * 获取全部通讯录
	 * 
	 * @param lastQueryTime
	 */
	public void getAllContact(final String lastQueryTime) {
		final String completeUrl = APIUri.getAllContact();
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		if (!StringUtils.isBlank(lastQueryTime)) {
			params.addParameter("lastQueryTime", lastQueryTime);
		}
		x.http().post(params, new APICallback() {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void execute() {
						// TODO Auto-generated method stub
						getAllContact(lastQueryTime);
					}
				}, context).refreshTocken(completeUrl);
			}
			
			@Override
			public void callbackSuccess(String arg0) {
				// TODO Auto-generated method stub
				apiInterface.returnAllContactSuccess(new GetAllContactResult(
						arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnAllContactFail(error);
			}
		});
		
	}
	
	/**
	 * 获取所有机器人信息
	 */
	public void getAllRobotInfo(){
		final String completeUrl = APIUri.getAllBotInfo();
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback() {
			
			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {
					
					@Override
					public void execute() {
						getAllRobotInfo();
					}
				}, context).refreshTocken(completeUrl);
			}
			
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnAllRobotsSuccess(new GetAllRobotsResult(arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnAllRobotsFail(error);
			}
		});
	}
	
	/**
	 * 通过id获取机器人信息
	 * @param id
	 */
	public void getRobotInfoById(final String id){
		final String completeUrl = APIUri.getBotInfoById()+"/"+id;
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback() {
			
			@Override
			public void callbackTokenExpire() {
				new OauthUtils(new OauthCallBack() {
					
					@Override
					public void execute() {
						getRobotInfoById(id);
					}
				}, context).refreshTocken(completeUrl);
			}
			
			@Override
			public void callbackSuccess(String arg0) {
				apiInterface.returnRobotByIdSuccess(new Robot(arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				apiInterface.returnRobotByIdFail(error);
			}
		});
	}
}
