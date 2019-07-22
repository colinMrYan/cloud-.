package com.inspur.emmcloud.webex.api;

import com.inspur.emmcloud.webex.bean.GetScheduleWebexMeetingSuccess;
import com.inspur.emmcloud.webex.bean.GetWebexMeetingListResult;
import com.inspur.emmcloud.webex.bean.GetWebexTKResult;
import com.inspur.emmcloud.webex.bean.WebexMeeting;

/**
 * Created by chenmch on 2019/7/19.
 */

public class WebexAPIInterfaceImpl implements WebexAPIInterface {
    @Override
    public void returnWebexMeetingListSuccess(GetWebexMeetingListResult getWebexMeetingListResult) {

    }

    @Override
    public void returnWebexMeetingListFail(String error, int errorCode) {

    }

    @Override
    public void returnScheduleWebexMeetingSuccess(GetScheduleWebexMeetingSuccess getScheduleWebexMeetingSuccess) {

    }

    @Override
    public void returnScheduleWebexMeetingFail(String error, int errorCode) {

    }

    @Override
    public void returnWebexMeetingSuccess(WebexMeeting webexMeeting) {

    }

    @Override
    public void returnWebexMeetingFail(String error, int errorCode) {

    }

    @Override
    public void returnWebexTKSuccess(GetWebexTKResult getWebexTKResult) {

    }

    @Override
    public void returnWebexTKFail(String error, int errorCode) {

    }

    @Override
    public void returnRemoveWebexMeetingSuccess() {

    }

    @Override
    public void returnRemoveWebexMeetingFail(String error, int errorCode) {

    }
}
