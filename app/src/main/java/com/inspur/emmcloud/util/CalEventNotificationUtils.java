package com.inspur.emmcloud.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.CalendarEvent;

public class CalEventNotificationUtils {
	/**
	 * 为Event设置通知提醒
	 * @param calEvent
	 */
	public static void setCalEventNotification(Context context,CalendarEvent calEvent) {
		// TODO Auto-generated method stub
		Long notificationId = calEvent.getCreationDateLong();
		if (notificationId == null) {
			return;
		}
		JPushLocalNotification ln = new JPushLocalNotification();
		ln.setBuilderId(0);
		ln.setContent(context.getString(R.string.alert));
		ln.setTitle(calEvent.getTitle());
		ln.setNotificationId(notificationId) ;
		Calendar startCalendar = calEvent.getLocalStartDate();
		startCalendar.add(Calendar.MILLISECOND, -300000);
		Calendar currentCalendar = Calendar.getInstance();
		if (startCalendar.after(currentCalendar)) {
			ln.setBroadcastTime(startCalendar.getTimeInMillis());

			Map<String , Object> map = new HashMap<String, Object>() ;
			String calEventJson = JSON.toJSONString(calEvent);
			map.put("calEvent",calEventJson) ;
			JSONObject json = new JSONObject(map) ;
			ln.setExtras(json.toString()) ;
			if(context != null){
				JPushInterface.removeLocalNotification(context, notificationId);
				JPushInterface.addLocalNotification(context, ln);
			}
		}
	}
}
