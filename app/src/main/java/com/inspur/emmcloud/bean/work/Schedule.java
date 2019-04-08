package com.inspur.emmcloud.bean.work;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by chenmch on 2019/4/6.
 */
@Table(name = "Schedule")
public class Schedule implements Serializable{
    @Column(name = "id", isId = true)
    private String id;// 唯一标识
    @Column(name = "title")
    private String title ;//日程标题
    @Column(name = "calendarType")
    private String calendarType;//日程类型（出差、会议等，可以自定义）
    @Column(name = "owner")
    private String owner;//创建人的inspurId
    @Column(name = "startTime")
    private long startTime;//开始时间
    @Column(name = "endTime")
    private long endTime;// 结束时间
    @Column(name = "creationTime")
    private long creationTime;//创建时间
    @Column(name = "lastTime")
    private long lastTime;// 最后修改时间
    private Calendar startTimeCalendar;//开始时间
    private Calendar endTimeCalendar;// 结束时间
    private Calendar creationTimeCalendar;//创建时间
    private Calendar lastTimeCalendar;// 最后修改时间
    @Column(name = "isAllDay")
    private Boolean isAllDay;//是否全天
    @Column(name = "isCommunity")
    private Boolean isCommunity;//是否公开（别人可以关注你的日程，此属性决定了当前日程是否对别人可见）
    @Column(name = "syncToLocal")
    private Boolean syncToLocal ;//是否将日程信息同步到 移动设备日历里边。
    @Column(name = "remindEvent")
    private String remindEvent;
    private RemindEvent remindEventObj;   //（详见RemindEvent 对象描述）， 日程的提醒信息，如果此属性为 null，表示该日程不需要提醒。
    @Column(name = "state")
    private int state;//日程的状态，客户端暂时可忽略该属性
    @Column(name = "location")
    private String location;
    private Location scheduleLocationObj;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCalendarType() {
        return calendarType;
    }

    public void setCalendarType(String calendarType) {
        this.calendarType = calendarType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public Boolean getAllDay() {
        return isAllDay;
    }

    public void setAllDay(Boolean allDay) {
        isAllDay = allDay;
    }

    public Boolean getCommunity() {
        return isCommunity;
    }

    public void setCommunity(Boolean community) {
        isCommunity = community;
    }

    public Boolean getSyncToLocal() {
        return syncToLocal;
    }

    public void setSyncToLocal(Boolean syncToLocal) {
        this.syncToLocal = syncToLocal;
    }

    public String getRemindEvent() {
        return remindEvent;
    }

    public void setRemindEvent(String remindEvent) {
        this.remindEvent = remindEvent;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Calendar getStartTimeCalendar() {
        return startTimeCalendar;
    }

    public Calendar getEndTimeCalendar() {
        return endTimeCalendar;
    }

    public Calendar getCreationTimeCalendar() {
        return creationTimeCalendar;
    }

    public Calendar getLastTimeCalendar() {
        return lastTimeCalendar;
    }

    public RemindEvent getRemindEventObj() {
        return remindEventObj;
    }

    public Location getScheduleLocationObj() {
        return scheduleLocationObj;
    }
}
