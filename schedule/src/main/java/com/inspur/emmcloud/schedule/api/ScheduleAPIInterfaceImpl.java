package com.inspur.emmcloud.schedule.api;

import com.inspur.emmcloud.schedule.bean.GetIDResult;
import com.inspur.emmcloud.schedule.bean.GetScheduleListResult;
import com.inspur.emmcloud.schedule.bean.Schedule;
import com.inspur.emmcloud.schedule.bean.calendar.GetHolidayDataResult;
import com.inspur.emmcloud.schedule.bean.calendar.GetMyCalendarResult;
import com.inspur.emmcloud.schedule.bean.calendar.GetScheduleBasicDataResult;
import com.inspur.emmcloud.schedule.bean.calendar.ScheduleCalendar;
import com.inspur.emmcloud.schedule.bean.meeting.Building;
import com.inspur.emmcloud.schedule.bean.meeting.GetIsMeetingAdminResult;
import com.inspur.emmcloud.schedule.bean.meeting.GetLocationResult;
import com.inspur.emmcloud.schedule.bean.meeting.GetMeetingListResult;
import com.inspur.emmcloud.schedule.bean.meeting.GetMeetingRoomListResult;
import com.inspur.emmcloud.schedule.bean.meeting.GetOfficeListResult;
import com.inspur.emmcloud.schedule.bean.meeting.GetTagResult;
import com.inspur.emmcloud.schedule.bean.meeting.Meeting;
import com.inspur.emmcloud.schedule.bean.meeting.Office;
import com.inspur.emmcloud.schedule.bean.task.Attachment;
import com.inspur.emmcloud.schedule.bean.task.GetTaskAddResult;
import com.inspur.emmcloud.schedule.bean.task.GetTaskListResult;
import com.inspur.emmcloud.schedule.bean.task.Task;

import java.util.Calendar;

/**
 * Created by libaochao on 2019/12/10.
 */

public class ScheduleAPIInterfaceImpl implements ScheduleAPIInterface {
    @Override
    public void returnScheduleListSuccess(GetScheduleListResult getScheduleListResult, Calendar startCalendar, Calendar endCalendar, ScheduleCalendar scheduleCalendar) {

    }

    @Override
    public void returnScheduleListFail(String error, int errorCode, ScheduleCalendar scheduleCalendar) {

    }

    @Override
    public void returnAddScheduleSuccess(GetIDResult getIDResult) {

    }

    @Override
    public void returnAddScheduleFail(String error, int errorCode) {

    }

    @Override
    public void returnUpdateScheduleSuccess() {

    }

    @Override
    public void returnUpdateScheduleFail(String error, int errorCode) {

    }

    @Override
    public void returnDeleteScheduleSuccess(String scheduleId) {

    }

    @Override
    public void returnDeleteScheduleFail(String error, int errorCode) {

    }

    @Override
    public void returnAddMeetingSuccess() {

    }

    @Override
    public void returnAddMeetingFail(String error, int errorCode) {

    }

    @Override
    public void returnDelMeetingSuccess(Schedule meeting) {

    }

    @Override
    public void returnDelMeetingFail(String error, int errorCode) {

    }

    @Override
    public void returnMeetingDataFromIdSuccess(Schedule meeting) {

    }

    @Override
    public void returnMeetingDataFromIdFail(String error, int errorCode) {

    }

    @Override
    public void returnScheduleDataFromIdSuccess(Schedule schedule) {

    }

    @Override
    public void returnScheduleDataFromIdFail(String error, int errorCode) {

    }

    @Override
    public void returnMeetingListSuccess(GetMeetingListResult getMeetingListByMeetingRoomResult) {

    }

    @Override
    public void returnMeetingListFail(String error, int errorCode) {

    }

    @Override
    public void returnMeetingListByMeetingRoomFail(String error, int errorCode) {

    }

    @Override
    public void returnMeetingHistoryListSuccess(GetMeetingListResult getMeetingListByMeetingRoomResult) {

    }

    @Override
    public void returnMeetingHistoryListFail(String error, int errorCode) {

    }

    @Override
    public void returnUpdateMeetingSuccess() {

    }

    @Override
    public void returnUpdateMeetingFail(String error, int errorCode) {

    }

    @Override
    public void returnHolidayDataSuccess(GetHolidayDataResult getHolidayDataResult) {

    }

    @Override
    public void returnHolidayDataFail(String error, int errorCode) {

    }

    @Override
    public void returnMeetingRoomListSuccess(GetMeetingRoomListResult getMeetingRoomsResult) {

    }

    @Override
    public void returnMeetingRoomListFail(String error, int errorCode) {

    }

    @Override
    public void returnMeetingRoomListSuccess(GetMeetingRoomListResult getMeetingRoomsResult, boolean isFilte) {

    }

    @Override
    public void returnSetCalendarChatBindSuccess(String calendarId, String chatId) {

    }

    @Override
    public void returnSetCalendarChatBindFail(String error, int errorCode) {

    }

    @Override
    public void returnGetCalendarChatBindSuccess(String calendarId, String cid) {

    }

    @Override
    public void returnGetCalendarChatBindFail(String error, int errorCode) {

    }

    @Override
    public void returnAttendMeetingStatusSuccess(String result, String responseType) {

    }

    @Override
    public void returnAttendMeetingStatusFail(String error, int errorCode) {

    }

    @Override
    public void returnIsMeetingAdminSuccess(GetIsMeetingAdminResult getIsAdmin) {

    }

    @Override
    public void returnIsMeetingAdminFail(String error, int errorCode) {

    }

    @Override
    public void returnScheduleBasicDataSuccess(GetScheduleBasicDataResult getScheduleBasicDataResult) {

    }

    @Override
    public void returnScheduleBasicDataFail(String error, int errorCode) {

    }

    @Override
    public void returnDelAttachmentSuccess(int position) {

    }

    @Override
    public void returnDelAttachmentFail(String error, int errorCode, int position) {

    }

    @Override
    public void returnChangeMessionOwnerSuccess(String managerName) {

    }

    @Override
    public void returnChangeMessionOwnerFail(String error, int errorCode) {

    }

    @Override
    public void returnChangeMessionTagSuccess() {

    }

    @Override
    public void returnChangeMessionTagFail(String error, int errorCode) {

    }

    @Override
    public void returnDelTaskTagSuccess() {

    }

    @Override
    public void returnDelTaskTagFail(String error, int errorCode) {

    }

    @Override
    public void returnAddTaskTagSuccess() {

    }

    @Override
    public void returnAddTaskTagFail(String error, int errorCode) {

    }

    @Override
    public void returnDeleteOfficeSuccess(Office office) {

    }

    @Override
    public void returnDeleteOfficeFail(String error, int errorCode) {

    }

    @Override
    public void returnSetMeetingCommonBuildingSuccess(Building building) {

    }

    @Override
    public void returnSetMeetingCommonBuildingFail(String error, int errorCode) {

    }

    @Override
    public void returnCancelMeetingCommonBuildingSuccess(Building building) {

    }

    @Override
    public void returnCancelMeetingCommonBuildingFail(String error, int errorCode) {

    }

    @Override
    public void returnLocationResultSuccess(GetLocationResult getLoctionResult) {

    }

    @Override
    public void returnLocationResultFail(String error, int errorCode) {

    }

    @Override
    public void returnOfficeListResultSuccess(GetOfficeListResult getOfficeListResult) {

    }

    @Override
    public void returnOfficeListResultFail(String error, int errorCode) {

    }

    @Override
    public void returnAddMeetingOfficeSuccess(Office office, Building building) {

    }

    @Override
    public void returnAddMeetingOfficeFail(String error, int errorCode) {

    }

    @Override
    public void returnRecentTasksSuccess(GetTaskListResult getTaskListResult) {

    }

    @Override
    public void returnRecentTasksFail(String error, int errorCode) {

    }

    @Override
    public void returnMyCalendarSuccess(GetMyCalendarResult getMyCalendarResult) {

    }

    @Override
    public void returnMyCalendarFail(String error, int errorCode) {

    }

    @Override
    public void returnDelelteCalendarByIdSuccess() {

    }

    @Override
    public void returnDelelteCalendarByIdFail(String error, int errorCode) {

    }

    @Override
    public void returnUpdateCalendarSuccess() {

    }

    @Override
    public void returnUpdateCalendarFail(String error, int errorCode) {

    }

    @Override
    public void returnGetTagResultFail(String error, int errorCode) {

    }

    @Override
    public void returnAddCalEventSuccess(GetIDResult getIDResult) {

    }

    @Override
    public void returnAddCalEventFail(String error, int errorCode) {

    }

    @Override
    public void returnDeleteTagSuccess() {

    }

    @Override
    public void returnDeleteTagFail(String error, int errorCode) {

    }

    @Override
    public void returnCreateTagSuccess() {

    }

    @Override
    public void returnCreateTagFail(String error, int errorCode) {

    }

    @Override
    public void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult) {

    }

    @Override
    public void returnCreateTaskFail(String error, int errorCode) {

    }

    @Override
    public void returnDeleteTaskSuccess() {

    }

    @Override
    public void returnDeleteTaskFail(String error, int errorCode) {

    }

    @Override
    public void returnInviteMateForTaskSuccess(String subobject) {

    }

    @Override
    public void returnInviteMateForTaskFail(String error, int errorCode) {

    }

    @Override
    public void returnUpdateTaskSuccess(int position) {

    }

    @Override
    public void returnUpdateTaskFail(String error, int errorCode, int position) {

    }

    @Override
    public void returnAttachmentSuccess(Task taskResult) {

    }

    @Override
    public void returnAttachmentFail(String error, int errorCode) {

    }

    @Override
    public void returnAddAttachMentSuccess(Attachment attachment) {

    }

    @Override
    public void returnAddAttachMentFail(String error, int errorCode) {

    }

    @Override
    public void returnGetTasksSuccess(GetTaskListResult getTaskListResult) {

    }

    @Override
    public void returnGetTasksFail(String error, int errorCode) {

    }

    @Override
    public void returnDelTaskMemSuccess() {

    }

    @Override
    public void returnDelTaskMemFail(String error, int errorCode) {

    }

    @Override
    public void returnDelTripSuccess() {

    }

    @Override
    public void returnDelTripFail(String error, int errorCode) {

    }

    @Override
    public void returnDeleteMeetingSuccess(Meeting meeting) {

    }

    @Override
    public void returnDeleteMeetingFail(String error, int errorCode) {

    }

    @Override
    public void returnGetTagResultSuccess(GetTagResult getTagResult) {

    }
}
