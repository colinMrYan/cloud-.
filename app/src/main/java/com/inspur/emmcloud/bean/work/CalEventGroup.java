package com.inspur.emmcloud.bean.work;

import com.inspur.emmcloud.bean.schedule.calendar.CalendarEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CalEventGroup implements Comparator {
    private int key;
    private List<CalendarEvent> calEventList = new ArrayList<CalendarEvent>();

    public CalEventGroup() {

    }

    public CalEventGroup(int key, List<CalendarEvent> calEventList) {
        this.key = key;
        this.calEventList = calEventList;

    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public List<CalendarEvent> getCalEventList() {
        if (calEventList == null) {
            calEventList = new ArrayList<CalendarEvent>();
        }
        return calEventList;
    }

    public void setCalEventList(List<CalendarEvent> calEventList) {
        this.calEventList = calEventList;
    }

    @Override
    public int compare(Object lhs, Object rhs) {
        CalEventGroup calEventGroupA = (CalEventGroup) lhs;
        CalEventGroup calEventGroupB = (CalEventGroup) rhs;
        long diff = calEventGroupA.getKey() - calEventGroupB.getKey();
        if (diff > 0) {
            return 1;
        } else if (diff == 0) {
            return 0;
        } else {
            return -1;
        }
    }
}
