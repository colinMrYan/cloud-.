package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.widget.Chronometer;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.schedule.calendar.CalendarEvent;
import com.inspur.emmcloud.util.common.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * TimeUtils
 * <p>
 * 本地 local 0时区UTC 方法区分local和UTC 没标注按local long型时间毫秒 timeLong String型时间
 * timeString
 */
public class TimeUtils {

    // public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new
    // SimpleDateFormat(
    // "yyyy-MM-dd HH:mm:ss");
    // public static final SimpleDateFormat DATE_FORMAT_DATE = new
    // SimpleDateFormat(
    // "yyyy-MM-dd");
    //
    public static final SimpleDateFormat DATE_FORMAT_HOUR_MINUTE = new SimpleDateFormat(
            "HH:mm");
//    public static final SimpleDateFormat utcFormat = new SimpleDateFormat(
//            "yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");

    public static final int FORMAT_DEFAULT_DATE = 1;
    public static final int FORMAT_YEAR_MONTH_DAY = 2;
    public static final int FORMAT_HOUR_MINUTE = 3;
    public static final int FORMAT_UTC = 4;
    public static final int FORMAT_MONTH_DAY = 5;
    public static final int FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE = 6;
    public static final int FORMAT_YEAR_MONTH = 7;
    public static final int FORMAT_YEAR_MONTH_DAY_BY_DASH = 8;
    public static final int FORMAT_MONTH_DAY_HOUR_MINUTE = 9;

    private static final int WEEK_MONDAY = 2;
    private static final int WEEK_TUESDAY = 3;
    private static final int WEEK_WENDNESDAY = 4;
    private static final int WEEK_THURSDAY = 5;
    private static final int WEEK_FRIDAY = 6;
    private static final int WEEK_SATURDAY = 7;
    private static final int WEEK_SUNDAY = 1;

    private TimeUtils() {
        throw new AssertionError();
    }

    public static SimpleDateFormat getFormat(Context context, int type) {
        String pattern = "";
        switch (type) {
            case FORMAT_DEFAULT_DATE:
                pattern = context.getString(R.string.format_default);
                break;
            case FORMAT_YEAR_MONTH_DAY:
                pattern = context.getString(R.string.format_year_month_day);
                break;
            case FORMAT_HOUR_MINUTE:
                pattern = context.getString(R.string.format_hour_minute);
                break;
            case FORMAT_UTC:
                pattern = context.getString(R.string.format_utc);
                break;
            case FORMAT_MONTH_DAY:
                pattern = context.getString(R.string.format_month_day);
                break;
            case FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE:
                pattern = context
                        .getString(R.string.format_year_month_day_hour_minute);
                break;
            case FORMAT_YEAR_MONTH:
                pattern = context.getString(R.string.format_year_month);
                break;
            case FORMAT_YEAR_MONTH_DAY_BY_DASH:
                pattern = context.getString(R.string.format_year_month_day_by_dash);
                break;
            case FORMAT_MONTH_DAY_HOUR_MINUTE:
                pattern = context
                        .getString(R.string.format_month_day_hour_minute);
                break;
            default:
                break;
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format;
    }

    /**
     * 零时区时间转为本地时间
     *
     * @param UTCCalendar
     * @return
     */
    public static Calendar UTCCalendar2LocalCalendar(Calendar UTCCalendar) {
        if (UTCCalendar == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(UTCCalendar.getTimeInMillis());
        calendar.setTimeZone(TimeZone.getDefault());
        return calendar;

    }

    /**
     * 本地时间转为零时区时间
     *
     * @param localCalendar
     * @return
     */
    public static Calendar localCalendar2UTCCalendar(Calendar localCalendar) {
        if (localCalendar == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(localCalendar.getTimeInMillis());
        TimeZone timezone = TimeZone.getTimeZone("Etc/GMT+0");
        calendar.setTimeZone(timezone);
        return calendar;
    }

    /**
     * UTC毫秒值转为本地Calendar
     *
     * @param time
     * @return
     */
    public static Calendar timeLong2Calendar(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar;
    }

    /**
     * UTC毫秒值转为本地Calendar
     *
     * @param time
     * @return
     */
    public static Calendar timeLong2UTCCalendar(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Etc/GMT+0"));
        calendar.setTimeInMillis(time);
        return calendar;
    }

    /**
     * 时间字符串转Calendar
     *
     * @param timeLong
     * @return
     */
    public static Calendar timeString2Calendar(String timeLong) {
        if (StringUtils.isBlank(timeLong)) {
            return null;
        }
        long time = Long.parseLong(timeLong);
        Calendar calendar = timeLong2Calendar(time);
        return calendar;
    }

    /**
     * 时间字符串转为Calendar
     *
     * @param time
     * @param fomat
     * @return
     */
    public static Calendar timeString2Calendar(String time,
                                               SimpleDateFormat fomat) {
        Calendar calendar = null;
        try {
            Date date = fomat.parse(time);
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(date.getTime());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return calendar;
    }

    /**
     * 时间字符串转为Calendar
     *
     * @param context
     * @param time
     * @param type
     * @return
     */
    public static Calendar timeString2Calendar(Context context, String time,
                                               int type) {
        SimpleDateFormat format = getFormat(context, type);
        return timeString2Calendar(time, format);
    }

    /**
     * 时间字符串转为Date
     *
     * @param time
     * @param fomat
     * @return
     */
    public static Date timeString2Date(String time, SimpleDateFormat fomat) {
        Date date = null;
        try {
            date = fomat.parse(time);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 时间字符串转为Date
     *
     * @param context
     * @param time
     * @param type
     * @return
     */
    public static Date timeString2Date(Context context, String time, int type) {
        SimpleDateFormat format = getFormat(context, type);
        return timeString2Date(time, format);
    }

    public static String parseDateFromtime(Long longtime, Context context) {
        SimpleDateFormat format = new SimpleDateFormat(
                context.getString(R.string.format_default));
        String d = "";
        d = format.format(longtime);
        // TimeZone timeZone = TimeZone.getDefault();
        // format.setTimeZone(timeZone);
        // Date date = null;
        // int offset = timeZone.getRawOffset();
        // try {
        // date = new Date(format.parse(d).getTime());
        // date = new Date(date.getTime() + offset);
        // } catch (ParseException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        return d;

    }

    /**
     * 获取国际化的周几
     *
     * @param context
     * @param calendar
     * @return
     */
    public static String getWeekDay(Context context, Calendar calendar) {
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        String weekDate = "";
        switch (weekDay) {
            case WEEK_MONDAY:
                weekDate = context.getString(R.string.monday);
                break;
            case WEEK_TUESDAY:
                weekDate = context.getString(R.string.tuesday);
                break;
            case WEEK_WENDNESDAY:
                weekDate = context.getString(R.string.wednesday);
                break;
            case WEEK_THURSDAY:
                weekDate = context.getString(R.string.thursday);
                break;
            case WEEK_FRIDAY:
                weekDate = context.getString(R.string.friday);
                break;
            case WEEK_SATURDAY:
                weekDate = context.getString(R.string.saturday);
                break;
            case WEEK_SUNDAY:
                weekDate = context.getString(R.string.sunday);
                break;
            default:
                weekDate = "";
                break;
        }
        return weekDate;
    }

    /**
     * Calendar转为毫秒值
     *
     * @param calendar
     * @return
     */
    public static long calendar2TimeLong(Calendar calendar) {
        return calendar.getTimeInMillis();
    }

    /**
     * Calendar转为时间字符串
     *
     * @param calendar
     * @param fomat
     * @return
     */
    public static String Calendar2TimeString(Calendar calendar,
                                             SimpleDateFormat fomat) {
        return fomat.format(calendar.getTime());
    }

    /**
     * UTC日期转为UTC字符串
     *
     * @param UTCCalendar
     * @return
     */
    public static String UTCCalendar2UTCTimeString(Calendar UTCCalendar) {
        SimpleDateFormat utcFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
        TimeZone timezone = TimeZone.getTimeZone("Etc/GMT+0");
        utcFormat.setTimeZone(timezone);
        return Calendar2TimeString(UTCCalendar, utcFormat);
    }

    /**
     * long time to string
     *
     * @param timeInMillis
     * @param dateFormat
     * @return
     */
    public static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date(timeInMillis));
    }

    public static String getTime(Context context, long timeInMillis, int type) {
        SimpleDateFormat format = getFormat(context, type);
        return getTime(timeInMillis, format);
    }

    public static String getTime(String timeInMills) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd");
        return dateFormat.format(new Date(Long.parseLong(timeInMills)));
    }

    /**
     * 获取传入是周几
     *
     * @param pTime
     * @return
     */
    public static int dayForWeek(String pTime) {
        if (StringUtils.isBlank(pTime)) {
            return -1;
        }
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date(Long.parseLong(pTime)));
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 得到两个日期之间的差值
     *
     * @param timeLong1
     * @param timeLong2
     * @return
     * @throws ParseException
     */
    public static int daysBetween(String timeLong1, String timeLong2)
            throws ParseException {
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTimeInMillis(Long.parseLong(timeLong1));
        calendar2.setTimeInMillis(Long.parseLong(timeLong2));
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);
        calendar2.set(Calendar.HOUR_OF_DAY, 0);
        calendar2.set(Calendar.MINUTE, 0);
        calendar2.set(Calendar.SECOND, 0);
        calendar2.set(Calendar.MILLISECOND, 0);
        long between_days = (calendar2.getTimeInMillis() - calendar1
                .getTimeInMillis()) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 得到当前日期和指定日期的差值
     *
     * @param longtime
     * @return
     * @throws ParseException
     */
    public static int daysBetweenToday(String longtime) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        return daysBetween(calendar.getTimeInMillis() + "", longtime);
    }

    /**
     * 得到今天0点时间戳
     *
     * @return
     */
    public static Long getStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTimeInMillis();
    }

    /**
     * 得到今天12点时间戳
     *
     * @return
     */
    public static Long getMiddleTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 12);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTime().getTime();
    }

    /**
     * 得到今天24点时间戳
     *
     * @return
     */
    public static Long getEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime().getTime();
    }

    /**
     * 获取系统时间今天年月日，形式如20171116，八位
     *
     * @return
     */
    public static String getFormatYearMonthDay() {
        Calendar today = Calendar.getInstance();
        String month = (today.get(Calendar.MONTH) < 10) ? ("0" + (today.get(Calendar.MONTH) + 1)) : ("" + (today.get(Calendar.MONTH) + 1));
        String day = (today.get(Calendar.DAY_OF_MONTH) < 10) ? ("0" + today.get(Calendar.DAY_OF_MONTH)) : ("" + today.get(Calendar.DAY_OF_MONTH));
        return today.get(Calendar.YEAR) + month + day;
    }

    /**
     * long time to string, format is {@link #FORMAT_DEFAULT_DATE}
     *
     * @param timeInMillis
     * @return
     */
    public static String getTime(Context context, long timeInMillis) {
        SimpleDateFormat format = getFormat(context, FORMAT_DEFAULT_DATE);
        return getTime(timeInMillis, format);
    }

    /**
     * get current time format
     *
     * @param context
     * @param type
     * @return
     */
    public static String getCurrentTimeInString(Context context, int type) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = getFormat(context, type);
        return getTime(calendar.getTimeInMillis(), format);
    }

    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static long getCurrentTimeInLong() {
        return System.currentTimeMillis();
    }


    public static String getCurrentTimeInString(Context context) {
        return getTime(context, getCurrentTimeInLong());
    }

    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static String getCurrentTimeInString(SimpleDateFormat dateFormat) {
        return getTime(getCurrentTimeInLong(), dateFormat);
    }

    /**
     * UTC字符串转为毫秒值
     *
     * @param UTCTime
     * @return
     */
    public static long UTCString2Long(String UTCTime) {
        SimpleDateFormat utcFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
        long timeLong = 0L;
        try {
            TimeZone timezone = TimeZone.getTimeZone("Etc/GMT+0");
            utcFormat.setTimeZone(timezone);
            timeLong = utcFormat.parse(UTCTime).getTime();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return timeLong;
    }

    /**
     * 获取当前的UTC时间
     *
     * @return
     */
    public static String getCurrentUTCTimeString() {
        Calendar localCalendar = Calendar.getInstance();
        Calendar UTCCalendar = localCalendar2UTCCalendar(localCalendar);
        return UTCCalendar2UTCTimeString(UTCCalendar);
    }

    /**
     * 获得频道消息的显示时间
     *
     * @param UTCTime
     * @return
     */
    public static String UTCString2YMDString(Context context, String UTCTime) {
        String time = "";
        try {
            Long timeLong = UTCString2Long(UTCTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeLong);
            time = calendar2FormatString(context, calendar,
                    FORMAT_YEAR_MONTH_DAY);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return time;
    }

    public static String timeLong2YMDString(Context context, long timeLong) {
        String time = "";
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeLong);
            time = calendar2FormatString(context, calendar,
                    FORMAT_YEAR_MONTH_DAY);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return time;
    }


    public static String timeLong2YMString(Context context, long timeLong) {
        String time = "";
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeLong);
            time = calendar2FormatString(context, calendar,
                    FORMAT_YEAR_MONTH);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return time;
    }


    /**
     * calendar转为特定格式字符串
     *
     * @param context
     * @param calendar
     * @param type
     * @return
     */
    public static String calendar2FormatString(Context context,
                                               Calendar calendar, int type) {
        if (calendar == null) {
            return "";
        }
        return calendar2FormatString(context, calendar.getTime(), type);
    }

    /**
     * date转为特定格式字符串
     *
     * @param context
     * @param date
     * @param type
     * @return
     */
    public static String calendar2FormatString(Context context, Date date,
                                               int type) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat format = getFormat(context, type);
        return format.format(date);
    }

    /**
     * calendar转为特定格式字符串
     *
     * @param context
     * @param calendar
     * @param format
     * @return
     */
    public static String calendar2FormatString(Context context,
                                               Calendar calendar, SimpleDateFormat format) {
        if (calendar == null) {
            return "";
        }
        return calendar2FormatString(context, calendar.getTime(), format);
    }

    /**
     * calendar转为特定格式字符串
     *
     * @param context
     * @param date
     * @param format
     * @return
     */
    public static String calendar2FormatString(Context context, Date date,
                                               SimpleDateFormat format) {
        if (date == null) {
            return "";
        }
        return format.format(date);
    }

    /**
     * 判断日期是否是今天
     *
     * @param calendar
     * @return
     */
    public static boolean isCalendarToday(Calendar calendar) {
        if (calendar != null) {
            Calendar targetCalendar = Calendar.getInstance();
            Calendar todayCalendar = Calendar.getInstance();
            targetCalendar.setTimeInMillis(calendar.getTimeInMillis());
            if (todayCalendar.get(Calendar.YEAR) != targetCalendar
                    .get(Calendar.YEAR)) {
                return false;
            }
            if (todayCalendar.get(Calendar.MONTH) != targetCalendar
                    .get(Calendar.MONTH)) {
                return false;
            }
            return todayCalendar.get(Calendar.DAY_OF_MONTH) == targetCalendar
                    .get(Calendar.DAY_OF_MONTH);

        }

        return false;

    }

    /**
     * 获取工作相关倒计时text
     *
     * @param context
     * @param calendarString
     * @return
     */
    public static String getCountdown(Context context, String calendarString) {
        Calendar calendar = timeString2Calendar(calendarString);
        return getCountdown(context, calendar);
    }

    /**
     * 获取工作相关倒计时text
     *
     * @param context
     * @param calendar
     * @return
     */
    public static String getCountdown(Context context, Calendar calendar) {
        if (calendar == null) {
            return "";
        }
        String countdown = "";
        Calendar targetCalendar = Calendar.getInstance();
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);
        targetCalendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        targetCalendar.set(Calendar.MILLISECOND, 0);
        long currentSec = currentCalendar.getTimeInMillis();
        long targetSec = targetCalendar.getTimeInMillis();
        int dayCount = (int) ((targetSec - currentSec) / 1000 / 60 / 60 / 24);
        if (dayCount == -1) {
            countdown = context.getString(R.string.cutdown_yestoday);
        } else if (dayCount == 0) {
            countdown = context.getString(R.string.cutdown_now);
        } else if (dayCount == 1) {
            countdown = context.getString(R.string.cutdown_tomorrow);
        } else {
            SimpleDateFormat format = new SimpleDateFormat(
                    context.getString(R.string.format_task_month_day));
            countdown = calendar2FormatString(context,
                    targetCalendar.getTime(), format);
            if (countdown.startsWith("0")) {
                countdown = countdown.substring(1, countdown.length());
            }
        }
        return countdown;
    }

    /**
     * 获取倒计时天数
     *
     * @param calendarString
     * @return
     */
    public static int getCountdownNum(String calendarString) {
        Calendar calendar = timeString2Calendar(calendarString);
        return getCountdownNum(calendar);
    }

    /**
     * 获取倒计时天数
     *
     * @param calendar
     * @return
     */
    public static int getCountdownNum(Calendar calendar) {
        if (calendar == null) {
            return 0;
        }
        int dayCount = 0;
        Calendar targetCalendar = Calendar.getInstance();
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);
        targetCalendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        targetCalendar.set(Calendar.MILLISECOND, 0);
        long currentSec = currentCalendar.getTimeInMillis();
        long targetSec = targetCalendar.getTimeInMillis();
        dayCount = (int) ((targetSec - currentSec) / 86400000);
        return dayCount;
    }

    /**
     * 获得当天calendar event的时间段
     *
     * @param calendarEvent
     * @return
     */
    public static String getCalEventTimeSelection(Context context,
                                                  CalendarEvent calendarEvent) {
        String timeSelection = "";
        Calendar startDate = calendarEvent.getStartDate();
        Calendar endDate = calendarEvent.getEndDate();
        boolean isAllday = calendarEvent.isAllday();
        if (isAllday) {
            timeSelection = context.getString(R.string.all_day);
        } else if (startDate == null || endDate == null) {
            timeSelection = context.getString(R.string.time_null);
        } else {
            String todayStartTime = Calendar2TimeString(startDate,
                    DATE_FORMAT_HOUR_MINUTE);
            String todayEndTime = Calendar2TimeString(endDate,
                    DATE_FORMAT_HOUR_MINUTE);
            timeSelection = todayStartTime + " - " + todayEndTime;
        }
        return timeSelection;
    }

    public static String getFormatStringFromTargetTime(Context context, Calendar targetCalendar, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(targetCalendar.getTimeInMillis());
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)
                + day);
        SimpleDateFormat format = getFormat(context, FORMAT_MONTH_DAY);
        return getTime(calendar.getTimeInMillis(), format);

    }

    /**
     * 获取从距离今天第几天的那天的开始时间
     *
     * @param day 距离今天的天数
     * @return
     */
    public static Long getTimeLongFromTargetTime(Calendar targetCalendar, int day, String dayStartTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(targetCalendar.getTimeInMillis());
        Calendar startCalendar = timeString2Calendar(dayStartTime,
                DATE_FORMAT_HOUR_MINUTE);
        int startHour = startCalendar.get(Calendar.HOUR_OF_DAY);
        int startMinute = startCalendar.get(Calendar.MINUTE);

        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)
                + day);
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        calendar.set(Calendar.MINUTE, startMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }


    /**
     * 获取显示时间
     *
     * @param UTCStringTime
     * @return
     */
    public static String getDisplayTime(Context context, String UTCStringTime) {
        long timeLong = UTCString2Long(UTCStringTime);
        Calendar displayCalendar = Calendar.getInstance();
        displayCalendar.setTimeInMillis(timeLong);
        String time = getDisplayTime(context, displayCalendar);
        return time;

    }

    /**
     * 获取显示时间
     *
     * @param context
     * @param timeLong
     * @return
     */
    public static String getDisplayTime(Context context, long timeLong) {
        Calendar displayCalendar = Calendar.getInstance();
        displayCalendar.setTimeInMillis(timeLong);
        return getDisplayTime(context, displayCalendar);

    }

    /**
     * 获取显示时间
     *
     * @param UTCStringTime
     * @return
     */
    public static String getChannelMsgDisplayTime(Context context, String UTCStringTime) {
        long timeLong = UTCString2Long(UTCStringTime);
        return getChannelMsgDisplayTime(context, timeLong);

    }

    public static String getChannelMsgDisplayTime(Context context, long timeLong) {
        Calendar displayCalendar = Calendar.getInstance();
        displayCalendar.setTimeInMillis(timeLong);
        String displayTime = "";
        Calendar todayBeginCalendar = Calendar.getInstance();
        todayBeginCalendar.set(todayBeginCalendar.get(Calendar.YEAR),
                todayBeginCalendar.get(Calendar.MONTH),
                todayBeginCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        todayBeginCalendar.set(Calendar.MILLISECOND, 0);

        Calendar yesterdayCalendar = Calendar.getInstance();
        yesterdayCalendar.setTimeInMillis(todayBeginCalendar.getTimeInMillis());
        yesterdayCalendar.add(Calendar.DAY_OF_MONTH, -1);

        Calendar yearBeginCalendar = Calendar.getInstance();
        yearBeginCalendar.set(todayBeginCalendar.get(Calendar.YEAR), 0, 0, 0,
                0, 0);
        yearBeginCalendar.set(Calendar.MILLISECOND, 0);
        displayTime = calendar2FormatString(context, displayCalendar,
                FORMAT_HOUR_MINUTE);
        if (displayCalendar.after(todayBeginCalendar)) {

        } else if (displayCalendar.after(yesterdayCalendar)) {
            displayTime = context.getString(R.string.yesterday) + " " + displayTime;
        } else if (displayCalendar.after(yearBeginCalendar)) {
            displayTime = calendar2FormatString(context, displayCalendar,
                    FORMAT_MONTH_DAY) + " " + displayTime;
        } else {
            displayTime = calendar2FormatString(context, displayCalendar,
                    FORMAT_YEAR_MONTH_DAY) + " " + displayTime;
        }

        return displayTime;
    }


    /**
     * 获取显示时间
     *
     * @param context
     * @param displayCalendar
     * @return
     */
    public static String getDisplayTime(Context context,
                                        Calendar displayCalendar) {
        String displayTime = "";
        Calendar todayBeginCalendar = Calendar.getInstance();
        todayBeginCalendar.set(todayBeginCalendar.get(Calendar.YEAR),
                todayBeginCalendar.get(Calendar.MONTH),
                todayBeginCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        todayBeginCalendar.set(Calendar.MILLISECOND, 0);

        Calendar yesterdayCalendar = Calendar.getInstance();
        yesterdayCalendar.setTimeInMillis(todayBeginCalendar.getTimeInMillis());
        yesterdayCalendar.add(Calendar.DAY_OF_MONTH, -1);

        Calendar yearBeginCalendar = Calendar.getInstance();
        yearBeginCalendar.set(todayBeginCalendar.get(Calendar.YEAR), 0, 0, 0,
                0, 0);
        yearBeginCalendar.set(Calendar.MILLISECOND, 0);

        if (displayCalendar.after(todayBeginCalendar)) {
            displayTime = calendar2FormatString(context, displayCalendar,
                    FORMAT_HOUR_MINUTE);
        } else if (displayCalendar.after(yesterdayCalendar)) {
            displayTime = context.getString(R.string.yesterday);
        } else if (displayCalendar.after(yearBeginCalendar)) {
            displayTime = calendar2FormatString(context, displayCalendar,
                    FORMAT_MONTH_DAY);
        } else {
            displayTime = calendar2FormatString(context, displayCalendar,
                    FORMAT_YEAR_MONTH_DAY);
        }

        return displayTime;

    }

    /**
     * 获取下一个半点时间
     *
     * @return
     */
    public static Calendar getNextHalfHourTime(Calendar calendar) {
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Long timeLong = calendar.getTimeInMillis();
        Calendar nextHalfHourCalendar = Calendar.getInstance();
        if ((timeLong % 1800000) != 0) {
            long times = (int) (timeLong / 1800000);
            times++;
            long nextHalfHourTimeLong = times * 1800000;
            nextHalfHourCalendar.setTimeInMillis(nextHalfHourTimeLong);
        } else {
            nextHalfHourCalendar.setTimeInMillis(timeLong);
        }
        return nextHalfHourCalendar;
    }

    /**
     * 获得协调时对应的Date对象
     *
     * @param UTCTime
     * @return
     */
    public static Date UTCString2LocalDate(String UTCTime) {
        SimpleDateFormat utcFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
        SimpleDateFormat format = null;
        if (UTCTime.contains(".")) {
            String[] UTCTimeArray = UTCTime.split("\\.");
            if (UTCTimeArray[1].length() == 4) {
                format = utcFormat;
            } else {
                format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SS'Z'");
            }

        } else {
            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        }

        Date date = null;
        try {
            TimeZone timeZone = TimeZone.getDefault();
            int offset = timeZone.getRawOffset();
            date = new Date(format.parse(UTCTime).getTime());
            date = new Date(date.getTime() + offset);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return date;
    }


    /**
     * 带有时区的时间路径,目前是零时区GMT
     * 如果需要改成东八区则GMT+8
     *
     * @param postTime
     * @return
     */
    public static String getNewsTimePathIn(String postTime) {
        SimpleDateFormat sdfGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
        postTime = sdfGMT.format(Long.parseLong(postTime));
        String timeYear = postTime.substring(0, 4);
        String timeMon = postTime.substring(5, 7);
        String timeDay = postTime.substring(8, 10);
        int year = Integer.parseInt(timeYear);
        int mon = Integer.parseInt(timeMon);
        int day = Integer.parseInt(timeDay);
        String timePath = APIUri.getGroupNewsArticleUrl() + year + "/" + mon
                + "/" + day + "/";
        return timePath;
    }

    /**
     * 获取发现搜索新闻中的发布时间显示
     *
     * @param utcTime
     * @return
     */
    public static String getFindSearchNewsDisplayTime(String utcTime) {
        String displayTime = "";
        try {
            SimpleDateFormat fromFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd");
            TimeZone timezone = TimeZone.getTimeZone("Etc/GMT+0");
            fromFormat.setTimeZone(timezone);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fromFormat.parse(utcTime));
            displayTime = Calendar2TimeString(calendar, toFormat);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return displayTime;
    }

    /**
     * 判断两个Calendar是否同一天
     *
     * @param calendarA
     * @param calendarB
     * @return
     */
    public static boolean isSameDay(Calendar calendarA, Calendar calendarB) {
        if (calendarA == null || calendarB == null) {
            return false;
        }
        return calendarA.get(Calendar.YEAR) == calendarB.get(Calendar.YEAR)
                && calendarA.get(Calendar.MONTH) == calendarB.get(Calendar.MONTH)
                && calendarA.get(Calendar.DAY_OF_MONTH) == calendarB
                .get(Calendar.DAY_OF_MONTH);
    }


    /**
     * 上取整时间
     *
     * @param calendar
     * @param calendarOther
     * @return
     */
    public static int getCeil(Calendar calendar, Calendar calendarOther) {
        return (int) Math.ceil((calendar.getTimeInMillis() - calendarOther.getTimeInMillis()) / (60 * 60) / 1000.0);
    }

    /**
     * @param cmt Chronometer控件
     * @return 小时+分钟+秒数  的所有秒数
     */
    public static String getChronometerSeconds(Chronometer cmt) {
        int totalss = 0;
        String string = cmt.getText().toString();
        if (string.length() == 7) {
            String[] split = string.split(":");
            String string2 = split[0];
            int hour = Integer.parseInt(string2);
            int Hours = hour * 3600;
            String string3 = split[1];
            int min = Integer.parseInt(string3);
            int Mins = min * 60;
            int SS = Integer.parseInt(split[2]);
            totalss = Hours + Mins + SS;
            return String.valueOf(totalss);
        }
        if (string.length() == 5) {
            String[] split = string.split(":");
            String string3 = split[0];
            int min = Integer.parseInt(string3);
            int Mins = min * 60;
            int SS = Integer.parseInt(split[1]);
            totalss = Mins + SS;
            return String.valueOf(totalss);
        }
        return String.valueOf(totalss);
    }

    /**
     * 开始日期和结束日期是否包含特定日期
     *
     * @param targetCalendar
     * @param startCalendar
     * @param endCalendar
     * @return
     */
    public static boolean isContainTargetCalendarDay(Calendar targetCalendar, Calendar startCalendar, Calendar endCalendar) {
        Calendar dayBeginCalendar = (Calendar) targetCalendar.clone();
        dayBeginCalendar = getDayBeginCalendar(dayBeginCalendar);
        Calendar dayEndCalendar = (Calendar) endCalendar.clone();
        dayEndCalendar = getDayEndCalendar(dayEndCalendar);
        return (!dayBeginCalendar.after(startCalendar) && dayEndCalendar.after(endCalendar)) || (dayBeginCalendar.before(endCalendar) && dayBeginCalendar.after(startCalendar)) || (dayEndCalendar.before(endCalendar) && dayEndCalendar.after(startCalendar));
    }

    /**
     * 时间戳转成提示性日期格式（昨天、今天……)
     */
    public static String getDateToString(long milSecond, String pattern) {
        Date date = new Date(milSecond);
        SimpleDateFormat format;
        String hintDate = "";
        //先获取年份
        int year = Integer.valueOf(new SimpleDateFormat("yyyy").format(date));
        //获取一年中的第几天
        int day = Integer.valueOf(new SimpleDateFormat("d").format(date));
        //获取当前年份 和 一年中的第几天
        Date currentDate = new Date(System.currentTimeMillis());
        int currentYear = Integer.valueOf(new SimpleDateFormat("yyyy").format(currentDate));
        int currentDay = Integer.valueOf(new SimpleDateFormat("d").format(currentDate));
        //计算 如果是去年的
        if (currentYear - year == 1) {
            //如果当前正好是 1月1日 计算去年有多少天，指定时间是否是一年中的最后一天
            if (currentDay == 1) {
                int yearDay;
                if (year % 400 == 0) {
                    yearDay = 366;//世纪闰年
                } else if (year % 4 == 0 && year % 100 != 0) {
                    yearDay = 366;//普通闰年
                } else {
                    yearDay = 365;//平年
                }
                if (day == yearDay) {
                    hintDate = "昨天";
                }
            }
        } else {
            if (currentDay - day == 1) {
                hintDate = "昨天";
            }
            if (currentDay - day == 0) {
                hintDate = "今天";
            }
        }
        if (StringUtils.isEmpty(hintDate)) {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return format.format(date);
        } else {
            format = new SimpleDateFormat("HH:mm");
            return hintDate + " " + format.format(date);
        }

    }


    public static Calendar getDayBeginCalendar(Calendar calendar) {
        Calendar dayBeginCalendar = (Calendar) calendar.clone();
        dayBeginCalendar.set(Calendar.HOUR_OF_DAY, 0);
        dayBeginCalendar.set(Calendar.MINUTE, 0);
        dayBeginCalendar.set(Calendar.SECOND, 0);
        dayBeginCalendar.set(Calendar.MILLISECOND, 0);
        return dayBeginCalendar;
    }

    public static Calendar getDayEndCalendar(Calendar calendar) {
        Calendar dayBeginCalendar = getDayBeginCalendar(calendar);
        dayBeginCalendar.add(Calendar.DAY_OF_YEAR, 1);
        dayBeginCalendar.add(Calendar.MILLISECOND, -1);
        return (Calendar) dayBeginCalendar.clone();
    }
}
