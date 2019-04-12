package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.bean.schedule.Schedule;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/11.
 */

public class ScheduleCacheUtils {

    public static void saveScheduleList(final Context context, final List<Schedule> scheduleList) {
        try {

            DbCacheUtils.getDb(context).saveOrUpdate(scheduleList); // 存储消息
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static List<Schedule> getScheduleList(final Context context, Calendar startTime, Calendar endTime) {
        List<Schedule> scheduleList = null;
        try {
            long startTimeLong = startTime.getTimeInMillis();
            long endTimeLong = endTime.getTimeInMillis();
            scheduleList = DbCacheUtils.getDb(context).selector(Schedule.class).where(WhereBuilder.b("startTime", ">", startTimeLong)
                    .and("endTime", "<", endTimeLong)).or(WhereBuilder.b("startTime", "<=", startTimeLong)
                    .and("endTime", ">", endTimeLong)).or(WhereBuilder.b("startTime", "<=", endTimeLong)
                    .and("endTime", ">=", endTimeLong)).orderBy("lastTime",true).findAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (scheduleList == null){
            scheduleList = new ArrayList<>();
        }
        return scheduleList;
    }


}
