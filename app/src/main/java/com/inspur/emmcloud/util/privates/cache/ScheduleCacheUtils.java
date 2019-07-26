package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.calendar.AccountType;
import com.inspur.emmcloud.bean.schedule.calendar.ScheduleCalendar;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/11.
 */

public class ScheduleCacheUtils {

    public static void saveScheduleList(Context context, List<Schedule> scheduleList) {
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(scheduleList); // 存储消息
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void removeScheduleList(Context context, List<String> scheduleIdList) {
        try {
            if (scheduleIdList.size() > 0) {
                DbCacheUtils.getDb(context).delete(Schedule.class, WhereBuilder.b("id", "in", scheduleIdList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void removeScheduleList(Context context, Calendar startTime, Calendar endTime, ScheduleCalendar scheduleCalendar) {
        try {
            long startTimeLong = startTime.getTimeInMillis();
            long endTimeLong = endTime.getTimeInMillis();
            if (scheduleCalendar == null || scheduleCalendar.getId().equals(AccountType.APP_SCHEDULE.toString()) || scheduleCalendar.getId().equals(AccountType.APP_MEETING.toString())) {
                List<String> accountTypeList = new ArrayList<>();
                accountTypeList.add(AccountType.APP_SCHEDULE.toString());
                DbCacheUtils.getDb(context).delete(Schedule.class, WhereBuilder.b("endTime", ">=", startTimeLong)
                        .and("startTime", "<=", endTimeLong).and("scheduleCalendar", "in", accountTypeList));
            } else {
                DbCacheUtils.getDb(context).delete(Schedule.class, WhereBuilder.b("endTime", ">=", startTimeLong)
                        .and("startTime", "<=", endTimeLong).and("scheduleCalendar", "=", scheduleCalendar.getId()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void removeSchedule(Context context, String scheduleId) {
        try {
            DbCacheUtils.getDb(context).deleteById(Schedule.class, scheduleId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static List<Schedule> getScheduleList(Context context, Calendar startTime, Calendar endTime) {
        List<Schedule> scheduleList = null;
        try {
            long startTimeLong = startTime.getTimeInMillis();
            long endTimeLong = endTime.getTimeInMillis();
            scheduleList = DbCacheUtils.getDb(context).selector(Schedule.class).where(WhereBuilder.b("endTime", ">=", startTimeLong)
                    .and("startTime", "<=", endTimeLong)).findAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (scheduleList == null) {
            scheduleList = new ArrayList<>();
        }
        return scheduleList;
    }

    public static List<Schedule> getScheduleListByIsExchange(Context context, Calendar startTime, Calendar endTime, boolean isExchange, boolean isSchedule) {
        List<Schedule> scheduleList = null;
        try {
            long startTimeLong = startTime.getTimeInMillis();
            long endTimeLong = endTime.getTimeInMillis();
            if (isExchange && isSchedule) {
                scheduleList = DbCacheUtils.getDb(context).selector(Schedule.class).where(WhereBuilder.b("endTime", ">=", startTimeLong)
                        .and("startTime", "<=", endTimeLong)).findAll();
            } else if (!isExchange && isSchedule) {
                scheduleList = DbCacheUtils.getDb(context).selector(Schedule.class).where(WhereBuilder.b("endTime", ">=", startTimeLong)
                        .and("startTime", "<=", endTimeLong).and("type", "!=", Schedule.CALENDAR_TYPE_EXCHANGE)).findAll();
            } else if (isExchange && !isSchedule) {
                scheduleList = DbCacheUtils.getDb(context).selector(Schedule.class).where(WhereBuilder.b("endTime", ">=", startTimeLong)
                        .and("startTime", "<=", endTimeLong).and("type", "!=", Schedule.CALENDAR_TYPE_EXCHANGE)).findAll();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (scheduleList == null) {
            scheduleList = new ArrayList<>();
        }
        return scheduleList;
    }


    public static List<Schedule> getScheduleList(Context context, Calendar startTime, Calendar endTime, List<ScheduleCalendar> scheduleCalendarList) {
        List<Schedule> scheduleList = null;
        List<String> scheduleCalendarIdList = new ArrayList<>();
        for (ScheduleCalendar scheduleCalendar : scheduleCalendarList) {
            if (scheduleCalendar.isOpen()) {
                scheduleCalendarIdList.add(scheduleCalendar.getId());
            }

        }
        if (scheduleCalendarIdList.size() > 0) {
            try {
                long startTimeLong = startTime.getTimeInMillis();
                long endTimeLong = endTime.getTimeInMillis();
                scheduleList = DbCacheUtils.getDb(context).selector(Schedule.class).where(WhereBuilder.b("endTime", ">=", startTimeLong)
                        .and("startTime", "<=", endTimeLong).and("scheduleCalendar", "in", scheduleCalendarIdList)).findAll();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        if (scheduleList == null) {
            scheduleList = new ArrayList<>();
        }
        return scheduleList;
    }


    /**
     * 通过id获取缓存日程据
     */
    public static Schedule getDBScheduleById(Context context, String id) {
        Schedule schedule = new Schedule();
        try {
            schedule = DbCacheUtils.getDb(context).findById(Schedule.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return schedule;
    }

}
