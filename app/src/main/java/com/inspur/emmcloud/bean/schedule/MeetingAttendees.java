package com.inspur.emmcloud.bean.schedule;

import com.inspur.emmcloud.basemodule.bean.SearchModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/7/5.
 */

public class MeetingAttendees {
    final public static String MEETING_ATTENDEES_INVITE = "meeting_attendees_invite";
    final public static String MEETING_ATTENDEES_ACCEPT = "meeting_attendees_accept";
    final public static String MEETING_ATTENDEES_DENY = "meeting_attendees_deny";
    final public static String MEETING_ATTENDEES_NO_ACTION = "meeting_attendees_no_action";

    private String type = MEETING_ATTENDEES_NO_ACTION;
    private String name = "";
    private List<SearchModel> meetingAttendeesList = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        name = getNameByType();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SearchModel> getMeetingAttendeesList() {
        return meetingAttendeesList;
    }

    public void setMeetingAttendeesList(List<SearchModel> meetingAttendeesList) {
        this.meetingAttendeesList = meetingAttendeesList;
    }

    private String getNameByType() {
        String name = "";
        switch (type) {
            case MEETING_ATTENDEES_INVITE:
                name = "邀请者";
                break;
            case MEETING_ATTENDEES_ACCEPT:
                name = "同意";
                break;
            case MEETING_ATTENDEES_DENY:
                name = "拒绝";
                break;
            case MEETING_ATTENDEES_NO_ACTION:
                name = "无响应";
                break;
            default:
                break;
        }
        return name;
    }
}
