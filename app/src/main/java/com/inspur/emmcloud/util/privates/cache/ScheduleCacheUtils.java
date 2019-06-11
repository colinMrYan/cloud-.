package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.schedule.Schedule;

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
