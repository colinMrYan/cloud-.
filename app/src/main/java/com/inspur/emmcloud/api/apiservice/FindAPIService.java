/**
 * 
 * FindAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.FindAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:34:33
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.CloudHttpMethod;
import com.inspur.emmcloud.api.HttpUtils;
import com.inspur.emmcloud.bean.find.GetKnowledgeInfo;
import com.inspur.emmcloud.bean.find.GetTripArriveCity;
import com.inspur.emmcloud.bean.find.Trip;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.json.JSONArray;
import org.xutils.http.RequestParams;

/**
 * com.inspur.emmcloud.api.apiservice.FindAPIService
 * create at 2016年11月8日 下午2:34:33
 */
public class FindAPIService {
	private Context context;
	private APIInterface apiInterface;
	public FindAPIService(Context context) {
		this.context = context;
	}

	public void setAPIInterface(APIInterface apiInterface) {
		this.apiInterface = apiInterface;
	}
	
	/**
	 * 获取行程信息
	 */
	public void getTripInfo(final String  tripId) {
		final String completeUrl = APIUri.getTripInfoUrl()+tripId;
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		HttpUtils.request(context, CloudHttpMethod.GET,params, new APICallback(context,completeUrl) {
			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						getTripInfo(tripId);
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
				apiInterface.returnTripSuccess(new Trip(arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnTripFail(error,responseCode);
			}
		});
	}

	/**
	 * 删除行程，180504已废弃？
	 * 
	 * @param tripId
	 */
	public void deleteTripByIds(final String tripId) {
		final String completeUrl = APIUri
				.getHttpApiUrl("trip/simple/states?state=DELETED");
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		JSONArray array = new JSONArray();
		array.put(tripId);
		params.setBodyContent(array.toString());
		params.setAsJsonContent(true);
		HttpUtils.request(context,CloudHttpMethod.PUT, params, new APICallback(context,completeUrl) {
			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						deleteTripByIds(tripId);
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
				apiInterface.returnDelTripSuccess();
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnDelTripFail(error,responseCode);
			}
		});

	}
	

	/**
	 * 上传最近一年的行程数据
	 *
	 * @param ticketInfos
	 */
	public void uploadTrainTicket(final String ticketInfos) {
		final String completeUrl = APIUri
				.getUpdateTripInfoUrl();
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.setBodyContent(ticketInfos);
		params.setAsJsonContent(true);
		HttpUtils.request(context, CloudHttpMethod.POST,params, new APICallback(context,completeUrl) {
			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						uploadTrainTicket(ticketInfos);
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
				apiInterface.returnUploadTrainTicketSuccess();
			}

			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnUploadTrainTicketFail(error,responseCode);
			}
		});
	}

	/**
	 * 更新行程数据
	 * 
	 * @param ticketInfos
	 */
	public void updateTrainTicket(final String ticketInfos) {
		final String completeUrl = APIUri
				.getUpdateTripInfoUrl();
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.setBodyContent(ticketInfos);
		params.setAsJsonContent(true);
		HttpUtils.request(context,CloudHttpMethod.PUT, params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						updateTrainTicket(ticketInfos);
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
				apiInterface.returnUploadTrainTicketSuccess();
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnUploadTrainTicketFail(error,responseCode);
			}
		});
	}

	/**
	 * 获取到达城市
	 *
	 * @param station
	 */
	public void getArriveCity(final String station) {
		final String completeUrl;
		completeUrl = APIUri.getTripArriveCityUrl() + "?";
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.addParameter("station", station);
		HttpUtils.request(context, CloudHttpMethod.GET,params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						getArriveCity(station);
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
				apiInterface.returnTripArriveSuccess(new GetTripArriveCity(
						arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.retrunTripArriveFail(error,responseCode);
			}
		});
	}
	
	/**
	 * 获取知识信息
	 */
	public void getKnowledgeList() {
		final String completeUrl = APIUri.getKnowledgeTipsUrl();
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		HttpUtils.request(context,CloudHttpMethod.GET,params, new APICallback(context,completeUrl) {

			@Override
			public void callbackTokenExpire(long requestTime) {
				OauthCallBack oauthCallBack = new OauthCallBack() {
					@Override
					public void reExecute() {
						getKnowledgeList();
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
				apiInterface.returnKnowledgeListSuccess(new GetKnowledgeInfo(
						arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnKnowledgeListFail(error,responseCode);
			}
		});
	}


}
