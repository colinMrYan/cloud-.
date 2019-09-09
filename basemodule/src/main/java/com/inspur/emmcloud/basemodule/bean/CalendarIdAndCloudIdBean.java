package com.inspur.emmcloud.basemodule.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 系统日历数据表和云+日程数据表的关联表
 */
@Table(name = "CalendarIdAndCloudIdBean")
public class CalendarIdAndCloudIdBean {

    public static final String CLOUD_PLUS_CALENDAR_ID = "calendarId";
    public static final String CLOUD_PLUS_SCHEDULE_CALENDAR_ID = "cloudScheduleRemindId";
    //CalendarIdAndCloudIdBean 本身的id
    @Column(name = "id", isId = true)
    private int id;
    //系统日历事件的id
    @Column(name = "calendarId")
    private String calendarId = "";
    //云+日程事件的Id
    @Column(name = "cloudScheduleId")
    private String cloudScheduleId = "";
    //系统日历提醒事件的id
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
