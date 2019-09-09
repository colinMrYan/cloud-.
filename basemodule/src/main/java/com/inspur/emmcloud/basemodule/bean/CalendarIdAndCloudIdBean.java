package com.inspur.emmcloud.basemodule.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "CalendarIdAndCloudIdBean")
public class CalendarIdAndCloudIdBean {

    public static final String CLOUD_PLUS_CALENDAR_ID = "calendarId";
    public static final String CLOUD_PLUS_SCHEDULE_CALENDAR_ID = "cloudScheduleRemindId";
    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "calendarId")
    private String calendarId = "";
    @Column(name = "cloudScheduleId")
    private String cloudScheduleId = "";
    @Column(name = "cloudScheduleRemindId")
    private String cloudScheduleRemindId = "";

    public CalendarIdAndCloudIdBean() {
    }

    public CalendarIdAndCloudIdBean(String calendarId, String cloudScheduleId, String cloudScheduleRemindId) {
        this.calendarId = calendarId;
        this.cloudScheduleId = cloudScheduleId;
        this.cloudScheduleRemindId = cloudScheduleRemindId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    public String getCloudScheduleId() {
        return cloudScheduleId;
    }

    public void setCloudScheduleId(String cloudScheduleId) {
        this.cloudScheduleId = cloudScheduleId;
    }

    public String getCloudScheduleRemindId() {
        return cloudScheduleRemindId;
    }

    public void setCloudScheduleRemindId(String cloudScheduleRemindId) {
        this.cloudScheduleRemindId = cloudScheduleRemindId;
    }
}
