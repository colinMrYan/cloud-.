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
import com.inspur.emmcloud.bean.GetKnowledgeInfo;
import com.inspur.emmcloud.bean.GetTripArriveCity;
import com.inspur.emmcloud.bean.Trip;
import com.inspur.emmcloud.callback.OauthCallBack;
import com.inspur.emmcloud.util.OauthUtils;
import com.inspur.emmcloud.util.UriUtils;

import org.json.JSONArray;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

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
		final String completeUrl = UriUtils.getHttpApiUri("trip/simple/detail?trip_ticket=")+tripId;
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		x.http().get(params, new APICallback(context,completeUrl) {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						getTripInfo(tripId);
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
	 * 删除行程
	 * 
	 * @param tripId
	 */
	public void deleteTripByIds(final String tripId) {
		final String completeUrl = UriUtils
				.getHttpApiUri("trip/simple/states?state=DELETED");
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		JSONArray array = new JSONArray();
		array.put(tripId);
		params.setBodyContent(array.toString());
		params.setAsJsonContent(true);
		x.http().request(HttpMethod.PUT, params, new APICallback(context,completeUrl) {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						deleteTripByIds(tripId);
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
		final String completeUrl = UriUtils
				.getHttpApiUri("trip/simple/upload");
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.setBodyContent(ticketInfos);
		params.setAsJsonContent(true);
		x.http().post(params, new APICallback(context,completeUrl) {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						uploadTrainTicket(ticketInfos);
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
		final String completeUrl = UriUtils
				.getHttpApiUri("trip/simple/upload");
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.setBodyContent(ticketInfos);
		params.setAsJsonContent(true);
		x.http().request(HttpMethod.PUT, params, new APICallback(context,completeUrl) {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						updateTrainTicket(ticketInfos);
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
	 * 获取上次用户扫描的最近的行程信息：返回单个行程
	 */
	public void getLastUploadTrip() {
		final String completeUrl = UriUtils
				.getHttpApiUri("trip/simple/latest");
		RequestParams params = ((MyApplication)context.getApplicationContext()).getHttpRequestParams(completeUrl);
		params.addParameter("way", "TRAIN");
		params.addParameter("source", "SMS");
		x.http().get(params, new APICallback(context,completeUrl) {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						getLastUploadTrip();
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
				apiInterface.returnLastUploadTripSuccess(new Trip(arg0));
			}
			
			@Override
			public void callbackFail(String error, int responseCode) {
				// TODO Auto-generated method stub
				apiInterface.returnLastUploadTripFail(error,responseCode);
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
		x.http().get(params, new APICallback(context,completeUrl) {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						getArriveCity(station);
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
		x.http().get(params, new APICallback(context,completeUrl) {
			
			@Override
			public void callbackTokenExpire() {
				// TODO Auto-generated method stub
				new OauthUtils(new OauthCallBack() {

					@Override
					public void reExecute() {
						// TODO Auto-generated method stub
						getKnowledgeList();
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
