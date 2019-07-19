package com.inspur.mvp_demo.meeting.model.bean;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.helper.StringUtil;
import org.xutils.db.annotation.Table;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/8.
 */

@Table(name = "Meeting")
public class Meeting extends Schedule {
    public Meeting() {

    }

    public Meeting(JSONObject obj) {
        super(obj);
    }

    public static List<Event> meetingEvent2EventList(List<Meeting> meetingList, Calendar selectCalendar) {
        List<Event> eventList = new ArrayList<>();
        for (Meeting meeting : meetingList) {
            Calendar meetingStartTime = meeting.getStartTimeCalendar();
            Calendar meetingEndTime = meeting.getEndTimeCalendar();
            if (TimeUtils.isContainTargetCalendarDay(selectCalendar, meetingStartTime, meetingEndTime)) {
                Calendar dayBeginCalendar = TimeUtils.getDayBeginCalendar(selectCalendar);
                Calendar dayEndCalendar = TimeUtils.getDayEndCalendar(selectCalendar);
                if (meetingStartTime.before(dayBeginCalendar)) {
                    meetingStartTime = dayBeginCalendar;
                }
                if (meetingEndTime.after(dayEndCalendar)) {
                    meetingEndTime = dayEndCalendar;
                }
                Event event = new Event(meeting.getId(), Schedule.TYPE_MEETING, meeting.getTitle(), meeting.getScheduleLocationObj().getDisplayName(), meetingStartTime, meetingEndTime, meeting);
                event.setAllDay(meeting.getAllDay());
                eventList.add(event);
            }
        }
        return eventList;
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            if (!StringUtils.isBlank(getId())) {
                obj.put("id", getId());
            }
            obj.put("title", getTitle());
            obj.put("type", getType());
            obj.put("owner", getOwner());
            obj.put("startTime", getStartTime());
            obj.put("endTime", getEndTime());
            obj.put("isAllDay", false);
            obj.put("isCommunity", false);
            obj.put("syncToLocal", false);
            obj.put("isAllDay", false);
            obj.put("note", getNote());
            if (!StringUtil.isBlank(getRemindEvent())) {
                JSONObject remindEventObj = new JSONObject(getRemindEvent());
                obj.put("remindEvent", remindEventObj);
            }
            if (!StringUtil.isBlank(getLocation())) {
                JSONObject locationObj = new JSONObject(getLocation());
                obj.put("location", locationObj);
            }
            if (!StringUtil.isBlank(getParticipants())) {
                JSONArray participantsArray = new JSONArray(getParticipants());
                obj.put("participants", participantsArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
