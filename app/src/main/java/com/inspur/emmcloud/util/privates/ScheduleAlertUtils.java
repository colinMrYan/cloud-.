package com.inspur.emmcloud.util.privates;

import android.app.NotificationManager;
import android.content.Context;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.schedule.Schedule;
import com.inspur.emmcloud.bean.schedule.meeting.Meeting;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ScheduleAlertUtils {

    public static void setScheduleListAlert(Context context,final List<Schedule> scheduleList) {
//        if (scheduleList == null || scheduleList.size() == 0) {
//            return;
//        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (Schedule schedule : scheduleList) {
//                    if (schedule.getRemindEventObj() != null && schedule.getRemindEventObj().getAdvanceTimeSpan() != -1) {
//                        setScheduleAlert(MyApplication.getInstance(), schedule,Schedule.TYPE_CALENDAR);
//                    }
//                }
//            }
//        }).start();

    }

    public static void setMeetingListAlert(Context context,final List<Meeting> meetingList) {
//        if (meetingList == null || meetingList.size() == 0) {
//            return;
//        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (Meeting schedule : meetingList) {
//                    if (schedule.getRemindEventObj() != null && schedule.getRemindEventObj().getAdvanceTimeSpan() != -1) {
//                        setScheduleAlert(MyApplication.getInstance(), schedule, Schedule.TYPE_MEETING
//                        );
//                    }
//                }
//            }
//        }).start();

    }


    public static void setScheduleAlert(Context context, Schedule schedule,String type) {
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
        long alertTime = schedule.getStartTime() - schedule.getRemindEventObj().getAdvanceTimeSpan()*1000;
        //当提醒时间在当前时间之前时，不设置提醒
        if (alertTime > Calendar.getInstance().getTimeInMillis()){
            ln.setBroadcastTime(alertTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(alertTime);
            JSONObject obj = schedule.toCalendarEventJSONObject();
            JSONObject object = new JSONObject();
            try {
                object.put("schedule",obj);
                object.put("type",type);
                ln.setExtras(object.toString());
                if (context != null) {
                    JPushInterface.removeLocalNotification(context, notificationId);
                    JPushInterface.addLocalNotification(context, ln);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    /**
     * 清除本地日历通知
     *
     * @param context
     */
    public static void cancelAllCalEventNotification(Context context) {
//        JPushInterface.clearLocalNotifications(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}
