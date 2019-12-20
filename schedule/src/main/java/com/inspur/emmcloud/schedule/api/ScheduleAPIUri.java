package com.inspur.emmcloud.schedule.api;

import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.schedule.bean.Schedule;
import com.inspur.emmcloud.schedule.bean.calendar.AccountType;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;

/**
 * Created by libaochao on 2019/12/10.
 */

public class ScheduleAPIUri {

    /**
     * EcmSchedule服务
     *
     * @return
     */
    public static String getECMScheduleUrl() {
        return WebServiceRouterManager.getInstance().getClusterSchedule();
    }

    /**
     * 网络状态检测API
     * 固定地址
     *
     * @return
     */

    public static String getScheduleBaseUrl() {
        return getECMScheduleUrl() + "/schedule-ext/";
    }


    public static String getScheduleListUrl(ScheduleCalendar scheduleCalendar) {
        String url = getScheduleBaseUrl() + "api/schedule/v6.0/calendar/GetList?";
        if (scheduleCalendar != null) {
            AccountType accountType = AccountType.getAccountType(scheduleCalendar.getAcType());
            switch (accountType) {
                case EXCHANGE:
                    url = getScheduleBaseUrl() + "api/schedule/v6.0/ews/GetList?";
                    break;
                default:
                    break;
            }
        }
        return url;
    }

    //添加日程
    public static String getAddScheduleUrl(ScheduleCalendar scheduleCalendar) {
        String url = getScheduleBaseUrl() + "api/schedule/v6.0/calendar/add";
        if (scheduleCalendar != null) {
            AccountType accountType = AccountType.getAccountType(scheduleCalendar.getAcType());
            switch (accountType) {
                case EXCHANGE:
                    url = getScheduleBaseUrl() + "api/schedule/v6.0/ews/add";
                    break;
                case APP_MEETING:
//                    url = getScheduleBaseUrl() + "api/schedule/v6.0/meeting/add";
//                    break;
                case APP_SCHEDULE:
                    url = getScheduleBaseUrl() + "api/schedule/v6.0/calendar/add";
                default:
                    break;
            }
        }
        return url;
    }

    //更新日程
    public static String getUpdateScheduleUrl(ScheduleCalendar scheduleCalendar, boolean isMeeting) {
        // String url = getScheduleBaseUrl() + (isMeeting ? "api/schedule/v6.0/meeting/update" : "api/schedule/v6.0/calendar/update");
        String url = getScheduleBaseUrl() + ("api/schedule/v6.0/calendar/update");
        if (scheduleCalendar != null) {
            AccountType accountType = AccountType.getAccountType(scheduleCalendar.getAcType());
            switch (accountType) {
                case EXCHANGE:
                    url = getScheduleBaseUrl() + "api/schedule/v6.0/ews/update";
                    break;
                default:
                    break;
            }
        }
        return url;
    }


    public static String getAddScheduleUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/calendar/add";
    }

    public static String getUpdateScheduleUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/calendar/update";
    }

    //删除日程
    public static String getDeleteScheduleUrl(ScheduleCalendar scheduleCalendar, Schedule schedule) {
        String url = "";
        AccountType accountType = AccountType.getAccountType(scheduleCalendar.getAcType());
        switch (accountType) {
            case EXCHANGE:
                url = getScheduleBaseUrl() + "api/schedule/v6.0/ews/remove";
                break;
            case APP_MEETING:
//                url = getScheduleBaseUrl() + "api/schedule/v6.0/meeting/remove/" + schedule.getId();
//                break;
            default:
                url = getScheduleBaseUrl() + "api/schedule/v6.0/calendar/remove/" + schedule.getId();
                break;
        }
        return url;
    }

    public static String getAddMeetingUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/add";
    }

    public static String getDelMeetingUrl(String meetingId) {
        return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/remove/" + meetingId;
    }

    public static String getMeetingListByStartTime(ScheduleCalendar scheduleCalendar) {
        return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/GetByStartTime?";
    }

    /**
     * 通过id获取会议详情
     */
    public static String getMeetingUrlFromId(String id, ScheduleCalendar scheduleCalendar) {
        //return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/Get/" + id;
        String url = getScheduleBaseUrl() + ("api/schedule/v6.0/calendar/Get/");
        if (scheduleCalendar != null) {
            AccountType accountType = AccountType.getAccountType(scheduleCalendar.getAcType());
            switch (accountType) {
                case EXCHANGE:
                    url = getScheduleBaseUrl() + "api/schedule/v6.0/ews/Get/";
                    break;
                default:
                    break;
            }
        }
        url = url + id;
        return url;

    }

    /**
     * 通过id获取日程详情
     */
    public static String getCalendarUrlFromId(String id) {
        return getScheduleBaseUrl() + "api/schedule/v6.0/calendar/Get/" + id;
    }

    public static String getMeetingHistoryListByPage(int id) {
        return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/GetHistory/" + id;
    }

    public static String getRoomMeetingListByMeetingRoom() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/GetRoomUse?";
    }

    public static String getMeetingUpdateUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/update";
    }

    public static String getHolidayDataUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/calendar/HolidayData/";
    }

    public static String getScheduleBasicDataUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/calendar/Basicdata";
    }


    /**
     * 会议室列表
     *
     * @return
     */
    public static String getMeetingRoomsUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/room";
    }

    /***************会议接口*****************************/
    /**
     * 工作页面会议
     *
     * @return
     */
    private static String getMeetingBaseUrl() {
        return getECMScheduleUrl() + "/meeting/";
    }

    /**
     * 会议预定
     *
     * @return
     */
    public static String getMeetingsUrl() {
        String meetingUrl = "";
        if (WebServiceRouterManager.getInstance().getClusterScheduleVersion().toLowerCase().startsWith("v0") || WebServiceRouterManager.getInstance().getClusterScheduleVersion().toLowerCase().startsWith("v1")) {
            meetingUrl = getMeetingBaseUrl() + "room/bookings";
        }
        return meetingUrl;
    }


    /**
     * 获取时间过滤的rooms
     *
     * @return
     */
    public static String getAvailable() {
        return getMeetingBaseUrl() + "room/available";
    }

    /**
     * 删除会议
     *
     * @return
     */
    public static String getMeetingDeleteUrl() {
        return getMeetingBaseUrl() + "room/booking/cancel?";
    }

    /**
     * 会议室接口
     *
     * @return
     */
    public static String getBookingRoomUrl() {
        return getMeetingBaseUrl() + "booking";
    }


    /**
     * 获取某一个会议室的会议预定情况
     *
     * @return
     */
    public static String getRoomMeetingListUrl() {
        return getMeetingBaseUrl() + "booking/room";
    }

    /**
     * 获取办公地点
     *
     * @return
     */
    public static String getOfficeUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/Location";
    }

    /**
     * 增加办公地点设置常用办公地点
     *
     * @return
     */
    public static String addOfficeUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/CommonLocation";
    }

    /**
     * 会议的root路径
     *
     * @return
     */
    public static String getMeetingRootUrl() {
        return getECMScheduleUrl() + "/meeting";
    }

    /**
     * 获取是否管理员接口
     *
     * @return
     */
    public static String getMeetingIsAdminUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/is_admin";
    }

    /**
     * 获取园区
     *
     * @return
     */
    public static String getLoctionUrl() {
        return getMeetingBaseUrl() + "location";
    }

    /**
     * 获取园区
     *
     * @return
     */
    public static String getLocationUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/meeting/Location";
    }


    /**
     * 设置日程的聊天组
     */
    public static String getSetCalendarBindChatUrl() {
        return getScheduleBaseUrl() + "api/schedule/v6.0/calendar/Chat/Bind";
    }

    /**
     * 获取对应日程的聊天组
     */
    public static String getCalendarBindChatUrl(String calendarId) {
        return getScheduleBaseUrl() + "api/schedule/v6.0/calendar/Chat/" + calendarId;
    }

    /**
     * 会议详情参会状态
     */
    public static String getMeetingAttendStatusUrl(String responseType, ScheduleCalendar scheduleCalendar) {
        switch (AccountType.getAccountType(scheduleCalendar.getAcType())) {
            case EXCHANGE:
                return getScheduleBaseUrl() + "api/schedule/v6.0/ews/" + responseType + "/";
            default:
                return getScheduleBaseUrl() + "api/schedule/v6.0/calendar/" + responseType + "/";


        }

    }


    /*******************任务*****************************/
    /**
     * 任务基础URL
     *
     * @return
     */
    private static String getToDoBaseUrl() {
        String scheduleVersion = WebServiceRouterManager.getInstance().getClusterScheduleVersion().toLowerCase();
        String todoUrl = "";
        if (scheduleVersion.startsWith("v0")) {
            todoUrl = getECMScheduleUrl() + "/api/v0/todo/";
        } else if (scheduleVersion.startsWith("v1")) {
            todoUrl = getECMScheduleUrl() + "/todo/";
        }
        return todoUrl;
    }

    /**
     * 添加附件
     *
     * @param cid
     * @return
     */
    public static String getAddAttachmentsUrl(String cid) {
        return getToDoBaseUrl() + cid + "/attachments";
    }

    /**
     * 获取我的任务
     *
     * @return
     */
    public static String getToDoRecentUrl() {
        return getToDoBaseUrl() + "recent";
    }

    /**
     * 获取我参与的任务
     *
     * @return
     */
    public static String getInvolvedTasksUrl() {
        return getToDoBaseUrl() + "involved";
    }

    /**
     * 获取我参与的任务
     *
     * @return
     */
    public static String getFocusedTasksUrl() {
        return getToDoBaseUrl() + "focused";
    }

    /**
     * 创建任务
     *
     * @return
     */
    public static String getCreateTaskUrl() {
        return getToDoBaseUrl();
    }

    /**
     * 获取今天的任务
     *
     * @return
     */
    public static String getTodayTaskUrl() {
        return getToDoBaseUrl() + "today";
    }

    /**
     * 获取所有Tag
     *
     * @return
     */
    public static String getTagUrl() {
        return getToDoBaseUrl() + "tag";
    }

    /**
     * 获取所有task
     *
     * @param id
     * @return
     */
    public static String getTasksList(String id) {
        return getToDoBaseUrl() + "list/" + id + "/tasks";
    }

    /**
     * 获取删除工作中的tags
     */
    public static String getDelTaskTagsUrl(String taskId) {
        return getToDoBaseUrl() + taskId + "/tags";
    }

    /**
     * 获取删除工作中的tags
     */
    public static String getAddTaskTagsUrl(String taskId) {
        return getToDoBaseUrl() + taskId + "/tags";
    }


    /**
     * 变更任务所有人
     *
     * @return
     */
    public static String getChangeMessionOwnerUrl() {
        return getToDoBaseUrl();
    }


    /**********************日历接口**********************/
    /**
     * 日历相关Uri
     *
     * @return
     */
    public static String getCalendarUrl() {
        String scheduleVersion = WebServiceRouterManager.getInstance().getClusterScheduleVersion().toLowerCase();
        String calendarUrl = "";
        if (scheduleVersion.startsWith("v0")) {
            calendarUrl = getECMScheduleUrl() + "/api/v0";
        } else if (scheduleVersion.startsWith("v1")) {
            calendarUrl = getECMScheduleUrl();
        }
        return calendarUrl;
    }

}
