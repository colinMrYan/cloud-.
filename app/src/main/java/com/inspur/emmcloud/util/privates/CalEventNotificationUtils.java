package com.inspur.emmcloud.util.privates;

import android.content.Context;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.work.CalendarEvent;
import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;

public class CalEventNotificationUtils {

    public static void setCalEventNotification(Context context, List<CalendarEvent> calEventList) {
        if (calEventList == null || calEventList.size() == 0) {
            return;
        }
        for (CalendarEvent calEvent : calEventList) {
            setCalEventNotification(context, calEvent);
        }
    }

    /**
     * 为Event设置通知提醒
     *
     * @param calEvent
     */
    public static void setCalEventNotification(Context context, CalendarEvent calEvent) {
        // TODO Auto-generated method stub
        Long notificationId = calEvent.getCreationDate().getTimeInMillis();
        if (notificationId == null) {
            return;
        }
        JPushLocalNotification ln = new JPushLocalNotification();
        ln.setBuilderId(0);
        ln.setContent(context.getString(R.string.alert));
        ln.setTitle(calEvent.getTitle());
        ln.setNotificationId(notificationId);
        Calendar startCalendar = calEvent.getStartDate();
        startCalendar.add(Calendar.MILLISECOND, -300000);
        Calendar currentCalendar = Calendar.getInstance();
        if (startCalendar.after(currentCalendar)) {
            ln.setBroadcastTime(startCalendar.getTimeInMillis());

            Map<String, Object> map = new HashMap<String, Object>();
            String calEventJson = JSONUtils.toJSONString(calEvent);
            map.put("calEvent", calEventJson);
            JSONObject json = new JSONObject(map);
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
