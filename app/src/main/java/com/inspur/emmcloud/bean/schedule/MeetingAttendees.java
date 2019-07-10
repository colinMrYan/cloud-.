package com.inspur.emmcloud.bean.schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/7/5.
 */

public class MeetingAttendees {
    final public static String MEETING_ATTENDEES_INVITE = "meeting_attendees_invite";

    private String type = Participant.CALENDAR_RESPONSE_TYPE_UNKNOWN;
    private String name = "";
    private List<Participant> meetingAttendeesList = new ArrayList<>();

    public MeetingAttendees() {
    }

    public MeetingAttendees(String type) {
        this.type = type;
        name = getNameByType();
    }

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

    public List<Participant> getMeetingAttendeesList() {
        return meetingAttendeesList;
    }

    public void setMeetingAttendeesList(List<Participant> meetingAttendeesList) {
        this.meetingAttendeesList = meetingAttendeesList;
    }

    private String getNameByType() {
        String name = "";
        switch (type) {
            case MEETING_ATTENDEES_INVITE:
                name = "邀请者";
                break;
            case Participant.CALENDAR_RESPONSE_TYPE_ACCEPT:
                name = "同意";
                break;
            case Participant.CALENDAR_RESPONSE_TYPE_DECLINE:
                name = "拒绝";
                break;
            case Participant.CALENDAR_RESPONSE_TYPE_UNKNOWN:
                name = "无响应";
                break;
            default:
                break;
        }
        return name;
    }
}
