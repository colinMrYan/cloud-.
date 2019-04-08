package com.inspur.emmcloud.bean.work;

import com.alibaba.fastjson.annotation.JSONField;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.widget.calendardayview.Event;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class CalendarEvent implements Serializable, Comparator {
    private String id;
    private Calendar creationDate;
    private Calendar lastUpdate;
    private String state;
    private String title;
    private String owner;
    private String location;
    private boolean AllDay;
    private Calendar startDate;
    private Calendar endDate;
    private MyCalendar calendar;

    public CalendarEvent() {

    }

    public CalendarEvent(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "");
        title = JSONUtils.getString(obj, "title", "");
        long creationDateLong = JSONUtils.getLong(obj, "creationDate", 0L);
        creationDate = TimeUtils.timeLong2Calendar(creationDateLong);
        Long lastUpdateLong = JSONUtils.getLong(obj, "lastUpdate", 0L);
        lastUpdate = TimeUtils.timeLong2Calendar(lastUpdateLong);
        AllDay = JSONUtils.getBoolean(obj,"allday",false);
        Long startDateLong = JSONUtils.getLong(obj, "startDate", 0L);
        startDate = TimeUtils.timeLong2Calendar(startDateLong);
        if (AllDay){
            startDate = TimeUtils.getDayBeginCalendar(startDate);
        }
        Long endDateLong = JSONUtils.getLong(obj, "endDate", 0L);
        if (endDateLong == 0){
            endDate = TimeUtils.getDayEndCalendar(startDate);
        }else if(AllDay){
            endDate = TimeUtils.timeLong2Calendar(endDateLong);
            endDate= TimeUtils.getDayEndCalendar(endDate);
        }
        endDate = TimeUtils.timeLong2Calendar(endDateLong);
        state = JSONUtils.getString(obj, "state", "");
        owner = JSONUtils.getString(obj, "owner", "");
        location = JSONUtils.getString(obj, "location", "");
        title = JSONUtils.getString(obj, "title", "");
        JSONObject calendarObj = JSONUtils.getJSONObject(obj, "calendar", new JSONObject());
        calendar = new MyCalendar(calendarObj);
    }


    public static List<Event> calendarEvent2EventList(List<CalendarEvent> calendarEventList, Calendar selectCalendar) {
        List<Event> eventList = new ArrayList<>();
        for (CalendarEvent calendarEvent : calendarEventList) {
            if (TimeUtils.isContainTargentCalendarDay(selectCalendar, calendarEvent.getStartDate(), calendarEvent.getEndDate())) {
                Calendar eventStartTime =  calendarEvent.getStartDate();
                Calendar eventEndTime = calendarEvent.getEndDate();
                Calendar dayBeginCalendar = TimeUtils.getDayBeginCalendar(selectCalendar);
                Calendar dayEndCalendar = TimeUtils.getDayEndCalendar(selectCalendar);
                if (eventStartTime.before(dayBeginCalendar)) {
                    eventStartTime = dayBeginCalendar;
                }
                if (eventEndTime.after(dayEndCalendar)) {
                    eventEndTime = dayEndCalendar;
                }
                Event event = new Event(calendarEvent.getId(), Event.TYPE_CALENDAR, calendarEvent.getTitle(), "", eventStartTime, eventEndTime);
                    event.setAllDay(calendarEvent.getAllDay());
                eventList.add(event);
            }
        }
        return eventList;
    }

    public MyCalendar getCalendar() {
        return calendar;
    }

    public void setCalendar(MyCalendar calendar) {
        this.calendar = calendar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOwner() {
        return owner;
    }

    @JSONField(serialize = false)
    public Calendar getLocalCreationDate() {
        return TimeUtils.UTCCalendar2LocalCalendar(creationDate);
    }

    public Calendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    @JSONField(serialize = false)
    public Calendar getLocalLastUpdate() {
        return TimeUtils.UTCCalendar2LocalCalendar(lastUpdate);
    }

    public Calendar getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Calendar lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean getAllDay() {
        return AllDay;
    }

    public void setAllDay(boolean allDay) {
        this.AllDay = allDay;
    }
    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }


    public List<Event> calendarEventList2EventList(List<CalendarEvent> calendarEventList) {
        List<Event> eventList = new ArrayList<>();
        for (CalendarEvent calendarEvent : calendarEventList) {
            Event event = new Event(calendarEvent.getId(), Event.TYPE_CALENDAR, title, "", startDate, endDate);
            eventList.add(event);
        }
        return eventList;
    }

    @Override
    public int compare(Object lhs, Object rhs) {
        CalendarEvent calEventA = (CalendarEvent) lhs;
        CalendarEvent calEventB = (CalendarEvent) rhs;
        if (calEventA.getStartDate().after(calEventB.getStartDate())) {
            return 1;
        } else if (calEventA.getStartDate().before(calEventB.getStartDate())) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
     */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof CalendarEvent))
            return false;

        final CalendarEvent otherCalendarEvent = (CalendarEvent) other;
        return getId().equals(otherCalendarEvent.getId());
    }
}
