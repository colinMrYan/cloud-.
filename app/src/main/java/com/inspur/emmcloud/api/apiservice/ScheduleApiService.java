package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.bean.appcenter.GetIDResult;
import com.inspur.emmcloud.bean.schedule.GetScheduleListResult;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.calendar.GetHolidayDataResult;
import com.inspur.emmcloud.bean.schedule.calendar.GetMyCalendarResult;
import com.inspur.emmcloud.bean.schedule.meeting.Building;
import com.inspur.emmcloud.bean.schedule.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetLocationResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetMeetingListResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetMeetingRoomListResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetOfficeListResult;
import com.inspur.emmcloud.bean.schedule.meeting.GetTagResult;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.bean.schedule.meeting.Office;
import com.inspur.emmcloud.bean.schedule.task.Attachment;
import com.inspur.emmcloud.bean.schedule.task.GetTaskAddResult;
import com.inspur.emmcloud.bean.schedule.task.GetTaskListResult;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    private void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.refreshToken(oauthCallBack, requestTime);
        }
    }

    /**
     * 添加日程
     */
    public void addSchedule(final String schedule) {
        final String completeUrl = APIUri.getAddScheduleUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(schedule);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addSchedule(schedule);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAddScheduleSuccess(new GetIDResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAddScheduleFail(error, responseCode);
            }
        });
    }

    /**
     * 更新日程
     */
    public void updateSchedule(final String schedule) {
        final String completeUrl = APIUri.getUpdateScheduleUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(schedule);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateSchedule(schedule);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateScheduleSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateScheduleFail(error, responseCode);
            }
        });
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
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnScheduleListSuccess(new GetScheduleListResult(new String(arg0)),
                        startTime, endTime, calendarIdList, meetingIdList, taskIdList);
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
                                taskLastTime, calendarIdList, meetingIdList, taskIdList);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }

    /**
     * 根据schedule Id 删除schedule
     */
    public void deleteSchedule(final String scheduleId) {
        final String completeUrl = APIUri.getDeleteScheduleUrl(scheduleId);
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(scheduleId);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteSchedule(scheduleId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteScheduleSuccess(scheduleId);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteScheduleFail(error, responseCode);
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
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

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
                refreshToken(oauthCallBack, requestTime);
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
    public void getMeetingRoomList(final long start, final long end, final List<String> officeIdList,
                                   final boolean isFilter) {
        String baseUrl = APIUri.getMeetingRoomsUrl() + "?";
        if (isFilter) {
            baseUrl = baseUrl + "start=" + start + "&end=" + end;
        } else {
            baseUrl = baseUrl + "start=" + "&end=";
        }

        final String completeUrl = baseUrl;
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingRoomList(start, end, officeIdList, isFilter);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingRoomListSuccess(new GetMeetingRoomListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingRoomListFail(error, responseCode);
            }
        });
    }


    /**
     * 获取会议室列表
     */
    public void getMeetingRoomList() {
        String baseUrl = APIUri.getMeetingRoomsUrl();
        final String completeUrl = baseUrl;
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingRoomList();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingRoomListSuccess(new GetMeetingRoomListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingRoomListFail(error, responseCode);
            }
        });
    }

    /**
     * 设置群聊id
     *
     * @param calendarId 日程id
     * @param chatId     群组id
     */
    public void setCalendarBindChat(final String calendarId, final String chatId) {
        String baseUrl = APIUri.getSetCalendarBindChatUrl();
        final String completeUrl = baseUrl;
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        setCalendarBindChat(calendarId, chatId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnSetCalendarChatBindSuccess(calendarId, chatId);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnSetCalendarChatBindFail(error, responseCode);
            }
        });
    }

    /**
     * 获取群组ID*/
    /**
     * 设置群聊id
     *
     * @param calendarId 日程id
     */
    public void getCalendarBindChat(final String calendarId) {
        String baseUrl = APIUri.getSetCalendarBindChatUrl();
        final String completeUrl = baseUrl;
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getCalendarBindChat(calendarId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnGetCalendarChatBindSuccess(calendarId, arg0.toString());
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnGetCalendarChatBindFail(error, responseCode);
            }
        });
    }


    /**
     * 获取我的任务
     *
     * @param orderBy
     * @param orderType
     */
    public void getMineTasks(final String orderBy, final String orderType) {
        final String completeUrl = APIUri.getToDoRecentUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addParameter("order_by", orderBy);
        params.addParameter("order_type", orderType);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMineTasks(orderBy, orderType);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksSuccess(new GetTaskListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksFail(error, responseCode);
            }
        });
    }

    /**
     * 获取我关注的任务
     */
    public void getFocusedTasks(final String orderBy, final String orderType) {
        final String completeUrl = APIUri.getFocusedTasksUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.addParameter("order_by", orderBy);
        params.addParameter("order_type", orderType);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getFocusedTasks(orderBy, orderType);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksSuccess(new GetTaskListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksFail(error, responseCode);
            }
        });
    }

    /**
     * 获取我参与的任务
     */
    public void getInvolvedTasks(final String orderBy, final String orderType) {
        final String completeUrl = APIUri.getInvolvedTasksUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.addParameter("order_by", orderBy);
        params.addParameter("order_type", orderType);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getInvolvedTasks(orderBy, orderType);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksSuccess(new GetTaskListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksFail(error, responseCode);
            }
        });
    }

    /**
     * 获取已完成任务（带分页）
     *
     * @param page
     * @param limit
     * @param state
     */
    public void getFinishTasks(final int page, final int limit, final String state) {
        final String completeUrl = APIUri.getCreateTaskUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.addHeader("Accept", "application/json");
        params.addParameter("page", page);
        params.addParameter("limit", limit);
        params.addParameter("state", state);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getFinishTasks(page, limit, state);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksSuccess(new GetTaskListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnRecentTasksFail(error, responseCode);
            }
        });
    }

    /**
     * 删除任务
     *
     * @param id
     */
    public void setTaskFinishById(final String id) {
        final String completeUrl = APIUri.getCreateTaskUrl() + "/" + id;
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        setTaskFinishById(id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteTaskSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteTaskFail(error, responseCode);
            }
        });
    }


    /**
     * 删除会议
     *
     * @param meeting
     */
    public void deleteMeeting(final Meeting meeting) {
        final String completeUrl = APIUri.getDelMeetingUrl(meeting.getId());
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(meeting.getId());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

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
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDelMeetingSuccess(meeting);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDelMeetingFail(error, responseCode);
            }
        });
    }

    /**
     * 通过id获取会议数据  zyj
     *
     * @param id
     */
    public void getMeetingDataFromId(final String id) {
        final String completeUrl = APIUri.getMeetingUrlFromId(id);
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingDataFromId(id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                JSONObject object = JSONUtils.getJSONObject(new String(arg0));
                apiInterface.returnMeetingDataFromIdSuccess(object.length() > 0 ?
                        new Meeting(object) : null);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnMeetingDataFromIdFail(error, responseCode);
            }
        });
    }

    /**
     * 通过id获取日程数据  zyj
     *
     * @param id
     */
    public void getCalendarDataFromId(final String id) {
        final String completeUrl = APIUri.getCalendarUrlFromId(id);
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getCalendarDataFromId(id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                JSONObject object = JSONUtils.getJSONObject(new String(arg0));
                apiInterface.returnScheduleDataFromIdSuccess(object.length() > 0 ?
                        new Schedule(object) : null);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnScheduleDataFromIdFail(error, responseCode);
            }
        });
    }

    /**
     * 获取对应room的会议情况
     *
     * @param roomId
     * @param startTime
     * @param endTime
     */
    public void getRoomMeetingListByMeetingRoom(final String roomId, final long startTime, final long endTime) {
        final String completeUrl = APIUri.getRoomMeetingListByMeetingRoom();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("roomId", roomId);
        params.addQueryStringParameter("startTime", startTime + "");
        params.addQueryStringParameter("endTime", endTime + "");
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getRoomMeetingListByMeetingRoom(roomId, startTime, endTime);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingListSuccess(new GetMeetingListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingListByMeetingRoomFail(error, responseCode);
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
        params.addQueryStringParameter("cid", uid);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

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
                refreshToken(
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
    public void getMeetingLocation() {
        final String completeUrl = APIUri.getLocationUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingLocation();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
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
     *
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
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

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
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAddMeetingOfficeSuccess(new Office(new String(arg0)), building);
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
     * @param office
     */
    public void deleteMeetingOffice(final Office office) {
        final String completeUrl = APIUri.addOfficeUrl() + "/" + office.getId();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteMeetingOffice(office);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteOfficeSuccess(office);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteOfficeFail(error, responseCode);
            }
        });

    }


    /**
     * 设置常用会议点点
     */
    public void setMeetingCommonBuilding(final Building building) {
        final String completeUrl = APIUri.addOfficeUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", building.getId());
            jsonObject.put("name", building.getName());
            params.setBodyContent(jsonObject.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

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
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                LogUtils.LbcDebug("returnSetMeetingCommonBuildingSuccess::" + arg0.toString());
                apiInterface.returnSetMeetingCommonBuildingSuccess(building);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnSetMeetingCommonBuildingFail(error, responseCode);
            }
        });
    }

    /**
     * 取消会议常用地点
     */
    public void cancelMeetingCommonBuilding(final Building building) {
        final String completeUrl = APIUri.addOfficeUrl() + "/" + building.getId();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        cancelMeetingCommonBuilding(building);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                LogUtils.LbcDebug("returnCancelMeetingCommonBuildingSuccess::" + arg0.toString());
                apiInterface.returnCancelMeetingCommonBuildingSuccess(building);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCancelMeetingCommonBuildingFail(error, responseCode);
            }
        });
    }

    /**
     * 添加会议室
     *
     * @param meetingJson
     */
    public void addMeeting(final String meetingJson) {
        final String completeUrl = APIUri.getAddMeetingUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.setBodyContent(meetingJson);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addMeeting(meetingJson);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAddMeetingSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAddMeetingFail(error, responseCode);
            }
        });
    }

    public void updateMeeting(final String meetingJson) {
        final String completeUrl = APIUri.getMeetingUpdateUrl();
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.setBodyContent(meetingJson);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addMeeting(meetingJson);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateMeetingSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateMeetingFail(error, responseCode);
            }
        });

    }

    /**
     * 通过开始时间获取会议
     *
     * @param startTime
     */
    public void getMeetingListByTime(final long startTime) {
        final String completeUrl = APIUri.getMeetingListByStartTime();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("startTime", startTime + "");
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingListByTime(startTime);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingListSuccess(new GetMeetingListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingListFail(error, responseCode);
            }
        });
    }

    /**
     * 获取会议历史列表
     *
     * @param pageNum
     */
    public void getMeetingHistoryListByPage(final int pageNum) {
        final String completeUrl = APIUri.getMeetingHistoryListByPage(pageNum);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingHistoryListByPage(pageNum);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingHistoryListSuccess(new GetMeetingListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMeetingHistoryListFail(error, responseCode);
            }
        });
    }

    /**
     * 获取节假日信息
     * @param year
     */
    public void getHolidayData(final int year) {
        final String completeUrl = APIUri.getHolidayDataUrl() + year;
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getHolidayData(year);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnHolidayDataSuccess(new GetHolidayDataResult(new String(arg0),year));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnHolidayDataFail(error, responseCode);
            }
        });
    }

    /**
     * 创建任务
     */
    public void createTasks(final String mession) {
        final String completeUrl = APIUri.getCreateTaskUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.addParameter("title", mession);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createTasks(mession);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnCreateTaskSuccess(new GetTaskAddResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateTaskFail(error, responseCode);
            }
        });

    }

    /**
     * 更新待办任务
     *
     * @param taskJson
     */
    public void updateTask(final String taskJson, final int position) {
        final String completeUrl = APIUri.getCreateTaskUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(taskJson);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateTask(taskJson, position);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateTaskSuccess(position);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnUpdateTaskFail(error, responseCode, position);
            }
        });
    }

    /**
     * 获取task
     *
     * @param id
     */
    public void getTask(final String id) {
        final String completeUrl = APIUri.getTasksList(id);
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getTask(id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnGetTasksSuccess(new GetTaskListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnGetTasksFail(error, responseCode);
            }
        });
    }


    /**
     * 邀请多人参与协作
     *
     * @param taskId
     * @param uidArray
     */
    public void inviteMateForTask(final String taskId, final JSONArray uidArray) {
        final String completeUrl = APIUri.getCreateTaskUrl() + "/" + taskId
                + "/mates";
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(uidArray.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        inviteMateForTask(taskId, uidArray);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub

                apiInterface.returnInviteMateForTaskSuccess(new String(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnInviteMateForTaskFail(error, responseCode);
            }
        });
    }

    /**
     * 删除参与人
     *
     * @param taskId
     * @param uidArray
     */
    public void deleteMateForTask(final String taskId, final JSONArray uidArray) {
        final String completeUrl = APIUri.getCreateTaskUrl() + "/" + taskId
                + "/mates";
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(uidArray.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteMateForTask(taskId, uidArray);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDelTaskMemSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDelTaskMemFail(error, responseCode);
            }
        });
    }

    /**
     * 获取所有标签
     */
    public void getTags() {
        final String completeUrl = APIUri.getTagUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.addHeader("Accept", "application/json");
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getTags();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnGetTagResultSuccess(new GetTagResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnGetTagResultFail(error, responseCode);
            }
        });

    }

    /**
     * 修改标签
     *
     * @param id
     * @param title
     * @param color
     * @param owner
     */
    public void changeTag(final String id, final String title,
                          final String color, final String owner) {
        final String completeUrl = APIUri.getTagUrl();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("title", title);
            jsonObject.put("color", color);
            jsonObject.put("owner", owner);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(jsonObject.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        changeTag(id, title, color, owner);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateTagSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateTagFail(error, responseCode);
            }
        });

    }

    /**
     * 删除标签
     *
     * @param id
     */
    public void deleteTag(final String id) {
        final String completeUrl = APIUri.getTagUrl() + "/" + id;
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);

        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteTag(id);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteTagSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteTagFail(error, responseCode);
            }
        });

    }

    /**
     * 创建标签
     *
     * @param title
     * @param color
     */
    public void createTag(final String title, final String color) {
        final String completeUrl = APIUri.getTagUrl();
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.addParameter("title", title);
        params.addParameter("color", color);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createTag(title, color);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateTagSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnCreateTagFail(error, responseCode);
            }
        });
    }

    /**
     * 修改任务所有人
     *
     * @param id
     * @param newOwner
     */
    public void changeMessionOwner(final String id, final String newOwner, final String managerName) {

        final String completeUrl = APIUri.getChangeMessionOwnerUrl() + id
                + "?";
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("owner", newOwner);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        changeMessionOwner(id, newOwner, managerName);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnChangeMessionOwnerSuccess(managerName);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnChangeMessionOwnerFail(error, responseCode);
            }
        });

    }


    /**
     * 创建附件
     *
     * @param id
     * @param attachments
     */
    public void addAttachments(final String id, final String attachments) {
        final String completeUrl = APIUri.getAddAttachmentsUrl(id);
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        try {
            JSONObject jsonObject = new JSONObject(attachments);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(jsonObject);
            params.setBodyContent(jsonArray.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addAttachments(id, attachments);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAddAttachMentSuccess(new Attachment(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAddAttachMentFail(error, responseCode);
            }
        });
    }

    /**
     * 删除附件
     *
     * @param id
     * @param attachments
     */
    public void deleteAttachments(final String id, final String attachments, final int position) {
        final String completeUrl = APIUri.getAddAttachmentsUrl(id);
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        try {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(attachments);
            params.setBodyContent(jsonArray.toString());
            params.setAsJsonContent(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteAttachments(id, attachments, position);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDelAttachmentSuccess(position);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDelAttachmentFail(error, responseCode, position);
            }
        });
    }


    /****************************************日历部分********************************************************************************/

    /**
     * 获取我的所有日历
     *
     * @param page
     * @param limit
     */
    public void getMyCalendar(final int page, final int limit) {
        final String completeUrl = APIUri.getCalendarUrl() + "/calendar";
        RequestParams params = MyApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        params.addParameter("page", page);
        params.addParameter("limit", limit);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMyCalendar(page, limit);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnMyCalendarSuccess(new GetMyCalendarResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnMyCalendarFail(error, responseCode);
            }
        });


    }


    /**
     * 删除任务中的标签
     **/
    public void deleteTaskTags(final String taskId,final String tagsIdJSON) {
        final String completeUrl = APIUri.getDelTaskTagsUrl(taskId);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(tagsIdJSON);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteTag(taskId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnDelTaskTagSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDelTaskTagFail(error, responseCode);
            }
        });
    }

    /**
     * 添加任务中的标签*
     */
    public void addTaskTags(final String taskId, final String tagsIdJSON) {
        final String completeUrl = APIUri.getAddTaskTagsUrl(taskId);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.setBodyContent(tagsIdJSON);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addTaskTags(taskId, tagsIdJSON);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAddTaskTagSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAddTaskTagFail(error, responseCode);
            }
        });
    }

}
