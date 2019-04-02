package com.inspur.emmcloud.widget.calendardayview;


import java.util.Calendar;

/**
 * Created by chenmch on 2019/3/29.
 */

public class Event {
    public static final String TYPE_MEETING = "event_meeting";
    public static final String TYPE_CALENDAR = "event_calendar";
    public static final String TYPE_TASK= "event_task";
    public  String eventId;
    public String eventType;
    public String eventTitle;
    public String eventSubTitle;
    public Calendar eventStartTime;
    public Calendar eventEndTime;
    private int index = -1;
    public Event(String eventId,String eventType,String eventTitle,String eventSubTitle,Calendar eventStartTime,Calendar eventEndTime){
        this.eventId = eventId;
        this.eventType  = eventType;
        this.eventTitle = eventTitle;
        this.eventSubTitle = eventSubTitle;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventSubTitle() {
        return eventSubTitle;
    }

    public void setEventSubTitle(String eventSubTitle) {
        this.eventSubTitle = eventSubTitle;
    }

    public Calendar getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(Calendar eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public Calendar getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(Calendar eventEndTime) {
        this.eventEndTime = eventEndTime;
    }
    public long getDurationInMillSeconds() {
        return eventEndTime.getTimeInMillis() - eventStartTime.getTimeInMillis();
    }
}
