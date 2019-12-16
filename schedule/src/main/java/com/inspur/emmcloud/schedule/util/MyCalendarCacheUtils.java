package com.inspur.emmcloud.schedule.util;

import android.content.Context;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.schedule.bean.MyCalendar;

import java.util.ArrayList;
import java.util.List;

/**
 * 频道列表缓存处理类
 *
 * @author Administrator
 */
public class MyCalendarCacheUtils {

    /**
     * 存储日历列表
     *
     * @param context
     */
    public static void saveMyCalendarList(final Context context,
                                          final List<MyCalendar> myCalendarList) {

        // TODO Auto-generated method stub
        try {
            if (myCalendarList == null || myCalendarList.size() == 0) {
                return;
            }
            DbCacheUtils.getDb(context).delete(MyCalendar.class);
            DbCacheUtils.getDb(context).saveOrUpdate(myCalendarList);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 获取我的所有日历
     *
     * @param context
     * @return
     */
    public static List<MyCalendar> getAllMyCalendarList(final Context context) {
        List<MyCalendar> myCalendarList = null;
        try {
            myCalendarList = DbCacheUtils.getDb(context).findAll(MyCalendar.class);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (myCalendarList == null) {
            myCalendarList = new ArrayList<MyCalendar>();
        }
        return myCalendarList;
    }

    /**
     * 存储单个日历
     *
     * @param context
     * @param channel
     */
    public static void saveMyCalendar(Context context, MyCalendar myCalendar) {
        try {
            if (myCalendar == null) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(myCalendar);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
