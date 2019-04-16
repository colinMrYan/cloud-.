package com.inspur.emmcloud.widget.calendardayview;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/3/28.
 */

public class TimeHourRow {
    private int id = 0;
    private int eventWidth = 0;
    private List<Event> eventList = new ArrayList<>();

    public TimeHourRow(int id, int maxWidth) {
        this.id = id;
        this.eventWidth = maxWidth;
    }

    public int getEventWidth() {
        return eventWidth;
    }

    public void setEventWidth(int eventWidth) {
        this.eventWidth = eventWidth;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public List<Event> getEventList() {
        return eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }
}
