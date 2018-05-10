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
import com.inspur.emmcloud.bean.contact.GetAllContactResult;
import com.inspur.emmcloud.bean.contact.OrgsInfo;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.OauthUtils;
import com.inspur.emmcloud.util.privates.cache.DbCacheUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

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
		HttpUtils.request(context, CloudHttpMethod.POST,params,new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						getAllContact(lastQueryTime);
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
				// TODO Auto-generated method stub
				apiInterface.returnAllContactSuccess(new GetAllContactResult(
						arg0));
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnAllContactFail(error,responseCode);
			}
		});
		
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

	public void getContactOrgPart(){
		String url = "http://10.24.51.1:8080/api/sys/v4.0/contacts/orgs";
		RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
		x.http().post(params, new Callback.CommonCallback<byte[]>() {
			@Override
			public void onSuccess(byte[] bytes) {
				LogUtils.jasonDebug("onSuccess-----------------------------");
				try {
					LogUtils.jasonDebug("result="+new String(bytes));
					List<OrgsInfo.org> orgsList = OrgsInfo.orgs.parseFrom(bytes).getOrgsList();
					LogUtils.jasonDebug(orgsList.get(0).getId());
					LogUtils.jasonDebug(orgsList.get(0).getName());
					//LogUtils.jasonDebug(orgsList.get(0).getPinyin());
					DbCacheUtils.getDb(context).saveOrUpdate(orgsList);
					List<OrgsInfo.org> orgsList1 = DbCacheUtils.getDb(context).findAll(OrgsInfo.org.class);
					LogUtils.jasonDebug("orgsList1=="+orgsList1.get(0).getName());

				}catch (Exception e){
					e.printStackTrace();
				}
				byte[] btte = null;
				String s = new String(btte);

			}

			@Override
			public void onError(Throwable throwable, boolean b) {
				LogUtils.jasonDebug("onError-----------------------------");
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
