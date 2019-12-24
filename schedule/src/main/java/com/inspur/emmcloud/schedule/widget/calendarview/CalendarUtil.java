/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/MiracleTimes-Dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inspur.emmcloud.schedule.widget.calendarview;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 一些日期辅助计算工具
 */
final class CalendarUtil {

    private static final long ONE_DAY = 1000 * 3600 * 24;

    @SuppressLint("SimpleDateFormat")
    static int getDate(String formatStr, Date date) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        return Integer.parseInt(format.format(date));
    }

    /**
     * 判断一个日期是否是周末，即周六日
     *
     * @param emmCalendar calendar
     * @return 判断一个日期是否是周末，即周六日
     */
    static boolean isWeekend(EmmCalendar emmCalendar) {
        int week = getWeekFormCalendar(emmCalendar);
        return week == 0 || week == 6;
    }

    /**
     * 获取某月的天数
     *
     * @param year  年
     * @param month 月
     * @return 某月的天数
     */
    static int getMonthDaysCount(int year, int month) {
        int count = 0;
        //判断大月份
        if (month == 1 || month == 3 || month == 5 || month == 7
                || month == 8 || month == 10 || month == 12) {
            count = 31;
        }

        //判断小月
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            count = 30;
        }

        //判断平年与闰年
        if (month == 2) {
            if (isLeapYear(year)) {
                count = 29;
            } else {
                count = 28;
            }
        }
        return count;
    }


    /**
     * 是否是闰年
     *
     * @param year year
     * @return 是否是闰年
     */
    static boolean isLeapYear(int year) {
        return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
    }


    /**
     * 获取月视图的确切高度
     * Test pass
     *
     * @param year       年
     * @param month      月
     * @param itemHeight 每项的高度
     * @return 不需要多余行的高度
     */
    static int getMonthViewHeight(int year, int month, int itemHeight, int weekStartWith) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(year, month - 1, 1);
        int preDiff = getMonthViewStartDiff(year, month, weekStartWith);
        int monthDaysCount = getMonthDaysCount(year, month);
        int nextDiff = getMonthEndDiff(year, month, monthDaysCount, weekStartWith);
        return (preDiff + monthDaysCount + nextDiff) / 7 * itemHeight;
    }


    /**
     * 获取某天在该月的第几周,换言之就是获取这一天在该月视图的第几行,第几周，根据周起始动态获取
     * Test pass，单元测试通过
     *
     * @param emmCalendar calendar
     * @param weekStart   其实星期是哪一天？
     * @return 获取某天在该月的第几周 the week line in MonthView
     */
    static int getWeekFromDayInMonth(EmmCalendar emmCalendar, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(emmCalendar.getYear(), emmCalendar.getMonth() - 1, 1);
        //该月第一天为星期几,星期天 == 0
        int diff = getMonthViewStartDiff(emmCalendar, weekStart);
        return (emmCalendar.getDay() + diff - 1) / 7 + 1;
    }

    /**
     * 获取上一个日子
     *
     * @param emmCalendar calendar
     * @return 获取上一个日子
     */
    static EmmCalendar getPreCalendar(EmmCalendar emmCalendar) {
        java.util.Calendar date = java.util.Calendar.getInstance();

        date.set(emmCalendar.getYear(), emmCalendar.getMonth() - 1, emmCalendar.getDay());//

        long timeMills = date.getTimeInMillis();//获得起始时间戳

        date.setTimeInMillis(timeMills - ONE_DAY);

        EmmCalendar preEmmCalendar = new EmmCalendar();
        preEmmCalendar.setYear(date.get(java.util.Calendar.YEAR));
        preEmmCalendar.setMonth(date.get(java.util.Calendar.MONTH) + 1);
        preEmmCalendar.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));

        return preEmmCalendar;
    }

    static EmmCalendar getNextCalendar(EmmCalendar emmCalendar) {
        java.util.Calendar date = java.util.Calendar.getInstance();

        date.set(emmCalendar.getYear(), emmCalendar.getMonth() - 1, emmCalendar.getDay());//

        long timeMills = date.getTimeInMillis();//获得起始时间戳

        date.setTimeInMillis(timeMills + ONE_DAY);

        EmmCalendar nextEmmCalendar = new EmmCalendar();
        nextEmmCalendar.setYear(date.get(java.util.Calendar.YEAR));
        nextEmmCalendar.setMonth(date.get(java.util.Calendar.MONTH) + 1);
        nextEmmCalendar.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));

        return nextEmmCalendar;
    }

    /**
     * DAY_OF_WEEK return  1  2  3 	4  5  6	 7，偏移了一位
     * 获取日期所在月视图对应的起始偏移量
     * Test pass
     *
     * @param emmCalendar calendar
     * @param weekStart   weekStart 星期的起始
     * @return 获取日期所在月视图对应的起始偏移量 the start diff with MonthView
     */
    static int getMonthViewStartDiff(EmmCalendar emmCalendar, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(emmCalendar.getYear(), emmCalendar.getMonth() - 1, 1);
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return week - 1;
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return week == 1 ? 6 : week - weekStart;
        }
        return week == CalendarViewDelegate.WEEK_START_WITH_SAT ? 0 : week;
    }


    /**
     * DAY_OF_WEEK return  1  2  3 	4  5  6	 7，偏移了一位
     * 获取日期所在月份的结束偏移量，用于计算两个年份之间总共有多少周，不用于MonthView
     * Test pass
     *
     * @param emmCalendar calendar
     * @param weekStart   weekStart 星期的起始
     * @return 获取日期所在月份的结束偏移量 the end diff in Month not MonthView
     */
    @SuppressWarnings("unused")
    static int getMonthEndDiff(EmmCalendar emmCalendar, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(emmCalendar.getYear(), emmCalendar.getMonth() - 1, getMonthDaysCount(emmCalendar.getYear(), emmCalendar.getMonth()));
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return 7 - week;
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return week == 1 ? 0 : 7 - week + 1;
        }
        return week == CalendarViewDelegate.WEEK_START_WITH_SAT ? 6 : 7 - week - 1;
    }

    /**
     * DAY_OF_WEEK return  1  2  3 	4  5  6	 7，偏移了一位
     * 获取日期所在月视图对应的起始偏移量
     * Test pass
     *
     * @param year      年
     * @param month     月
     * @param weekStart 周起始
     * @return 获取日期所在月视图对应的起始偏移量 the start diff with MonthView
     */
    static int getMonthViewStartDiff(int year, int month, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(year, month - 1, 1);
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return week - 1;
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return week == 1 ? 6 : week - weekStart;
        }
        return week == CalendarViewDelegate.WEEK_START_WITH_SAT ? 0 : week;
    }


    /**
     * DAY_OF_WEEK return  1  2  3 	4  5  6	 7，偏移了一位
     * 获取日期月份对应的结束偏移量,用于计算两个年份之间总共有多少周，不用于MonthView
     * Test pass
     *
     * @param year      年
     * @param month     月
     * @param weekStart 周起始
     * @return 获取日期月份对应的结束偏移量 the end diff in Month not MonthView
     */
    static int getMonthEndDiff(int year, int month, int weekStart) {
        return getMonthEndDiff(year, month, getMonthDaysCount(year, month), weekStart);
    }


    /**
     * DAY_OF_WEEK return  1  2  3 	4  5  6	 7，偏移了一位
     * 获取日期月份对应的结束偏移量,用于计算两个年份之间总共有多少周，不用于MonthView
     * Test pass
     *
     * @param year      年
     * @param month     月
     * @param weekStart 周起始
     * @return 获取日期月份对应的结束偏移量 the end diff in Month not MonthView
     */
    private static int getMonthEndDiff(int year, int month, int day, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(year, month - 1, day);
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return 7 - week;
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return week == 1 ? 0 : 7 - week + 1;
        }
        return week == 7 ? 6 : 7 - week - 1;
    }

    /**
     * 获取某个日期是星期几
     * 测试通过
     *
     * @param emmCalendar 某个日期
     * @return 返回某个日期是星期几
     */
    static int getWeekFormCalendar(EmmCalendar emmCalendar) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(emmCalendar.getYear(), emmCalendar.getMonth() - 1, emmCalendar.getDay());
        return date.get(java.util.Calendar.DAY_OF_WEEK) - 1;
    }


    /**
     * 获取周视图的切换默认选项位置 WeekView index
     * 测试通过 test pass
     *
     * @param emmCalendar calendar
     * @param weekStart   weekStart
     * @return 获取周视图的切换默认选项位置
     */
    static int getWeekViewIndexFromCalendar(EmmCalendar emmCalendar, int weekStart) {
        return getWeekViewStartDiff(emmCalendar.getYear(), emmCalendar.getMonth(), emmCalendar.getDay(), weekStart);
    }

    /**
     * 是否在日期范围內
     * 测试通过 test pass
     *
     * @param emmCalendar  calendar
     * @param minYear      minYear
     * @param minYearDay   最小年份天
     * @param minYearMonth minYearMonth
     * @param maxYear      maxYear
     * @param maxYearMonth maxYearMonth
     * @param maxYearDay   最大年份天
     * @return 是否在日期范围內
     */
    static boolean isCalendarInRange(EmmCalendar emmCalendar,
                                     int minYear, int minYearMonth, int minYearDay,
                                     int maxYear, int maxYearMonth, int maxYearDay) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(minYear, minYearMonth - 1, minYearDay);
        long minTime = c.getTimeInMillis();
        c.set(maxYear, maxYearMonth - 1, maxYearDay);
        long maxTime = c.getTimeInMillis();
        c.set(emmCalendar.getYear(), emmCalendar.getMonth() - 1, emmCalendar.getDay());
        long curTime = c.getTimeInMillis();
        return curTime >= minTime && curTime <= maxTime;
    }

    /**
     * 获取两个日期之间一共有多少周，
     * 注意周起始周一、周日、周六
     * 测试通过 test pass
     *
     * @param minYear      minYear 最小年份
     * @param minYearMonth maxYear 最小年份月份
     * @param minYearDay   最小年份天
     * @param maxYear      maxYear 最大年份
     * @param maxYearMonth maxYear 最大年份月份
     * @param maxYearDay   最大年份天
     * @param weekStart    周起始
     * @return 周数用于WeekViewPager itemCount
     */
    static int getWeekCountBetweenBothCalendar(int minYear, int minYearMonth, int minYearDay,
                                               int maxYear, int maxYearMonth, int maxYearDay,
                                               int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(minYear, minYearMonth - 1, minYearDay);
        long minTimeMills = date.getTimeInMillis();//给定时间戳
        int preDiff = getWeekViewStartDiff(minYear, minYearMonth, minYearDay, weekStart);

        date.set(maxYear, maxYearMonth - 1, maxYearDay);

        long maxTimeMills = date.getTimeInMillis();//给定时间戳

        int nextDiff = getWeekViewEndDiff(maxYear, maxYearMonth, maxYearDay, weekStart);

        int count = preDiff + nextDiff;

        int c = (int) ((maxTimeMills - minTimeMills) / ONE_DAY) + 1;
        count += c;
        return count / 7;
    }


    /**
     * 根据日期获取距离最小日期在第几周
     * 用来设置 WeekView currentItem
     * 测试通过 test pass
     *
     * @param emmCalendar  calendar
     * @param minYear      minYear 最小年份
     * @param minYearMonth maxYear 最小年份月份
     * @param minYearDay   最小年份天
     * @param weekStart    周起始
     * @return 返回两个年份中第几周 the WeekView currentItem
     */
    static int getWeekFromCalendarStartWithMinCalendar(EmmCalendar emmCalendar,
                                                       int minYear, int minYearMonth, int minYearDay,
                                                       int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(minYear, minYearMonth - 1, minYearDay);//起始日期
        long firstTimeMill = date.getTimeInMillis();//获得范围起始时间戳

        int preDiff = getWeekViewStartDiff(minYear, minYearMonth, minYearDay, weekStart);//范围起始的周偏移量

        int weekStartDiff = getWeekViewStartDiff(emmCalendar.getYear(),
                emmCalendar.getMonth(),
                emmCalendar.getDay(),
                weekStart);//获取点击的日子在周视图的起始，为了兼容全球时区，最大日差为一天，如果周起始偏差weekStartDiff=0，则日期加1

        date.set(emmCalendar.getYear(),
                emmCalendar.getMonth() - 1,
                weekStartDiff == 0 ? emmCalendar.getDay() + 1 : emmCalendar.getDay());

        long curTimeMills = date.getTimeInMillis();//给定时间戳

        int c = (int) ((curTimeMills - firstTimeMill) / ONE_DAY);

        int count = preDiff + c;

        return count / 7 + 1;
    }

    /**
     * 根据星期数和最小日期推算出该星期的第一天
     * //测试通过 Test pass
     *
     * @param minYear      最小年份如2017
     * @param minYearMonth maxYear 最小年份月份，like : 2017-07
     * @param minYearDay   最小年份天
     * @param week         从最小年份minYear月minYearMonth 日1 开始的第几周 week > 0
     * @return 该星期的第一天日期
     */
    static EmmCalendar getFirstCalendarStartWithMinCalendar(int minYear, int minYearMonth, int minYearDay, int week, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();

        date.set(minYear, minYearMonth - 1, minYearDay);//

        long firstTimeMills = date.getTimeInMillis();//获得起始时间戳


        long weekTimeMills = (week - 1) * 7 * ONE_DAY;

        long timeCountMills = weekTimeMills + firstTimeMills;

        date.setTimeInMillis(timeCountMills);

        int startDiff = getWeekViewStartDiff(date.get(java.util.Calendar.YEAR),
                date.get(java.util.Calendar.MONTH) + 1,
                date.get(java.util.Calendar.DAY_OF_MONTH), weekStart);

        timeCountMills -= startDiff * ONE_DAY;
        date.setTimeInMillis(timeCountMills);

        EmmCalendar emmCalendar = new EmmCalendar();
        emmCalendar.setYear(date.get(java.util.Calendar.YEAR));
        emmCalendar.setMonth(date.get(java.util.Calendar.MONTH) + 1);
        emmCalendar.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));

        return emmCalendar;
    }


    /**
     * 是否在日期范围内
     *
     * @param emmCalendar calendar
     * @param delegate    delegate
     * @return 是否在日期范围内
     */
    static boolean isCalendarInRange(EmmCalendar emmCalendar, CalendarViewDelegate delegate) {
        return isCalendarInRange(emmCalendar,
                delegate.getMinYear(), delegate.getMinYearMonth(), delegate.getMinYearDay(),
                delegate.getMaxYear(), delegate.getMaxYearMonth(), delegate.getMaxYearDay());
    }

    /**
     * 是否在日期范围內
     *
     * @param year         year
     * @param month        month
     * @param minYear      minYear
     * @param minYearMonth minYearMonth
     * @param maxYear      maxYear
     * @param maxYearMonth maxYearMonth
     * @return 是否在日期范围內
     */
    static boolean isMonthInRange(int year, int month, int minYear, int minYearMonth, int maxYear, int maxYearMonth) {
        return !(year < minYear || year > maxYear) &&
                !(year == minYear && month < minYearMonth) &&
                !(year == maxYear && month > maxYearMonth);
    }

    /**
     * 运算 calendar1 - calendar2
     * test Pass
     *
     * @param emmCalendar1 calendar1
     * @param emmCalendar2 calendar2
     * @return calendar1 - calendar2
     */
    static int differ(EmmCalendar emmCalendar1, EmmCalendar emmCalendar2) {
        if (emmCalendar1 == null) {
            return Integer.MIN_VALUE;
        }
        if (emmCalendar2 == null) {
            return Integer.MAX_VALUE;
        }
        java.util.Calendar date = java.util.Calendar.getInstance();

        date.set(emmCalendar1.getYear(), emmCalendar1.getMonth() - 1, emmCalendar1.getDay());//

        long startTimeMills = date.getTimeInMillis();//获得起始时间戳

        date.set(emmCalendar2.getYear(), emmCalendar2.getMonth() - 1, emmCalendar2.getDay());//

        long endTimeMills = date.getTimeInMillis();//获得结束时间戳

        return (int) ((startTimeMills - endTimeMills) / ONE_DAY);
    }

    /**
     * 比较日期大小
     *
     * @param minYear      minYear
     * @param minYearMonth minYearMonth
     * @param minYearDay   minYearDay
     * @param maxYear      maxYear
     * @param maxYearMonth maxYearMonth
     * @param maxYearDay   maxYearDay
     * @return -1 0 1
     */
    static int compareTo(int minYear, int minYearMonth, int minYearDay,
                         int maxYear, int maxYearMonth, int maxYearDay) {
        EmmCalendar first = new EmmCalendar();
        first.setYear(minYear);
        first.setMonth(minYearMonth);
        first.setDay(minYearDay);

        EmmCalendar second = new EmmCalendar();
        second.setYear(maxYear);
        second.setMonth(maxYearMonth);
        second.setDay(maxYearDay);
        return first.compareTo(second);
    }

    /**
     * 为月视图初始化日历
     *
     * @param year        year
     * @param month       month
     * @param currentDate currentDate
     * @param weekStar    weekStar
     * @return 为月视图初始化日历项
     */
    static List<EmmCalendar> initCalendarForMonthView(int year, int month, EmmCalendar currentDate, int weekStar) {
        java.util.Calendar date = java.util.Calendar.getInstance();

        date.set(year, month - 1, 1);

        int mPreDiff = getMonthViewStartDiff(year, month, weekStar);//获取月视图其实偏移量

        int monthDayCount = getMonthDaysCount(year, month);//获取月份真实天数

        int preYear, preMonth;
        int nextYear, nextMonth;

        int size = 42;

        List<EmmCalendar> mItems = new ArrayList<>();

        int preMonthDaysCount;
        if (month == 1) {//如果是1月
            preYear = year - 1;
            preMonth = 12;
            nextYear = year;
            nextMonth = month + 1;
            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
        } else if (month == 12) {//如果是12月
            preYear = year;
            preMonth = month - 1;
            nextYear = year + 1;
            nextMonth = 1;
            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
        } else {//平常
            preYear = year;
            preMonth = month - 1;
            nextYear = year;
            nextMonth = month + 1;
            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
        }
        int nextDay = 1;
        for (int i = 0; i < size; i++) {
            EmmCalendar emmCalendarDate = new EmmCalendar();
            if (i < mPreDiff) {
                emmCalendarDate.setYear(preYear);
                emmCalendarDate.setMonth(preMonth);
                emmCalendarDate.setDay(preMonthDaysCount - mPreDiff + i + 1);
            } else if (i >= monthDayCount + mPreDiff) {
                emmCalendarDate.setYear(nextYear);
                emmCalendarDate.setMonth(nextMonth);
                emmCalendarDate.setDay(nextDay);
                ++nextDay;
            } else {
                emmCalendarDate.setYear(year);
                emmCalendarDate.setMonth(month);
                emmCalendarDate.setCurrentMonth(true);
                emmCalendarDate.setDay(i - mPreDiff + 1);
            }
            if (emmCalendarDate.equals(currentDate)) {
                emmCalendarDate.setCurrentDay(true);
            }
            LunarCalendar.setupLunarCalendar(emmCalendarDate);
            mItems.add(emmCalendarDate);
        }
        return mItems;
    }

    static List<EmmCalendar> getWeekCalendars(EmmCalendar emmCalendar, CalendarViewDelegate mDelegate) {
        long curTime = emmCalendar.getTimeInMillis();

        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(emmCalendar.getYear(),
                emmCalendar.getMonth() - 1,
                emmCalendar.getDay());//
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        int startDiff;
        if (mDelegate.getWeekStart() == 1) {
            startDiff = week - 1;
        } else if (mDelegate.getWeekStart() == 2) {
            startDiff = week == 1 ? 6 : week - mDelegate.getWeekStart();
        } else {
            startDiff = week == 7 ? 0 : week;
        }

        curTime -= startDiff * ONE_DAY;
        java.util.Calendar minCalendar = java.util.Calendar.getInstance();
        minCalendar.setTimeInMillis(curTime);
        EmmCalendar startEmmCalendar = new EmmCalendar();
        startEmmCalendar.setYear(minCalendar.get(java.util.Calendar.YEAR));
        startEmmCalendar.setMonth(minCalendar.get(java.util.Calendar.MONTH) + 1);
        startEmmCalendar.setDay(minCalendar.get(java.util.Calendar.DAY_OF_MONTH));
        return initCalendarForWeekView(startEmmCalendar, mDelegate, mDelegate.getWeekStart());
    }

    /**
     * 生成周视图的7个item
     *
     * @param emmCalendar calendar
     * @param mDelegate   mDelegate
     * @param weekStart   weekStart
     * @return 生成周视图的7个item
     */
    static List<EmmCalendar> initCalendarForWeekView(EmmCalendar emmCalendar, CalendarViewDelegate mDelegate, int weekStart) {

        java.util.Calendar date = java.util.Calendar.getInstance();//当天时间
        date.set(emmCalendar.getYear(), emmCalendar.getMonth() - 1, emmCalendar.getDay());
        long curDateMills = date.getTimeInMillis();//生成选择的日期时间戳

        int weekEndDiff = getWeekViewEndDiff(emmCalendar.getYear(), emmCalendar.getMonth(), emmCalendar.getDay(), weekStart);
        List<EmmCalendar> mItems = new ArrayList<>();

        date.setTimeInMillis(curDateMills);
        EmmCalendar selectEmmCalendar = new EmmCalendar();
        selectEmmCalendar.setYear(date.get(java.util.Calendar.YEAR));
        selectEmmCalendar.setMonth(date.get(java.util.Calendar.MONTH) + 1);
        selectEmmCalendar.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));
        if (selectEmmCalendar.equals(mDelegate.getCurrentDay())) {
            selectEmmCalendar.setCurrentDay(true);
        }
        LunarCalendar.setupLunarCalendar(selectEmmCalendar);
        selectEmmCalendar.setCurrentMonth(true);
        mItems.add(selectEmmCalendar);


        for (int i = 1; i <= weekEndDiff; i++) {
            date.setTimeInMillis(curDateMills + i * ONE_DAY);
            EmmCalendar emmCalendarDate = new EmmCalendar();
            emmCalendarDate.setYear(date.get(java.util.Calendar.YEAR));
            emmCalendarDate.setMonth(date.get(java.util.Calendar.MONTH) + 1);
            emmCalendarDate.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));
            if (emmCalendarDate.equals(mDelegate.getCurrentDay())) {
                emmCalendarDate.setCurrentDay(true);
            }
            LunarCalendar.setupLunarCalendar(emmCalendarDate);
            emmCalendarDate.setCurrentMonth(true);
            mItems.add(emmCalendarDate);
        }
        return mItems;
    }

    /**
     * 单元测试通过
     * 从选定的日期，获取周视图起始偏移量，用来生成周视图布局
     *
     * @param year      year
     * @param month     month
     * @param day       day
     * @param weekStart 周起始，1，2，7 日 一 六
     * @return 获取周视图起始偏移量，用来生成周视图布局
     */
    private static int getWeekViewStartDiff(int year, int month, int day, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(year, month - 1, day);//
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        if (weekStart == 1) {
            return week - 1;
        }
        if (weekStart == 2) {
            return week == 1 ? 6 : week - weekStart;
        }
        return week == 7 ? 0 : week;
    }


    /**
     * 单元测试通过
     * 从选定的日期，获取周视图结束偏移量，用来生成周视图布局
     *
     * @param year      year
     * @param month     month
     * @param day       day
     * @param weekStart 周起始，1，2，7 日 一 六
     * @return 获取周视图结束偏移量，用来生成周视图布局
     */
    private static int getWeekViewEndDiff(int year, int month, int day, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(year, month - 1, day);
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        if (weekStart == 1) {
            return 7 - week;
        }
        if (weekStart == 2) {
            return week == 1 ? 0 : 7 - week + 1;
        }
        return week == 7 ? 6 : 7 - week - 1;
    }


    /**
     * 从月视图切换获得第一天的日期
     *
     * @param position position
     * @param delegate position
     * @return 从月视图切换获得第一天的日期
     */
    static EmmCalendar getFirstCalendarFromMonthViewPager(int position, CalendarViewDelegate delegate) {
        EmmCalendar emmCalendar = new EmmCalendar();
        emmCalendar.setYear((position + delegate.getMinYearMonth() - 1) / 12 + delegate.getMinYear());
        emmCalendar.setMonth((position + delegate.getMinYearMonth() - 1) % 12 + 1);
        emmCalendar.setDay(1);
        if (!isCalendarInRange(emmCalendar, delegate)) {
            if (isMinRangeEdge(emmCalendar, delegate)) {
                emmCalendar = delegate.getMinRangeCalendar();
            } else {
                emmCalendar = delegate.getMaxRangeCalendar();
            }
        }
        emmCalendar.setCurrentMonth(emmCalendar.getYear() == delegate.getCurrentDay().getYear() &&
                emmCalendar.getMonth() == delegate.getCurrentDay().getMonth());
        emmCalendar.setCurrentDay(emmCalendar.equals(delegate.getCurrentDay()));
        LunarCalendar.setupLunarCalendar(emmCalendar);
        return emmCalendar;
    }


    /**
     * 根据传入的日期获取边界访问日期，要么最大，要么最小
     *
     * @param emmCalendar calendar
     * @param delegate    delegate
     * @return 获取边界访问日期
     */
    static EmmCalendar getRangeEdgeCalendar(EmmCalendar emmCalendar, CalendarViewDelegate delegate) {
        if (CalendarUtil.isCalendarInRange(delegate.getCurrentDay(), delegate)) {
            return delegate.createCurrentDate();
        }
        if (isCalendarInRange(emmCalendar, delegate)) {
            return emmCalendar;
        }
        EmmCalendar minRangeEmmCalendar = delegate.getMinRangeCalendar();
        if (minRangeEmmCalendar.isSameMonth(emmCalendar)) {
            return delegate.getMinRangeCalendar();
        }
        return delegate.getMaxRangeCalendar();
    }

    /**
     * 是否是最小访问边界了
     *
     * @param emmCalendar calendar
     * @return 是否是最小访问边界了
     */
    private static boolean isMinRangeEdge(EmmCalendar emmCalendar, CalendarViewDelegate delegate) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(delegate.getMinYear(), delegate.getMinYearMonth() - 1, delegate.getMinYearDay());
        long minTime = c.getTimeInMillis();
        c.set(emmCalendar.getYear(), emmCalendar.getMonth() - 1, emmCalendar.getDay());
        long curTime = c.getTimeInMillis();
        return curTime < minTime;
    }

    /**
     * dp转px
     *
     * @param context context
     * @param dpValue dp
     * @return px
     */
    static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
