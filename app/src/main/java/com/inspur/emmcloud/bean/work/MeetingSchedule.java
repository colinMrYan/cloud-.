package com.inspur.emmcloud.bean.work;


import com.inspur.emmcloud.bean.schedule.meeting.Meeting;

public class MeetingSchedule {
    private Long from;
    private Long to;
    private Meeting meeting;

    public MeetingSchedule(Long from, Long to, Meeting meeting) {
        this.from = from;
        this.to = to;
        this.meeting = meeting;
    }

    public MeetingSchedule() {

    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }
}
