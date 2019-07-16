package com.inspur.emmcloud.widget.calendardayview;


import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.schedule.Schedule;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenmch on 2019/3/29.
 */

public class Event {
    public String eventId;
    public String eventType;
    public String eventTitle;
    public String eventSubTitle;
    public Calendar eventStartTime;
    public Calendar eventEndTime;
    private int index = -1;
    private boolean isAllDay = false;
    private Object eventObj;
    private String calendarType;
    private String owner = "";

    public Event(String eventId, String eventType, String eventTitle, String eventSubTitle, Calendar eventStartTime, Calendar eventEndTime, Object eventObj, String calendarType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventTitle = eventTitle;
        this.eventSubTitle = eventSubTitle;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
        this.eventObj = eventObj;
        this.calendarType = calendarType;
    }

    public Event(String eventId, String eventType, String eventTitle, String eventSubTitle, Calendar eventStartTime, Calendar eventEndTime, Object eventObj, String calendarType, String owner) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.eventTitle = eventTitle;
        this.eventSubTitle = eventSubTitle;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
        this.eventObj = eventObj;
        this.calendarType = calendarType;
        this.owner = owner;

    }

    public static List<Event> removeEventByType(List<Event> eventList, String eventType) {
        if (eventList.size() > 0) {
            Iterator<Event> iterator = eventList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getEventType().equals(eventType)) {
                    iterator.remove();
                }
            }
        }
        return eventList;
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

    public Calendar getDayEventStartTime(Calendar selectCalendar) {
        if (!TimeUtils.isSameDay(eventStartTime, selectCalendar)) {
            return TimeUtils.getDayBeginCalendar(selectCalendar);
        }
        return eventStartTime;
    }

    public Calendar getDayEventEndTime(Calendar selectCalendar) {
        if (!TimeUtils.isSameDay(eventEndTime, selectCalendar)) {
            return TimeUtils.getDayEndCalendar(selectCalendar);
        }
        return eventEndTime;
    }

    public boolean canDelete() {
        if (getEventType().equals(Schedule.TYPE_CALENDAR)) {
            return true;
        }
        if (getEventType().equals(Schedule.TYPE_MEETING)) {
            boolean isAdmin = PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_IS_MEETING_ADMIN,
                    false);
            if (isAdmin || (getOwner().equals(BaseApplication.getInstance().getUid()) && getEventEndTime().after(Calendar.getInstance()))) {
                return true;
            }
        }
        return false;
    }

    public boolean canModify() {
        boolean isOwner = getOwner().equals(BaseApplication.getInstance().getUid());
        if (getEventType().equals(Schedule.TYPE_CALENDAR) && isOwner) {
            return true;
        }
        if (getEventType().equals(Schedule.TYPE_MEETING) && isOwner && getEventEndTime().after(Calendar.getInstance())) {
            return true;
        }
        return false;
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

    public long getDayDurationInMillSeconds(Calendar selectCalendar) {

        return getDayEventEndTime(selectCalendar).getTimeInMillis() - getDayEventStartTime(selectCalendar).getTimeInMillis();
    }

    public Object getEventObj() {
        return eventObj;
    }

    public void setEventObj(Object eventObj) {
        this.eventObj = eventObj;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    public int getEventIconResId(boolean isSelect) {
        int eventIconResId = -1;
        if (getEventType().equals(Schedule.TYPE_CALENDAR)) {
            eventIconResId = isSelect ? R.drawable.ic_schedule_event_calendar_select : R.drawable.ic_schedule_event_calendar_normal;
        } else if (getEventType().equals(Schedule.TYPE_MEETING)) {
            eventIconResId = isSelect ? R.drawable.ic_schedule_event_meeting_select : R.drawable.ic_schedule_event_meeting_normal;
        } else {
            eventIconResId = isSelect ? R.drawable.ic_schedule_event_task_select : R.drawable.ic_schedule_event_task_normal;
        }
        return eventIconResId;
    }

    public int getEventColorResId() {
        int eventColorIconResId = -1;
        if (getEventType().equals(Schedule.TYPE_CALENDAR)) {
            eventColorIconResId = R.drawable.schedule_calendar_type_orange;
        } else if (getEventType().equals(Schedule.TYPE_MEETING)) {
            eventColorIconResId = R.drawable.schedule_calendar_type_yellow;
        } else {
            eventColorIconResId = R.drawable.schedule_calendar_type_purple;
        }
        return eventColorIconResId;
    }

    public String getShowEventSubTitle(Context context, Calendar selectCalendar) {
        String showEventSubTitle = "";
        if (getEventType().equals(Schedule.TYPE_MEETING)) {
            showEventSubTitle = getEventSubTitle();
        } else {
            if (TimeUtils.isSameDay(selectCalendar, Calendar.getInstance())) {
                showEventSubTitle = context.getString(R.string.today);
            } else {
                showEventSubTitle = TimeUtils.calendar2FormatString(context, getEventEndTime(), TimeUtils.FORMAT_MONTH_DAY);
            }
        }
        return showEventSubTitle;
    }
}
