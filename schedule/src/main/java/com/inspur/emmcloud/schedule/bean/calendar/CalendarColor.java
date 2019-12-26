package com.inspur.emmcloud.schedule.bean.calendar;

import com.inspur.emmcloud.schedule.R;

/**
 * Created by chenmch on 2019/7/25.
 */

public enum CalendarColor {
    RED(R.color.schedule_red, R.drawable.schedule_calendar_type_red, R.drawable.schedule_calendar_view_event_bg_red),
    ORANGE(R.color.schedule_orange, R.drawable.schedule_calendar_type_orange, R.drawable.schedule_calendar_view_event_bg_orange),
    YELLOW(R.color.schedule_yellow, R.drawable.schedule_calendar_type_yellow, R.drawable.schedule_calendar_view_event_bg_yellow),
    BLUE(R.color.schedule_blue, R.drawable.schedule_calendar_type_blue, R.drawable.schedule_calendar_view_event_bg_blue),
    GREEN(R.color.schedule_green, R.drawable.schedule_calendar_type_green, R.drawable.schedule_calendar_view_event_bg_green),
    PURPLE(R.color.schedule_purple, R.drawable.schedule_calendar_type_purple, R.drawable.schedule_calendar_view_event_bg_purple),
    BROWN(R.color.schedule_brown, R.drawable.schedule_calendar_type_brown, R.drawable.schedule_calendar_view_event_bg_brown);
    public int color;
    public int iconResId;
    public int eventBgNormalResId;

    CalendarColor(int color, int iconResId, int eventBgNormalResId) {
        this.color = color;
        this.iconResId = iconResId;
        this.eventBgNormalResId = eventBgNormalResId;
    }

    public static CalendarColor getCalendarColor(String color) {
        try {
            return CalendarColor.valueOf(color);
        } catch (Exception e) {
            e.printStackTrace();
            return CalendarColor.BLUE;
        }
    }

    public int getColor() {
        return color;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getEventBgNormalResId() {
        return eventBgNormalResId;
    }
}