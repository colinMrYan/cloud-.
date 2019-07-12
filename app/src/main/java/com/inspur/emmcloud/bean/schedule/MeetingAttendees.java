package com.inspur.emmcloud.bean.schedule;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;

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
                name = BaseApplication.getInstance().getString(R.string.meeting_detail_invite);
                break;
            case Participant.CALENDAR_RESPONSE_TYPE_ACCEPT:
                name = BaseApplication.getInstance().getString(R.string.meeting_detail_attendee_accept);
                break;
            case Participant.CALENDAR_RESPONSE_TYPE_DECLINE:
                name = BaseApplication.getInstance().getString(R.string.meeting_detail_attendee_decline);
                break;
            case Participant.CALENDAR_RESPONSE_TYPE_UNKNOWN:
                name = BaseApplication.getInstance().getString(R.string.meeting_detail_attendee_unknown);
                break;
            default:
                break;
        }
        return name;
    }
}
