package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.util.Base64;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.calendar.AccountType;
import com.inspur.emmcloud.bean.schedule.calendar.CalendarColor;
import com.inspur.emmcloud.bean.schedule.calendar.ScheduleCalendar;
import com.inspur.emmcloud.util.privates.cache.ScheduleCalendarCacheUtils;

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



    /**
     * 获取日程calendar的Icon
     *
     * @param schedule
     * @return
     */
    public static int getCalendarIconResId(Schedule schedule) {
        int eventColorIconResId = R.drawable.schedule_calendar_type_blue;
        ScheduleCalendar scheduleCalendar = ScheduleCalendarCacheUtils.getScheduleCalendar(BaseApplication.getInstance(), schedule.getScheduleCalendar());
        if (scheduleCalendar != null) {
            CalendarColor calendarColor = CalendarColor.getCalendarColor(scheduleCalendar.getColor());
            return calendarColor.getIconResId();
        }
        return eventColorIconResId;
    }


    /********************************************************************************/

    /**
     * 获取日程calendar的名称
     *
     * @param schedule
     * @return
     */
    public static String getCalendarName(Schedule schedule) {
        ScheduleCalendar scheduleCalendar = ScheduleCalendarCacheUtils.getScheduleCalendar(BaseApplication.getInstance(), schedule.getScheduleCalendar());
        return getScheduleCalendarShowName(scheduleCalendar);
    }



    public static String getScheduleCalendarShowName(ScheduleCalendar scheduleCalendar) {
        String scheduleCalendarShowName = BaseApplication.getInstance().getString(R.string.schedule_cloud_calendar);
        switch (AccountType.getAccountType(scheduleCalendar.getAcType())) {
            case EXCHANGE:
                scheduleCalendarShowName = scheduleCalendar.getAcName();
                break;
            case APP_MEETING:
                scheduleCalendarShowName = BaseApplication.getInstance().getString(R.string.schedule_cloud_meeting);
                break;
            case APP_SCHEDULE:
                scheduleCalendarShowName = BaseApplication.getInstance().getString(R.string.schedule_cloud_calendar);
                break;
        }
        return scheduleCalendarShowName;
    }


    public static String getHttpHeaderExtraValue(ScheduleCalendar scheduleCalendar) {
        String exchangeAuthHeaderValue = "";
        if (scheduleCalendar != null) {
            switch (AccountType.getAccountType(scheduleCalendar.getAcType())) {
                case EXCHANGE:
                    String exchangeAccount = scheduleCalendar.getAcName();
                    String exchangePassword = scheduleCalendar.getAcPW();
                    if (!StringUtils.isBlank(exchangeAccount) && !StringUtils.isBlank(exchangePassword)) {
                        exchangeAuthHeaderValue = exchangeAccount + ":" + exchangePassword;
                        exchangeAuthHeaderValue = Base64.encodeToString(exchangeAuthHeaderValue.getBytes(), Base64.NO_WRAP);
                    }
                    break;
                default:
                    break;
            }
        }

        return exchangeAuthHeaderValue;
    }

    public static String getHttpHeaderExtraKey(ScheduleCalendar scheduleCalendar) {
        String exchangeAuthHeaderKey = "";
        if (scheduleCalendar != null) {
            switch (AccountType.getAccountType(scheduleCalendar.getAcType())) {
                case EXCHANGE:
                    exchangeAuthHeaderKey = "x-ews-auth";
                    break;
                default:
                    break;
            }
        }
        return exchangeAuthHeaderKey;
    }


}
