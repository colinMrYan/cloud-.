package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.bean.schedule.calendar.Holiday;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/4/19.
 */

public class HolidayCacheUtils {
    public static List<Holiday> getHolidayList(Context context){
        List<Holiday> holidayList = new ArrayList<>();
        holidayList.add(new Holiday(2018,12,29,true));
        holidayList.add(new Holiday(2018,12,30));
        holidayList.add(new Holiday(2018,12,31));
        holidayList.add(new Holiday(2019,1,1));

        holidayList.add(new Holiday(2018,2,2,true));
        holidayList.add(new Holiday(2018,2,3,true));
        holidayList.add(new Holiday(2019,2,4));
        holidayList.add(new Holiday(2019,2,5));
        holidayList.add(new Holiday(2019,2,6));
        holidayList.add(new Holiday(2019,2,7));
        holidayList.add(new Holiday(2019,2,8));
        holidayList.add(new Holiday(2019,2,9));
        holidayList.add(new Holiday(2019,2,10));

        holidayList.add(new Holiday(2019,4,5));
        holidayList.add(new Holiday(2019,4,6));
        holidayList.add(new Holiday(2019,4,7));

        holidayList.add(new Holiday(2019,4,28,true));
        holidayList.add(new Holiday(2019,5,1));
        holidayList.add(new Holiday(2019,5,2));
        holidayList.add(new Holiday(2019,5,3));
        holidayList.add(new Holiday(2019,5,4));
        holidayList.add(new Holiday(2019,5,5,true));

        holidayList.add(new Holiday(2019,6,7));
        holidayList.add(new Holiday(2019,6,8));
        holidayList.add(new Holiday(2019,6,9));

        holidayList.add(new Holiday(2019,9,13));
        holidayList.add(new Holiday(2019,9,14));
        holidayList.add(new Holiday(2019,9,15));

        holidayList.add(new Holiday(2019,9,29,true));
        holidayList.add(new Holiday(2019,10,1));
        holidayList.add(new Holiday(2019,10,2));
        holidayList.add(new Holiday(2019,10,3));
        holidayList.add(new Holiday(2019,10,4));
        holidayList.add(new Holiday(2019,10,5));
        holidayList.add(new Holiday(2019,10,6));
        holidayList.add(new Holiday(2019,10,7));
        holidayList.add(new Holiday(2019,10,12,true));
        return holidayList;
    }

}
