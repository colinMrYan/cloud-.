package com.inspur.emmcloud.schedule.api;

import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.schedule.bean.GetIDResult;
import com.inspur.emmcloud.schedule.bean.GetScheduleListResult;
import com.inspur.emmcloud.schedule.bean.Schedule;
import com.inspur.emmcloud.schedule.bean.calendar.AccountType;
import com.inspur.emmcloud.schedule.bean.calendar.GetMyCalendarResult;
import com.inspur.emmcloud.schedule.bean.calendar.GetScheduleBasicDataResult;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;
import com.inspur.emmcloud.schedule.bean.meeting.Building;
import com.inspur.emmcloud.schedule.bean.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.schedule.bean.meeting.GetLocationResult;
import com.inspur.emmcloud.schedule.bean.meeting.GetMeetingListResult;
import com.inspur.emmcloud.schedule.bean.meeting.GetMeetingRoomListResult;
import com.inspur.emmcloud.schedule.bean.meeting.GetTagResult;
import com.inspur.emmcloud.schedule.bean.meeting.Meeting;
import com.inspur.emmcloud.schedule.bean.meeting.Office;
import com.inspur.emmcloud.schedule.bean.task.Attachment;
import com.inspur.emmcloud.schedule.bean.task.GetTaskAddResult;
import com.inspur.emmcloud.schedule.bean.task.GetTaskListResult;
import com.inspur.emmcloud.schedule.util.CalendarUtils;
import com.inspur.emmcloud.schedule.util.ScheduleCalendarCacheUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.Calendar;

/**
 * Created by libaochao on 2019/12/10.
 */

public class ScheduleAPIService {
    private Context context;
    private ScheduleAPIInterface apiInterface;

    public ScheduleAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(ScheduleAPIInterface apiInterface) {
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
        final String completeUrl = ScheduleAPIUri.getAddScheduleUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
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
     * 获取日程列表（日程和会议）
     *
     * @param startTime
     * @param endTime
     */
    public void getScheduleList(final Calendar startTime, final Calendar endTime, final ScheduleCalendar scheduleCalendar) {
        boolean isExchange = scheduleCalendar != null && scheduleCalendar.getAcType().equals(AccountType.EXCHANGE.toString());
        final String url = ScheduleAPIUri.getScheduleListUrl(scheduleCalendar);
        RequestParams params = null;
        if (isExchange) {
            params = BaseApplication.getInstance().getHttpRequestParams(url, CalendarUtils.getHttpHeaderExtraKey(scheduleCalendar), CalendarUtils.getHttpHeaderExtraValue(scheduleCalendar));
        } else {
            params = BaseApplication.getInstance().getHttpRequestParams(url);
            params.addQueryStringParameter("calendarLastTime", "0");
            params.addQueryStringParameter("meetingLastTime", "0");
            params.addQueryStringParameter("taskLastTime", "0");
        }
        params.addQueryStringParameter("startTime", startTime.getTimeInMillis() + "");
        params.addQueryStringParameter("endTime", endTime.getTimeInMillis() + "");

        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnScheduleListSuccess(new GetScheduleListResult(new String(arg0), scheduleCalendar),
                        startTime, endTime, scheduleCalendar);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnScheduleListFail(error, responseCode, scheduleCalendar);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getScheduleList(startTime, endTime, scheduleCalendar);
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
     * 删除日程
     *
     * @param schedule
     */
    public void deleteSchedule(final Schedule schedule) {
        ScheduleCalendar scheduleCalendar = ScheduleCalendarCacheUtils.getScheduleCalendar(BaseApplication.getInstance(), schedule.getScheduleCalendar());
        boolean isExchange = scheduleCalendar != null && scheduleCalendar.getAcType().equals(AccountType.EXCHANGE.toString());
        final String completeUrl = ScheduleAPIUri.getDeleteScheduleUrl(scheduleCalendar, schedule);
        RequestParams params = null;
        if (isExchange) {
            params = BaseApplication.getInstance()
                    .getHttpRequestParams(completeUrl, CalendarUtils.getHttpHeaderExtraKey(scheduleCalendar), CalendarUtils.getHttpHeaderExtraValue(scheduleCalendar));
            JSONObject object = new JSONObject();
            try {
                object.put("id", schedule.getId());
                object.put("isMeeting", schedule.isMeeting());
            } catch (Exception e) {
                e.printStackTrace();
            }
            params.setBodyContent(object.toString());
            params.setAsJsonContent(true);
        } else {
            params = BaseApplication.getInstance()
                    .getHttpRequestParams(completeUrl);
        }

        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteSchedule(schedule);
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
                apiInterface.returnDeleteScheduleSuccess(schedule.getId());
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnDeleteScheduleFail(error, responseCode);
            }
        });
    }

//    /**
//     * 获取常用办公地点
//     */
//    public void getOfficeList() {
//        final String completeUrl = APIUri.getOfficeUrl();
//        RequestParams params = MyApplication.getInstance()
//                .getHttpRequestParams(completeUrl,true);
//        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
//
//            @Override
//            public void callbackSuccess(byte[] arg0) {
//                // TODO Auto-generated method stub
//                apiInterface.returnOfficeListResultSuccess(new GetOfficeListResult(new String(arg0)));
//            }
//
//            @Override
//            public void callbackFail(String error, int responseCode) {
//                // TODO Auto-generated method stub
//                apiInterface.returnOfficeListResultFail(error, responseCode);
//            }
//
//            @Override
//            public void callbackTokenExpire(long requestTime) {
//                OauthCallBack oauthCallBack = new OauthCallBack() {
//                    @Override
//                    public void reExecute() {
//                        getOfficeList();
//                    }
//
//                    @Override
//                    public void executeFailCallback() {
//                        callbackFail("", -1);
//                    }
//                };
//                refreshToken(oauthCallBack, requestTime);
//            }
//
//        });
//    }


    /**
     * 获取会议室列表
     */
    public void getMeetingRoomList() {
        final String completeUrl = ScheduleAPIUri.getMeetingRoomsUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
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
        String baseUrl = ScheduleAPIUri.getSetCalendarBindChatUrl();
        final String completeUrl = baseUrl;
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addParameter("calendarId", calendarId);
        params.addParameter("chatId", chatId);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

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
        String baseUrl = ScheduleAPIUri.getCalendarBindChatUrl(calendarId);
        final String completeUrl = baseUrl;
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
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
                apiInterface.returnGetCalendarChatBindSuccess(calendarId, new String(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnGetCalendarChatBindFail(error, responseCode);
            }
        });
    }

    /**
     * 会议详情页  参会状态
     */
    public void setMeetingAttendStatus(final Schedule schedule, final String responseType) {
        ScheduleCalendar scheduleCalendar = ScheduleCalendarCacheUtils.getScheduleCalendar(BaseApplication.getInstance(), schedule.getScheduleCalendar());
        final String completeUrl = ScheduleAPIUri.getMeetingAttendStatusUrl(responseType, scheduleCalendar) + schedule.getId();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl, CalendarUtils.getHttpHeaderExtraKey(scheduleCalendar), CalendarUtils.getHttpHeaderExtraValue(scheduleCalendar));
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnAttendMeetingStatusSuccess(new String(arg0), responseType);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnAttendMeetingStatusFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        setMeetingAttendStatus(schedule, responseType);
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
     * 获取我的任务
     *
     * @param orderBy
     * @param orderType
     */
    public void getMineTasks(final String orderBy, final String orderType) {
        final String completeUrl = ScheduleAPIUri.getToDoRecentUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
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
        final String completeUrl = ScheduleAPIUri.getFocusedTasksUrl();
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getInvolvedTasksUrl();
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getCreateTaskUrl();
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getCreateTaskUrl() + "/" + id;
        RequestParams params = BaseApplication.getInstance()
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
    public void deleteMeeting(final Schedule meeting) {
        final String completeUrl = ScheduleAPIUri.getDelMeetingUrl(meeting.getId());
        RequestParams params = BaseApplication.getInstance()
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
    public void getMeetingDataFromId(final String id, final ScheduleCalendar scheduleCalendar) {
        final String completeUrl = ScheduleAPIUri.getMeetingUrlFromId(id, scheduleCalendar);
        String headerExtraKey = CalendarUtils.getHttpHeaderExtraKey(scheduleCalendar);
        String headerExtraValue = CalendarUtils.getHttpHeaderExtraValue(scheduleCalendar);
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl, headerExtraKey, headerExtraValue);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingDataFromId(id, scheduleCalendar);
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
        final String completeUrl = ScheduleAPIUri.getCalendarUrlFromId(id);
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getRoomMeetingListByMeetingRoom();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
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
        final String completeUrl = ScheduleAPIUri.getMeetingIsAdminUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
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
        final String completeUrl = ScheduleAPIUri.getLocationUrl();
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.addOfficeUrl();
        RequestParams params = BaseApplication.getInstance()
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


//    /**
//     * 删除常用办公地点
//     *
//     * @param office
//     */
//    public void deleteMeetingOffice(final Office office) {
//        final String completeUrl = APIUri.addOfficeUrl() + "/" + office.getId();
//        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
//        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, completeUrl) {
//
//            @Override
//            public void callbackTokenExpire(long requestTime) {
//                OauthCallBack oauthCallBack = new OauthCallBack() {
//                    @Override
//                    public void reExecute() {
//                        deleteMeetingOffice(office);
//                    }
//
//                    @Override
//                    public void executeFailCallback() {
//                        callbackFail("", -1);
//                    }
//                };
//                refreshToken(
//                        oauthCallBack, requestTime);
//            }
//
//            @Override
//            public void callbackSuccess(byte[] arg0) {
//                // TODO Auto-generated method stub
//                apiInterface.returnDeleteOfficeSuccess(office);
//            }
//
//            @Override
//            public void callbackFail(String error, int responseCode) {
//                // TODO Auto-generated method stub
//                apiInterface.returnDeleteOfficeFail(error, responseCode);
//            }
//        });
//
//    }


    /**
     * 设置常用会议地点
     */
    public void setMeetingCommonBuilding(final Building building) {
        final String completeUrl = ScheduleAPIUri.addOfficeUrl();
        RequestParams params = BaseApplication.getInstance()
                .getHttpRequestParams(completeUrl);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", building.getName());
            jsonObject.put("id", building.getId());
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
        final String completeUrl = ScheduleAPIUri.addOfficeUrl() + "/" + building.getId();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
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
        final String completeUrl = ScheduleAPIUri.getAddMeetingUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
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
        final String completeUrl = ScheduleAPIUri.getMeetingUpdateUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
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
    public void getMeetingListByTime(final long startTime, final ScheduleCalendar scheduleCalendar) {
        final String completeUrl = ScheduleAPIUri.getMeetingListByStartTime(scheduleCalendar);
        RequestParams params = BaseApplication.getInstance()
                .getHttpRequestParams(completeUrl, CalendarUtils.getHttpHeaderExtraKey(scheduleCalendar), CalendarUtils.getHttpHeaderExtraValue(scheduleCalendar));
        params.addQueryStringParameter("startTime", startTime + "");
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMeetingListByTime(startTime, scheduleCalendar);
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
                apiInterface.returnMeetingListSuccess(new GetMeetingListResult(new String(arg0), scheduleCalendar));
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
        final String completeUrl = ScheduleAPIUri.getMeetingHistoryListByPage(pageNum);
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
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
     * 创建任务
     */
    public void createTasks(final String mession) {
        final String completeUrl = ScheduleAPIUri.getCreateTaskUrl();
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getCreateTaskUrl();
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getTasksList(id);
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getCreateTaskUrl() + "/" + taskId
                + "/mates";
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getCreateTaskUrl() + "/" + taskId
                + "/mates";
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getTagUrl();
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getTagUrl();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("title", title);
            jsonObject.put("color", color);
            jsonObject.put("owner", owner);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getTagUrl() + "/" + id;
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getTagUrl();
        RequestParams params = BaseApplication.getInstance()
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

        final String completeUrl = ScheduleAPIUri.getChangeMessionOwnerUrl() + id
                + "?";
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getAddAttachmentsUrl(id);
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getAddAttachmentsUrl(id);
        RequestParams params = BaseApplication.getInstance()
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
        final String completeUrl = ScheduleAPIUri.getCalendarUrl() + "/calendar";
        RequestParams params = BaseApplication.getInstance()
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
    public void deleteTaskTags(final String taskId, final String tagsIdJSON) {
        final String completeUrl = ScheduleAPIUri.getDelTaskTagsUrl(taskId);
        RequestParams params = ((BaseApplication) context.getApplicationContext())
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
        final String completeUrl = ScheduleAPIUri.getAddTaskTagsUrl(taskId);
        RequestParams params = ((BaseApplication) context.getApplicationContext())
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

    public void getScheduleBasicData(final int year, final String version) {
        final String completeUrl = ScheduleAPIUri.getScheduleBasicDataUrl();
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addQueryStringParameter("year", year + "");
        params.addQueryStringParameter("version", version);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getScheduleBasicData(year, version);
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
                apiInterface.returnScheduleBasicDataSuccess(new GetScheduleBasicDataResult(new String(arg0), year));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnScheduleBasicDataFail(error, responseCode);
            }
        });
    }


    /**
     * 添加日程
     *
     * @param schedule
     * @param scheduleCalendar
     */
    public void addSchedule(final String schedule, final ScheduleCalendar scheduleCalendar) {
        final String completeUrl = ScheduleAPIUri.getAddScheduleUrl(scheduleCalendar);
        String headerExtraKey = CalendarUtils.getHttpHeaderExtraKey(scheduleCalendar);
        String headerExtraValue = CalendarUtils.getHttpHeaderExtraValue(scheduleCalendar);
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl, headerExtraKey, headerExtraValue);
        params.setBodyContent(schedule);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addSchedule(schedule, scheduleCalendar);
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
    public void updateSchedule(final String scheduleJson, final Schedule schedule) {
        ScheduleCalendar scheduleCalendar = ScheduleCalendarCacheUtils.getScheduleCalendar(context, schedule.getScheduleCalendar());
        final String completeUrl = ScheduleAPIUri.getUpdateScheduleUrl(scheduleCalendar, schedule.isMeeting());
        String headerExtraKey = CalendarUtils.getHttpHeaderExtraKey(scheduleCalendar);
        String headerExtraValue = CalendarUtils.getHttpHeaderExtraValue(scheduleCalendar);
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl, headerExtraKey, headerExtraValue);
        params.setBodyContent(scheduleJson);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateSchedule(scheduleJson, schedule);
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
}
