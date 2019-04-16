package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.CloudHttpMethod;
import com.inspur.emmcloud.api.HttpUtils;
import com.inspur.emmcloud.bean.schedule.GetScheduleListResult;
import com.inspur.emmcloud.bean.schedule.meeting.Building;
import com.inspur.emmcloud.bean.schedule.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetOfficeListResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.work.GetAddOfficeResult;
import com.inspur.emmcloud.bean.work.GetLocationResult;
import com.inspur.emmcloud.bean.work.GetMeetingListResult;
import com.inspur.emmcloud.bean.work.GetMeetingRoomListResult;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.http.app.RequestTracker;
import org.xutils.http.request.UriRequest;

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


    /**
     * 删除会议
     *
     * @param meeting
     */
    public void deleteMeeting(final Meeting meeting) {
        final String completeUrl = APIUri.getMeetingDeleteUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("rid",meeting.getId());
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteMeeting(meeting);
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
                apiInterface.returnDeleteMeetingSuccess(meeting);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteMeetingFail(error, responseCode);
            }
        });
    }


    /**
     * 获取对应room的会议情况
     *
     * @param meetingRoomId
     */
    public void getRoomMeetingListByMeetingRoom(final String meetingRoomId) {
        final String completeUrl = APIUri.getRoomMeetingListUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.addParameter("bid", meetingRoomId);
        params.setRequestTracker(new RequestTracker() {

            @Override
            public void onWaiting(RequestParams arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(UriRequest arg0, Object arg1) {
                // TODO Auto-generated method stub
                String result = "";
                if (arg1 != null) {
                    result = new String((byte[]) arg1);
                }
                apiInterface
                        .returnMeetingListSuccess(new GetMeetingListResult(result), arg0.getResponseHeader("Date"));
            }

            @Override
            public void onStart(RequestParams arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onRequestCreated(UriRequest arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFinished(UriRequest arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(UriRequest arg0, Throwable arg1, boolean arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onCancelled(UriRequest arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onCache(UriRequest arg0, Object arg1) {
                // TODO Auto-generated method stub

            }
        });
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getRoomMeetingListByMeetingRoom(meetingRoomId);
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
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingListFail(error, responseCode);
            }
        });
    }

    /**
     * 获取是否会议室管理员
     *
     * @param uid
     */
    public void getIsMeetingAdmin(final String uid) {
        final String completeUrl = APIUri.getMeetingIsAdminUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("cid",uid);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getIsMeetingAdmin(uid);
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
                apiInterface.returnIsMeetingAdminSuccess(new GetIsMeetingAdminResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnIsMeetingAdminFail(error, responseCode);
            }
        });
    }

    /**
     * 获取园区
     */
    public void getMeetingLoction() {
        final String completeUrl = APIUri.getLoctionUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingLoction();
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
                apiInterface.returnLocationResultSuccess(new GetLocationResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnLocationResultFail(error, responseCode);
            }
        });

    }


    /**
     * 添加常用办公地点
     * @param building
     */
    public void addMeetingOffice(final Building building) {
        final String completeUrl = APIUri.addOfficeUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        JSONObject jsonBuild = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonBuild.put("id", building.getId());
            jsonObject.put("name", building.getName());
            jsonObject.put("building", jsonBuild);
            params.setBodyContent(jsonObject.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpUtils.request(context, CloudHttpMethod.POST, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addMeetingOffice(building);
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
                apiInterface.returnAddMeetingOfficeSuccess(new GetAddOfficeResult(new String(arg0)),building);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAddMeetingOfficeFail(error, responseCode);
            }
        });
    }


    /**
     * 删除常用办公地点
     *
     * @param building
     */
    public void deleteMeetingOffice(final Building building) {
        final String completeUrl = APIUri.addOfficeUrl() + "?id=" + building.getId();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteMeetingOffice(building);
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
                apiInterface.returnDeleteOfficeSuccess(building);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteOfficeFail(error, responseCode);
            }
        });

    }
}
