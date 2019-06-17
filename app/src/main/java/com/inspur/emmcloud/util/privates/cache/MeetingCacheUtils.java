package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;

import org.xutils.db.sqlite.WhereBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chenmch on 2019/4/11.
 */

public class MeetingCacheUtils {

    public static void saveMeetingList(final Context context, final List<Meeting> meetingList) {
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(meetingList); // 存储消息
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void removeMeetingList(Context context, List<String> meetingIdList) {
        try {
            if (meetingIdList.size() > 0) {
                DbCacheUtils.getDb(context).delete(Meeting.class, WhereBuilder.b("id", "in", meetingIdList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static List<Meeting> getMeetingList(final Context context, Calendar startTime, Calendar endTime) {
        List<Meeting> meetingList = null;
        try {
            long startTimeLong = startTime.getTimeInMillis();
            long endTimeLong = endTime.getTimeInMillis();
            meetingList = DbCacheUtils.getDb(context).selector(Meeting.class).where(WhereBuilder.b("startTime", ">", startTimeLong)
                    .and("endTime", "<", endTimeLong)).or(WhereBuilder.b("startTime", "<=", startTimeLong)
                    .and("endTime", ">", endTimeLong)).or(WhereBuilder.b("startTime", "<=", endTimeLong)
                    .and("endTime", ">=", endTimeLong)).orderBy("lastTime", true).findAll();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (meetingList == null) {
            meetingList = new ArrayList<>();
        }
        return meetingList;
    }

    /**
     *通过id获取缓存meeting数据
     */
    public static Meeting getDBMeetingById(Context context, String id) {
        Meeting meeting = new Meeting();
        try {
            meeting = DbCacheUtils.getDb(context).findById(Meeting.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return meeting;
    }


}
