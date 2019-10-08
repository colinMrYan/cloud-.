package com.inspur.emmcloud.basemodule.util;

import android.content.Context;

import com.inspur.emmcloud.basemodule.bean.CalendarIdAndCloudIdBean;

import org.xutils.db.sqlite.WhereBuilder;

public class CalendarIdAndCloudScheduleIdCacheUtils {

    /**
     * 存储日程Id
     *
     * @param context
     * @param calendarIdAndCloudIdBean
     */
    public static void saveCalendarIdAndCloudIdBean(final Context context, final CalendarIdAndCloudIdBean calendarIdAndCloudIdBean) {
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(calendarIdAndCloudIdBean);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 通过云+id获取系统日历的id
     *
     * @param context
     * @param cloudScheduleId
     * @return
     */
    public static CalendarIdAndCloudIdBean getCalendarIdByCloudScheduleId(Context context, String cloudScheduleId) {
        try {
            return DbCacheUtils.getDb(context).selector(CalendarIdAndCloudIdBean.class)
                    .where("cloudScheduleId", "=", cloudScheduleId)
                    .findFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过云+日程id删除关联表里的记录
     *
     * @param context
     * @param cloudScheduleId
     * @return
     */
    public static void deleteByCloudScheduleId(Context context, String cloudScheduleId) {
        try {
            DbCacheUtils.getDb(context).delete(CalendarIdAndCloudIdBean.class,
                    WhereBuilder.b("cloudScheduleId", "=", cloudScheduleId));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
