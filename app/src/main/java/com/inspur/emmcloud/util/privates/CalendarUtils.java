package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.widget.roundbutton.CustomRoundButtonDrawable;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.widget.calendardayview.Event;

public class CalendarUtils {
    public static int getColor(Context context, String color) {
        int displayColor = -1;
        if (color.equals("PINK")) {
            displayColor = context.getResources().getColor(R.color.cal_pink);
        } else if (color.equals("ORANGE")) {
            displayColor = context.getResources().getColor(R.color.cal_orange);
        } else if (color.equals("YELLOW")) {
            displayColor = context.getResources().getColor(R.color.cal_yellow);
        } else if (color.equals("GREEN")) {
            displayColor = context.getResources().getColor(R.color.cal_green);
        } else if (color.equals("BLUE")) {
            displayColor = context.getResources().getColor(R.color.cal_blue);
        } else if (color.equals("PURPLE")) {
            displayColor = context.getResources().getColor(R.color.cal_pink);
        } else if (color.equals("BROWN")) {
            displayColor = context.getResources().getColor(R.color.cal_purple);
        } else {
            displayColor = context.getResources().getColor(R.color.transparent);
        }
        return displayColor;
    }

    public static int getTaskTagResId(String color) {
        int displayColor = -1;
        if (color.equals("ORANGE")) {
            displayColor = R.drawable.icon_orange_circle;
        } else if (color.equals("YELLOW")) {
            displayColor = R.drawable.icon_yellow_circle;
        } else if (color.equals("GREEN")) {
            displayColor = R.drawable.icon_green_circle;
        } else if (color.equals("BLUE")) {
            displayColor = R.drawable.icon_blue_circle;
        } else if (color.equals("PURPLE")) {
            displayColor = R.drawable.icon_purple_circle;
        } else if (color.equals("BROWN")) {
            displayColor = R.drawable.icon_brown_circle;
        } else if (color.equals("PINK")) {
            displayColor = R.drawable.icon_red_circle;
        } else {
            displayColor = R.drawable.icon_blue_circle;
        }
        return displayColor;
    }

    public static int getCalendarTypeResId(String color) {
        int resId = -1;
        if (color.equals("ORANGE")) {
            resId = R.drawable.schedule_calendar_type_orange;
        } else if (color.equals("YELLOW")) {
            resId = R.drawable.schedule_calendar_type_yellow;
        } else if (color.equals("GREEN")) {
            resId = R.drawable.schedule_calendar_type_green;
        } else if (color.equals("PURPLE")) {
            resId = R.drawable.schedule_calendar_type_purple;
        } else if (color.equals("BROWN")) {
            resId = R.drawable.schedule_calendar_type_brown;
        } else if (color.equals("PINK")) {
            resId = R.drawable.schedule_calendar_type_red;
        } else if (color.equals("BLUE")) {
            resId = R.drawable.schedule_calendar_type_blue;
        }
        return resId;
    }

    public static int getCalendarDayViewEventBgResId(String color) {
        int resId = R.drawable.ic_schedule_calendar_view_event_bg_blue;
        if (color.equals("ORANGE")) {
            resId = R.drawable.ic_schedule_calendar_view_event_bg_orange;
        } else if (color.equals("PURPLE")) {
            resId = R.drawable.ic_schedule_calendar_view_event_bg_purple;
        } else if (color.equals("BLUE")) {
            resId = R.drawable.ic_schedule_calendar_view_event_bg_blue;
        } else if (color.equals("GREEN")) {
            resId = R.drawable.ic_schedule_calendar_view_event_bg_green;
        }
        return resId;
    }


    public static String getCalendarName(Event event) {
        String calendarName = "";
        switch (event.getEventType()) {
            case Schedule.TYPE_CALENDAR:
                if (event.getCalendarType().equals("exchange")) {
                    calendarName = "Exchange";
                } else {
                    calendarName = BaseApplication.getInstance().getString(R.string.schedule_calendar_my_schedule);
                }

                break;
            case Schedule.TYPE_MEETING:
                if (event.getCalendarType().equals("exchange")) {
                    calendarName = "Exchange";
                } else {
                    calendarName = BaseApplication.getInstance().getString(R.string.schedule_calendar_my_meeting);
                }

                break;
            default:
                break;
        }
        return calendarName;
    }

    public static Drawable getEventBgNormalDrawable(Event event) {
        Drawable drawable = null;
        switch (event.getEventType()) {
            case Schedule.TYPE_CALENDAR:
                if (event.getCalendarType().equals("exchange")) {
                    drawable = ContextCompat.getDrawable(BaseApplication.getInstance(), (R.drawable.ic_schedule_calendar_view_event_bg_green));
                } else {
                    drawable = ContextCompat.getDrawable(BaseApplication.getInstance(), (R.drawable.ic_schedule_calendar_view_event_bg_blue));
                }

                break;
            case Schedule.TYPE_MEETING:
                if (event.getCalendarType().equals("exchange")) {
                    drawable = ContextCompat.getDrawable(BaseApplication.getInstance(), (R.drawable.ic_schedule_calendar_view_event_bg_green));
                } else {
                    drawable = ContextCompat.getDrawable(BaseApplication.getInstance(), (R.drawable.ic_schedule_calendar_view_event_bg_orange));
                }

                break;
            default:
                drawable = ContextCompat.getDrawable(BaseApplication.getInstance(), (R.drawable.ic_schedule_calendar_view_event_bg_blue));
                break;
        }
        return drawable;

    }

    public static CustomRoundButtonDrawable getEventBgSelectDrawable(Event event) {
        CustomRoundButtonDrawable drawableSelected = new CustomRoundButtonDrawable();
        ColorStateList colorStateList = null;

        switch (event.getEventType()) {
            case Schedule.TYPE_CALENDAR:

                if (event.getCalendarType().equals("exchange")) {
                    colorStateList = ColorStateList.valueOf(Color.parseColor("#7ED321"));
                } else {
                    colorStateList = ColorStateList.valueOf(Color.parseColor("#36A5F6"));
                }

                break;
            case Schedule.TYPE_MEETING:
                if (event.getCalendarType().equals("exchange")) {
                    colorStateList = ColorStateList.valueOf(Color.parseColor("#7ED321"));
                } else {
                    colorStateList = ColorStateList.valueOf(Color.parseColor("#FF8603"));
                }

                break;
            default:
                colorStateList = ColorStateList.valueOf(Color.parseColor("#36A5F6"));
                break;
        }
        drawableSelected.setBgData(colorStateList);
        drawableSelected.setCornerRadius(DensityUtil.dip2px(2));
        drawableSelected.setIsRadiusAdjustBounds(false);
        return drawableSelected;
    }

    public static int getCalendarTypeImgResId(Event event) {
        int resId = -1;
        switch (event.getEventType()) {
            case Schedule.TYPE_CALENDAR:
                if (event.getCalendarType().equals("exchange")) {
                    resId = CalendarUtils.getCalendarTypeResId("GREEN");
                } else {
                    resId = CalendarUtils.getCalendarTypeResId("BLUE");
                }
                break;
            case Schedule.TYPE_MEETING:
                if (event.getCalendarType().equals("exchange")) {
                    resId = CalendarUtils.getCalendarTypeResId("GREEN");
                } else {
                    resId = CalendarUtils.getCalendarTypeResId("ORANGE");
                }
                break;
            default:
                break;
        }
        return resId;
    }

    public static int getCalendarTypeColor(Event event) {
        Integer color = null;
        switch (event.getEventType()) {
            case Schedule.TYPE_CALENDAR:
                if (event.getCalendarType().equals("exchange")) {
                    color = Color.parseColor("#7ED321");
                } else {
                    color = Color.parseColor("#36A5F6");
                }

                break;
            case Schedule.TYPE_MEETING:
                if (event.getCalendarType().equals("exchange")) {
                    color = Color.parseColor("#7ED321");
                } else {
                    color = Color.parseColor("#FF8603");
                }

                break;
            default:
                color = Color.parseColor("#36A5F6");
                break;
        }
        return color;
    }
}
