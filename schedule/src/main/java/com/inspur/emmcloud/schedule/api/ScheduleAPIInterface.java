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

public interface ScheduleAPIInterface {
    void returnScheduleListSuccess(GetScheduleListResult getScheduleListResult, Calendar startCalendar, Calendar endCalendar, ScheduleCalendar scheduleCalendar);

    void returnScheduleListFail(String error, int errorCode, ScheduleCalendar scheduleCalendar);

    void returnAddScheduleSuccess(GetIDResult getIDResult);

    void returnAddScheduleFail(String error, int errorCode);

    void returnUpdateScheduleSuccess();

    void returnUpdateScheduleFail(String error, int errorCode);

    void returnDeleteScheduleSuccess(String scheduleId);

    void returnDeleteScheduleFail(String error, int errorCode);

    void returnAddMeetingSuccess();

    void returnAddMeetingFail(String error, int errorCode);

    void returnDelMeetingSuccess(Schedule meeting);

    void returnDelMeetingFail(String error, int errorCode);

    //会议-通过id获取
    void returnMeetingDataFromIdSuccess(Schedule meeting);

    void returnMeetingDataFromIdFail(String error, int errorCode);

    //日程-通过id获取
    void returnScheduleDataFromIdSuccess(Schedule schedule);

    void returnScheduleDataFromIdFail(String error, int errorCode);

    void returnMeetingListSuccess(GetMeetingListResult getMeetingListByMeetingRoomResult);

    void returnMeetingListFail(String error, int errorCode);

    void returnMeetingListByMeetingRoomFail(String error, int errorCode);

    void returnMeetingHistoryListSuccess(GetMeetingListResult getMeetingListByMeetingRoomResult);

    void returnMeetingHistoryListFail(String error, int errorCode);

    void returnUpdateMeetingSuccess();

    void returnUpdateMeetingFail(String error, int errorCode);

    void returnHolidayDataSuccess(GetHolidayDataResult getHolidayDataResult);

    void returnHolidayDataFail(String error, int errorCode);

    void returnMeetingRoomListSuccess(GetMeetingRoomListResult getMeetingRoomsResult);

    void returnMeetingRoomListFail(String error, int errorCode);

    void returnMeetingRoomListSuccess(GetMeetingRoomListResult getMeetingRoomsResult, boolean isFilte);

    void returnSetCalendarChatBindSuccess(String calendarId, String chatId);

    void returnSetCalendarChatBindFail(String error, int errorCode);

    void returnGetCalendarChatBindSuccess(String calendarId, String cid);

    void returnGetCalendarChatBindFail(String error, int errorCode);

    void returnAttendMeetingStatusSuccess(String result, String responseType);

    void returnAttendMeetingStatusFail(String error, int errorCode);

    void returnIsMeetingAdminSuccess(GetIsMeetingAdminResult getIsAdmin);

    void returnIsMeetingAdminFail(String error, int errorCode);

    void returnScheduleBasicDataSuccess(GetScheduleBasicDataResult getScheduleBasicDataResult);

    void returnScheduleBasicDataFail(String error, int errorCode);

    void returnDelAttachmentSuccess(int position);

    void returnDelAttachmentFail(String error, int errorCode, int position);

    void returnChangeMessionOwnerSuccess(String managerName);

    void returnChangeMessionOwnerFail(String error, int errorCode);

    void returnChangeMessionTagSuccess();

    void returnChangeMessionTagFail(String error, int errorCode);

    void returnDelTaskTagSuccess();

    void returnDelTaskTagFail(String error, int errorCode);

    void returnAddTaskTagSuccess();

    void returnAddTaskTagFail(String error, int errorCode);

    void returnDeleteOfficeSuccess(Office office);

    void returnDeleteOfficeFail(String error, int errorCode);

    void returnSetMeetingCommonBuildingSuccess(Building building);

    void returnSetMeetingCommonBuildingFail(String error, int errorCode);

    void returnCancelMeetingCommonBuildingSuccess(Building building);

    void returnCancelMeetingCommonBuildingFail(String error, int errorCode);

    void returnLocationResultSuccess(GetLocationResult getLoctionResult);

    void returnLocationResultFail(String error, int errorCode);

    void returnOfficeListResultSuccess(GetOfficeListResult getOfficeListResult);

    void returnOfficeListResultFail(String error, int errorCode);

    void returnAddMeetingOfficeSuccess(Office office, Building building);

    void returnAddMeetingOfficeFail(String error, int errorCode);

    void returnRecentTasksSuccess(GetTaskListResult getTaskListResult);

    void returnRecentTasksFail(String error, int errorCode);

    void returnMyCalendarSuccess(GetMyCalendarResult getMyCalendarResult);

    void returnMyCalendarFail(String error, int errorCode);

    void returnDelelteCalendarByIdSuccess();

    void returnDelelteCalendarByIdFail(String error, int errorCode);

    void returnUpdateCalendarSuccess();

    void returnUpdateCalendarFail(String error, int errorCode);

    void returnGetTagResultSuccess(GetTagResult getTagResult);

    void returnGetTagResultFail(String error, int errorCode);

    void returnAddCalEventSuccess(GetIDResult getIDResult);

    void returnAddCalEventFail(String error, int errorCode);

    void returnDeleteTagSuccess();

    void returnDeleteTagFail(String error, int errorCode);

    void returnCreateTagSuccess();

    void returnCreateTagFail(String error, int errorCode);

    void returnCreateTaskSuccess(GetTaskAddResult getTaskAddResult);

    void returnCreateTaskFail(String error, int errorCode);

    void returnDeleteTaskSuccess();

    void returnDeleteTaskFail(String error, int errorCode);

    void returnInviteMateForTaskSuccess(String subobject);

    void returnInviteMateForTaskFail(String error, int errorCode);

    void returnUpdateTaskSuccess(int position);

    void returnUpdateTaskFail(String error, int errorCode, int position);

    void returnAttachmentSuccess(Task taskResult);

    void returnAttachmentFail(String error, int errorCode);

    void returnAddAttachMentSuccess(Attachment attachment);

    void returnAddAttachMentFail(String error, int errorCode);

    void returnGetTasksSuccess(GetTaskListResult getTaskListResult);

    void returnGetTasksFail(String error, int errorCode);

    void returnDelTaskMemSuccess();

    void returnDelTaskMemFail(String error, int errorCode);

    void returnDelTripSuccess();

    void returnDelTripFail(String error, int errorCode);

    void returnDeleteMeetingSuccess(Meeting meeting);

    void returnDeleteMeetingFail(String error, int errorCode);

}
