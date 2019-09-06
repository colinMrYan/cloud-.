package com.inspur.emmcloud.basemodule.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "CalendarIdAndCloudIdBean")
public class CalendarIdAndCloudIdBean {
    @Column(name = "id", isId = true)
    private int id;
    @Column(name = "calendarId")
    private String calendarId = "";
    @Column(name = "cloudScheduleId")
    private String cloudScheduleId = "";

    public CalendarIdAndCloudIdBean() {
    }

    public CalendarIdAndCloudIdBean(String calendarId, String cloudScheduleId) {
        this.calendarId = calendarId;
        this.cloudScheduleId = cloudScheduleId;
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
}
