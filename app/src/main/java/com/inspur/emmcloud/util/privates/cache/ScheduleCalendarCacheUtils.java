package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.schedule.calendar.ScheduleCalendar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/7/25.
 */

public class ScheduleCalendarCacheUtils {
    public static void saveScheduleCalendarList(Context context, List<ScheduleCalendar> scheduleCalendarList) {
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(scheduleCalendarList); // 存储消息
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void saveScheduleCalendar(Context context, ScheduleCalendar scheduleCalendar) {
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(scheduleCalendar);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static ScheduleCalendar getScheduleCalendar(Context context, String id) {
        try {
            if (!StringUtils.isBlank(id)) {
                return DbCacheUtils.getDb(context).findById(ScheduleCalendar.class, id);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

//    public static List<ScheduleCalendar> getScheduleCalendarList(Context context) {
//        return getScheduleCalendarList(context,true);
//    }
//
//    public static void removeExchangeScheduleCalendarList(Context context){
//        try {
//            DbCacheUtils.getDb(context).delete(ScheduleCalendar.class, WhereBuilder.b("acType","!=","Exchange"));
//
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }

    public static List<ScheduleCalendar> getScheduleCalendarList(Context context) {
        boolean isEnableExchange = PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_ENABLE_EXCHANGE, false);
        List<ScheduleCalendar> scheduleCalendarList = null;
        try {

            scheduleCalendarList = DbCacheUtils.getDb(context).selector(ScheduleCalendar.class).where("acType", "!=", "Exchange").findAll();
            if (isEnableExchange) {
                List<ScheduleCalendar> exchangeScheduleCalendarList = DbCacheUtils.getDb(context).selector(ScheduleCalendar.class).where("acType", "=", "Exchange").findAll();
                if (exchangeScheduleCalendarList != null) {
                    scheduleCalendarList.addAll(exchangeScheduleCalendarList);
                }

            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (scheduleCalendarList == null) {
            scheduleCalendarList = new ArrayList<>();
        }
        return scheduleCalendarList;
    }

    public static void removeScheduleCalendar(Context context, ScheduleCalendar scheduleCalendar) {
        try {
            DbCacheUtils.getDb(context).delete(scheduleCalendar);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static List<ScheduleCalendar> getScheduleCalendarList(Context context, boolean isOpen) {
        boolean isEnableExchange = PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(), Constant.PREF_SCHEDULE_ENABLE_EXCHANGE, false);
        List<ScheduleCalendar> scheduleCalendarList = null;
        try {
            if (isEnableExchange) {
                scheduleCalendarList = DbCacheUtils.getDb(context).selector(ScheduleCalendar.class).where("isOpen", "=", isOpen).findAll();
            } else {
                scheduleCalendarList = DbCacheUtils.getDb(context).selector(ScheduleCalendar.class).where("acType", "!=", "Exchange").and("isOpen", "=", isOpen).findAll();
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (scheduleCalendarList == null) {
            scheduleCalendarList = new ArrayList<>();
        }
        return scheduleCalendarList;
    }

    public ScheduleCalendar getScheduleCalendarById(Context context, String id) {
        try {
            return DbCacheUtils.getDb(context).findById(ScheduleCalendar.class, id); // 存储消息
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
