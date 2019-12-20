package com.inspur.emmcloud.schedule.util;

import android.content.Context;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.schedule.bean.calendar.Holiday;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenmch on 2019/4/19.
 */

public class HolidayCacheUtils {
    public static List<Holiday> getHolidayList(Context context, int year) {
       List<Holiday> holidayList = null;
       try {
           holidayList = DbCacheUtils.getDb(context).selector(Holiday.class).where("year","=",year).or("year","=",year+1).or("year","=",year-1).findAll();
       }catch (Exception e){
           e.printStackTrace();
       }
       if (holidayList == null){
           holidayList = new ArrayList<>();
       }
       return holidayList;
    }

    public static Map<Integer,List<Holiday>> getYearHolidayListMap(Context context){
        Map<Integer,List<Holiday>> yearHolidayListMap = new HashMap<>();
        try {
            List<Holiday> allHolidayList = DbCacheUtils.getDb(context).findAll(Holiday.class);
            for (Holiday holiday:allHolidayList){
                List<Holiday> yearHolidayList = yearHolidayListMap.get(holiday.getYear());
                if (yearHolidayList == null){
                    yearHolidayList = new ArrayList<>();
                }
                yearHolidayList.add(holiday);
                yearHolidayListMap.put(holiday.getYear(),yearHolidayList);
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        return yearHolidayListMap;
    }

    public static void saveHolidayList(final Context context,final int year,final List<Holiday> holidayList){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DbCacheUtils.getDb(context).delete(Holiday.class, WhereBuilder.b("year","=",year));
                    DbCacheUtils.getDb(context).save(holidayList);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();


    }

}
