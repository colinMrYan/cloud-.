package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.widget.calendardayview.Event;

import org.xutils.db.annotation.Table;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/8.
 */

@Table(name = "Meeting")
public class Meeting extends Schedule {
    public static List<Event> meetingEvent2EventList(List<Meeting> meetingList, Calendar selectCalendar) {
        List<Event> eventList = new ArrayList<>();
        for (Meeting meeting : meetingList) {
            Calendar meetingStartTime =  meeting.getStartTimeCalendar();
            Calendar meetingEndTime = meeting.getEndTimeCalendar();
            if (TimeUtils.isContainTargentCalendarDay(selectCalendar, meetingStartTime, meetingEndTime)) {
                Calendar dayBeginCalendar = TimeUtils.getDayBeginCalendar(selectCalendar);
                Calendar dayEndCalendar = TimeUtils.getDayEndCalendar(selectCalendar);
                if (meetingStartTime.before(dayBeginCalendar)) {
                    meetingStartTime = dayBeginCalendar;
                }
                if (meetingEndTime.after(dayEndCalendar)) {
                    meetingEndTime = dayEndCalendar;
                }
                Event event = new Event(meeting.getId(), Event.TYPE_MEETING,meeting.getTitle(),meeting.getScheduleLocationObj().getDisplayName(), meetingStartTime, meetingEndTime,meeting);
                event.setAllDay(meeting.getAllDay());
                eventList.add(event);
            }
        }
        return eventList;
    }
}
