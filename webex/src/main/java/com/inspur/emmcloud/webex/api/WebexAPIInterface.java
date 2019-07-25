package com.inspur.emmcloud.webex.api;

import com.inspur.emmcloud.webex.bean.GetScheduleWebexMeetingSuccess;
import com.inspur.emmcloud.webex.bean.GetWebexMeetingListResult;
import com.inspur.emmcloud.webex.bean.GetWebexTKResult;
import com.inspur.emmcloud.webex.bean.WebexMeeting;

/**
 * Created by chenmch on 2019/7/19.
 */

public interface WebexAPIInterface {
    void returnWebexMeetingListSuccess(GetWebexMeetingListResult getWebexMeetingListResult);

    void returnWebexMeetingListFail(String error, int errorCode);

    void returnScheduleWebexMeetingSuccess(GetScheduleWebexMeetingSuccess getScheduleWebexMeetingSuccess);

    void returnScheduleWebexMeetingFail(String error, int errorCode);

    void returnWebexMeetingSuccess(WebexMeeting webexMeeting);

    void returnWebexMeetingFail(String error, int errorCode);

    void returnWebexTKSuccess(GetWebexTKResult getWebexTKResult);

    void returnWebexTKFail(String error, int errorCode);

    void returnRemoveWebexMeetingSuccess();

    void returnRemoveWebexMeetingFail(String error, int errorCode);
}
