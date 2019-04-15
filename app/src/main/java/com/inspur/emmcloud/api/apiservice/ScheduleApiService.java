package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.CloudHttpMethod;
import com.inspur.emmcloud.api.HttpUtils;
import com.inspur.emmcloud.bean.schedule.GetScheduleListResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetOfficeListResult;
import com.inspur.emmcloud.bean.work.GetMeetingRoomListResult;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.xutils.http.RequestParams;

import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/15.
 */

public class ScheduleApiService {
    private Context context;
    private APIInterface apiInterface;

    public ScheduleApiService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    /**
     * 获取日程列表（日程和会议）
     *
     * @param startTime
     * @param endTime
     * @param taskLastTime
     */
    public void getScheduleList(final Calendar startTime, final Calendar endTime, final long calendarLastTime, final long meetingLastTime, final long taskLastTime, final List<String> calendarIdList, final List<String> meetingIdList, final List<String> taskIdList) {
        final String url = APIUri.getScheduleListUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        params.addQueryStringParameter("startTime", startTime.getTimeInMillis() + "");
        params.addQueryStringParameter("endTime", endTime.getTimeInMillis() + "");
        params.addQueryStringParameter("calendarLastTime", calendarLastTime + "");
        params.addQueryStringParameter("meetingLastTime", meetingLastTime + "");
        params.addQueryStringParameter("taskLastTime", taskLastTime + "");
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnScheduleListSuccess(new GetScheduleListResult(new String(arg0)),
                        startTime, endTime,calendarIdList,meetingIdList,taskIdList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnScheduleListFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getScheduleList(startTime, endTime, calendarLastTime, meetingLastTime,
                                taskLastTime,calendarIdList,meetingIdList,taskIdList);
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

    /**
     * 获取常用办公地点
     */
    public void getOfficeList() {
        final String completeUrl = APIUri.getOfficeUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnOfficeListResultSuccess(new GetOfficeListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnOfficeListResultFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getOfficeList();
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

    /**
     * 获取过滤后的会议室列表
     *
     * @param start
     * @param end
     * @param officeIdList
     * @param isFilter
     */
    public void getMeetingRoomList(final long start, final long end,
                                   final List<String> officeIdList, final boolean isFilter) {
        String baseUrl = APIUri.getMeetingRoomsUrl() + "?";
        if (isFilter) {
            baseUrl = baseUrl + "startWebSocket=" + start + "&end=" + end;
        } else {
            baseUrl = baseUrl + "startWebSocket=" + "&end=";
        }
        baseUrl = baseUrl + "&isIdle=" + isFilter;
        for (int j = 0; j < officeIdList.size(); j++) {
            baseUrl = baseUrl + "&oids=" + officeIdList.get(j);
        }
        if (officeIdList.size() == 0) {
            baseUrl = baseUrl + "&oids=";
        }
        final String completeUrl = baseUrl;
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingRoomList(start, end, officeIdList,
                                isFilter);
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
                apiInterface.returnMeetingRoomListSuccess(
                        new GetMeetingRoomListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingRoomListFail(error, responseCode);
            }
        });
    }

}
