package com.inspur.emmcloud.basemodule.util;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.TimeZone;

public class CalendarReminderUtils {

    private static String CALENDER_URL = "content://com.android.calendar/calendars";
    private static String CALENDER_EVENT_URL = "content://com.android.calendar/events";
    private static String CALENDER_REMINDER_URL = "content://com.android.calendar/reminders";

    //此处是建立默认账号时所需的命名信息
    private static String CALENDARS_NAME = "boohee";
    private static String CALENDARS_ACCOUNT_NAME = "BOOHEE@boohee.com";
    private static String CALENDARS_ACCOUNT_TYPE = "com.android.boohee";
    private static String CALENDARS_DISPLAY_NAME = "BOOHEE账户";

    /**
     * 检查是否已经添加了日历账户，如果没有添加先添加一个日历账户再查询
     * 获取账户成功返回账户id，否则返回-1
     */
    private static int checkAndAddCalendarAccount(Context context) {
        int oldId = checkCalendarAccount(context);
        if (oldId >= 0) {
            return oldId;
        } else {
            long addId = addCalendarAccount(context);
            if (addId >= 0) {
                return checkCalendarAccount(context);
            } else {
                return -1;
            }
        }
    }

    /**
     * 检查是否存在现有账户，存在则返回账户id，否则返回-1
     *
     * @param context
     * @return
     */
    private static int checkCalendarAccount(Context context) {
        Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDER_URL), null, null, null, null);
        try {
            if (userCursor == null) {
                //查询返回空值
                return -1;
            }
            int count = userCursor.getCount();
            if (count > 0) {
                //存在现有账户，取第一个账户的id返回
                userCursor.moveToFirst();
                return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            } else {
                return -1;
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    /**
     * 添加日历账户，账户创建成功则返回账户id，否则返回-1
     *
     * @param context
     * @return
     */
    private static long addCalendarAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = Uri.parse(CALENDER_URL);
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build();

        Uri result = context.getContentResolver().insert(calendarUri, value);
        long id = result == null ? -1 : ContentUris.parseId(result);
        return id;
    }

    /**
     * 添加日历事件
     * 调用者需要自己判断返回URI是否为空
     * 如果返回URI为空  说明日程添加失败
     *
     * @param context
     * @param title        日程标题
     * @param description  日程描述
     * @param reminderTime 提醒事件，也就是日程开始时间
     * @param endTime      日程结束时间
     * @param scheduleId   云+日程Id
     * @param isAllDay     是不是整天
     * @param previousDate 提前多长时间提醒 单位是分钟
     */
    public static Uri saveCloudSchedule(Context context, String title, String description,
                                        long reminderTime, long endTime,
                                        String scheduleId, boolean isAllDay, int previousDate) {
        if (context == null) {
            return null;
        }
        int calId = checkAndAddCalendarAccount(context);
        //获取日历账户的id
        if (calId < 0) {
            //获取账户id失败直接返回，添加日历事件失败
            return null;
        }
//        //添加日历事件
//        Calendar mCalendar = Calendar.getInstance();
//        //设置开始时间
//        mCalendar.setTimeInMillis(reminderTime);
//        long start = mCalendar.getTime().getTime();
        //设置终止时间，开始时间加10分钟
//        mCalendar.setTimeInMillis(start + 6*60 * 60 * 1000);
//        long end = mCalendar.getTime().getTime();
        ContentValues event = new ContentValues();
        event.put("title", title);
        event.put("description", description);
        //插入账户的id
        event.put("calendar_id", calId);
        event.put(CalendarContract.Events.DTSTART, reminderTime);
        event.put(CalendarContract.Events.DTEND, endTime);
        event.put(CalendarContract.Events.ALL_DAY, isAllDay);
        //设置有闹钟提醒
        event.put(CalendarContract.Events.HAS_ALARM, 1);
        //这个是时区，必须有
        event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai");
        //添加事件
        Uri newEvent = context.getContentResolver().insert(Uri.parse(CALENDER_EVENT_URL), event);
        if (newEvent == null) {
            //添加日历事件失败直接返回
            return null;
        }
        //事件提醒的设定
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(newEvent));
        // 提前previousDate分钟有提醒
        values.put(CalendarContract.Reminders.MINUTES, previousDate);
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        Uri uri = context.getContentResolver().insert(Uri.parse(CALENDER_REMINDER_URL), values);

//        if(uri == null) {
//            //添加事件提醒失败直接返回
//            return;
//        }
        return newEvent;
    }

    public static Uri updateCloudScheduleInSysCalendar(Context context, String title, String description,
                                                       long reminderTime, long endTime,
                                                       String scheduleId, boolean isAllDay, int previousDate) {
//        long eventID = Long.parseLong(getCalendarId(context, "", scheduleId));
//        int calId = checkAndAddCalendarAccount(context);
//        ContentValues values = new ContentValues();
//        values.put("title", title);
//        values.put("description", description);
//        //插入账户的id
//        values.put("calendar_id", calId);
//        values.put(CalendarContract.Calendars._ID, eventID);
//        values.put(CalendarContract.Events.DTSTART, reminderTime);
//        values.put(CalendarContract.Events.DTEND, endTime);
//        values.put(CalendarContract.Events.ALL_DAY, isAllDay);
//        //设置有闹钟提醒
//        values.put(CalendarContract.Events.HAS_ALARM, 1);
//        //这个是时区，必须有
//        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai");
//        Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
//        int rows = context.getContentResolver().update(updateUri, values, null, null);

        int calId = checkAndAddCalendarAccount(context);
        ContentValues event = new ContentValues();
        event.put("title", title);
        event.put("description", description);
        //插入账户的id
        event.put("calendar_id", calId);
        event.put(CalendarContract.Events.DTSTART, reminderTime);
        event.put(CalendarContract.Events.DTEND, endTime);
        event.put(CalendarContract.Events.ALL_DAY, isAllDay);
        //设置有闹钟提醒
        event.put(CalendarContract.Events.HAS_ALARM, 1);
        //这个是时区，必须有
        event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai");
        //添加事件
//        Uri newEvent = context.getContentResolver().update(Uri.parse(CALENDER_EVENT_URL), event);
        Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.
                parseLong(getCalendarId(context, "", scheduleId)));
        context.getContentResolver().update(updateUri, event, null, null);

        //事件提醒的设定
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, ContentUris.parseId(updateUri));
        // 提前previousDate分钟有提醒
        values.put(CalendarContract.Reminders.MINUTES, previousDate);
        values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        Uri uri = context.getContentResolver().insert(Uri.parse(CALENDER_REMINDER_URL), values);
        return updateUri;
    }

    /**
     * 通过Uri查询日程事件
     *
     * @param context
     * @param uri
     */
    public static void getCalendarEventByUri(Context context, Uri uri) {
        LogUtils.YfcDebug("uri:" + uri);
        Cursor eventCursor = context.getContentResolver().query(uri,
                null, null, null, null);
        try {
            if (eventCursor != null) {
                for (int i = 0; i < eventCursor.getColumnCount(); i++) {
                    //获取到属性的名称
                    String columnName = eventCursor.getColumnName(i);
                    //获取到属性对应的值
                    String message = eventCursor.getString(eventCursor.getColumnIndex(columnName));
                    LogUtils.YfcDebug(columnName + "----" + message);
                }
            } else {
                LogUtils.YfcDebug("cursor为空");
            }
        } catch (Exception e) {
            LogUtils.YfcDebug("e:" + e.getMessage());
        }
    }

    /**
     * 删除日历事件
     */
    public static void deleteCalendarEvent(Context context, String title) {
        if (context == null) {
            return;
        }
        Cursor eventCursor = context.getContentResolver().query(Uri.parse(CALENDER_EVENT_URL),
                null, null, null, null);
        try {
            if (eventCursor == null) {
                //查询返回空值
                return;
            }
            if (eventCursor.getCount() > 0) {
                //遍历所有事件，找到title跟需要查询的title一样的项
                for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
                    String eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
                    if (!TextUtils.isEmpty(title) && title.equals(eventTitle)) {
                        //取得id
                        int id = eventCursor.getInt(eventCursor.getColumnIndex(CalendarContract.Calendars._ID));
                        Uri deleteUri = ContentUris.withAppendedId(Uri.parse(CALENDER_EVENT_URL), id);
                        int rows = context.getContentResolver().delete(deleteUri, null, null);
                        if (rows == -1) {
                            //事件删除失败
                            return;
                        }
                    }
                }
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }
    }

    /**
     * 获取系统日历所有事件的所有字段
     *
     * @param context
     */
    public static JSONArray getAllCalendarEvent(Context context) {
        //所有的日程
        JSONArray jsonArray = new JSONArray();
        Uri uri = Uri.parse(CALENDER_EVENT_URL);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        while (cursor.moveToNext()) {
            JSONObject jsonObject = new JSONObject();
            int columnCount = cursor.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                //获取到属性的名称
                String columnName = cursor.getColumnName(i);
                //获取到属性对应的值
                String message = cursor.getString(cursor.getColumnIndex(columnName));
                try {
                    jsonObject.put(columnName, message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    /**
     * 通过系统日程Id或者云+日程Id来获取系统日程数据
     *
     * @param context
     * @param calendarId
     * @param cloudPlusId
     * @return
     */
    public static JSONObject getCalendarEventById(Context context, String calendarId, String cloudPlusId) {
        //特定Id的日程
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isBlank(calendarId) && StringUtils.isBlank(cloudPlusId)) {
            return jsonObject;
        }
        calendarId = getCalendarId(context, calendarId, cloudPlusId);
        Uri uri = Uri.parse(CALENDER_EVENT_URL);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        while (cursor.moveToNext()) {
            //取得系统日程id
            int id = cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars._ID));
            if (calendarId.equals(id + "")) {
                int columnCount = cursor.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    //获取到属性的名称
                    String columnName = cursor.getColumnName(i);
                    //获取到属性对应的值
                    String message = cursor.getString(cursor.getColumnIndex(columnName));
                    try {
                        jsonObject.put(columnName, message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return jsonObject;
    }

    /**
     * 获取系统日历Id
     *
     * @param context
     * @param calendarId
     * @param cloudPlusId
     * @return
     */
    private static String getCalendarId(Context context, String calendarId, String cloudPlusId) {
        if (!StringUtils.isBlank(calendarId)) {
            return calendarId;
        }
        return StringUtils.isBlank(cloudPlusId) ? "" : CalendarIdAndCloudScheduleIdCacheUtils.
                getCalendarIdByCloudScheduleId(context, cloudPlusId).getCalendarId();
    }
}
