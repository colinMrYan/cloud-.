package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;
import com.inspur.emmcloud.util.common.LogUtils;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;

public class ScheduleAlertUtils {

    public static void setScheduleListAlert(Context context,final List<Schedule> scheduleList) {
        if (scheduleList == null || scheduleList.size() == 0) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Schedule schedule : scheduleList) {
                    if (schedule.getRemindEventObj() != null && schedule.getRemindEventObj().getAdvanceTimeSpan() != -1) {
                        setScheduleAlert(MyApplication.getInstance(), schedule);
                    }
                }
            }
        }).start();

    }

    public static void setMeetingListAlert(Context context,final List<Meeting> meetingList) {
        if (meetingList == null || meetingList.size() == 0) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Meeting schedule : meetingList) {
                    if (schedule.getRemindEventObj() != null && schedule.getRemindEventObj().getAdvanceTimeSpan() != -1) {
                        setScheduleAlert(MyApplication.getInstance(), schedule);
                    }
                }
            }
        }).start();

    }


    public static void setScheduleAlert(Context context, Schedule schedule) {
        // TODO Auto-generated method stub
        Long notificationId = schedule.getCreationTime();
        if (notificationId == null) {
            return;
        }
        JPushLocalNotification ln = new JPushLocalNotification();
        ln.setBuilderId(0);
        ln.setContent(context.getString(R.string.alert));
        ln.setTitle(schedule.getTitle());
        ln.setNotificationId(notificationId);
        Calendar calendar0 = Calendar.getInstance();
        calendar0.setTimeInMillis(schedule.getStartTime());
        LogUtils.jasonDebug("schedule.getStartTime()="+TimeUtils.calendar2FormatString(MyApplication.getInstance(),calendar0,TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
        long alertTime = schedule.getStartTime() - schedule.getRemindEventObj().getAdvanceTimeSpan()*1000;
        LogUtils.jasonDebug("schedule.title()="+schedule.getTitle());
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(alertTime);
        LogUtils.jasonDebug("alertTime="+TimeUtils.calendar2FormatString(MyApplication.getInstance(),calendar1,TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
        LogUtils.jasonDebug(" schedule.getRemindEventObj().getAdvanceTimeSpan()="+ schedule.getRemindEventObj().getAdvanceTimeSpan());
        if (alertTime > Calendar.getInstance().getTimeInMillis()){
            ln.setBroadcastTime(alertTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(alertTime);

            LogUtils.jasonDebug("time=="+TimeUtils.calendar2FormatString(MyApplication.getInstance(),calendar,TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
            JSONObject json = schedule.toCalendarEventJSONObject();
            ln.setExtras(json.toString());
            if (context != null) {
                JPushInterface.removeLocalNotification(context, notificationId);
                JPushInterface.addLocalNotification(context, ln);
            }
        }

    }

    /**
     * 清除本地日历通知
     *
     * @param context
     */
    public static void cancelAllCalEventNotification(Context context) {
        JPushInterface.clearLocalNotifications(context);
    }
}
