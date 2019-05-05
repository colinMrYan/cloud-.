package com.inspur.emmcloud.bean.work;

import com.inspur.emmcloud.bean.schedule.calendar.CalendarEvent;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class GetCalendarEventsResult {
    private List<CalendarEvent> calendarEventList = new ArrayList<CalendarEvent>();

    public GetCalendarEventsResult(String response) {
        try {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                calendarEventList.add(new CalendarEvent(array.getJSONObject(i)));
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    public List<CalendarEvent> getCalEventList() {
        return calendarEventList;
    }
}
