package com.inspur.mvp_demo.meeting.contract;

import com.inspur.emmcloud.basemodule.mvp.BaseView;
import com.inspur.mvp_demo.meeting.model.bean.Meeting;

import java.util.List;

public interface MeetingContract {
    interface Model {

    }

    interface View extends BaseView {
        void showMeetingList(List<Meeting> meetingList);

    }

    interface Presenter {
        void getMeetingList(int pageNum);
    }
}
