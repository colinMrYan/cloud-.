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
package com.inspur.emmcloud.widget.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.inspur.emmcloud.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google规范化的属性委托,
 * 代码量多，但是不影响阅读性
 */
final class CalendarViewDelegate {

    /**
     * 周起始：周日
     */
    static final int WEEK_START_WITH_SUN = 1;

    /**
     * 周起始：周一
     */
    static final int WEEK_START_WITH_MON = 2;

    /**
     * 周起始：周六
     */
    static final int WEEK_START_WITH_SAT = 7;
    /**
     * 全部显示
     */
    static final int MODE_ALL_MONTH = 0;
    /**
     * 仅显示当前月份
     */
    static final int MODE_ONLY_CURRENT_MONTH = 1;
    /**
     * 自适应显示，不会多出一行，但是会自动填充
     */
    static final int MODE_FIT_MONTH = 2;
    /**
     * 默认选择模式
     */
    static final int SELECT_MODE_DEFAULT = 0;
    /**
     * 单选模式
     */
    static final int SELECT_MODE_SINGLE = 1;
    /**
     * 范围选择模式
     */
    static final int SELECT_MODE_RANGE = 2;
    /**
     * 多选模式
     */
    static final int SELECT_MODE_MULTI = 3;
    /**
     * 支持转换的最小农历年份
     */
    static final int MIN_YEAR = 1900;
    /**
     * 支持转换的最大农历年份
     */
    private static final int MAX_YEAR = 2099;
    /**
     * 年月视图是否打开
     */
    boolean isShowYearSelectedLayout;
    /**
     * 当前月份和周视图的item位置
     */
    int mCurrentMonthViewItem;
    /**
     * 标记的日期,数量巨大，请使用这个
     */
    Map<String, EmmCalendar> mSchemeDatesMap;
    /**
     * 日期拦截事件
     */
    CalendarView.OnCalendarInterceptListener mCalendarInterceptListener;
    /**
     * 日期选中监听
     */
    CalendarView.OnCalendarSelectListener mCalendarSelectListener;
    /**
     * 范围选择
     */
    CalendarView.OnCalendarRangeSelectListener mCalendarRangeSelectListener;
    /**
     * 多选选择事件
     */
    CalendarView.OnCalendarMultiSelectListener mCalendarMultiSelectListener;
    /**
     * 外部日期长按事件
     */
    CalendarView.OnCalendarLongClickListener mCalendarLongClickListener;
    /**
     * 内部日期切换监听，用于内部更新计算
     */
    CalendarView.OnInnerDateSelectedListener mInnerListener;
    /**
     * 快速年份切换
     */
    CalendarView.OnYearChangeListener mYearChangeListener;
    /**
     * 月份切换事件
     */
    CalendarView.OnMonthChangeListener mMonthChangeListener;
    /**
     * 周视图改变事件
     */
    CalendarView.OnWeekChangeListener mWeekChangeListener;
    /**
     * 视图改变事件
     */
    CalendarView.OnViewChangeListener mViewChangeListener;
    /**
     * 年视图改变事件
     */
    CalendarView.OnYearViewChangeListener mYearViewChangeListener;
    /**
     * 保存选中的日期
     */
    EmmCalendar mSelectedEmmCalendar;
    /**
     * 保存标记位置
     */
    EmmCalendar mIndexEmmCalendar;
    /**
     * 多选日历
     */
    Map<String, EmmCalendar> mSelectedCalendars = new HashMap<>();
    /**
     * 选择范围日历
     */
    EmmCalendar mSelectedStartRangeEmmCalendar, mSelectedEndRangeEmmCalendar;
    /**
     * 周起始
     */
    private int mWeekStart;
    /**
     * 月份显示模式
     */
    private int mMonthViewShowMode;
    /**
     * 选择模式
     */
    private int mSelectMode;
    /**
     * 各种字体颜色，看名字知道对应的地方
     */
    private int mCurDayTextColor,
            mCurDayLunarTextColor,
            mWeekTextColor,
            mSchemeTextColor,
            mSchemeLunarTextColor,
            mOtherMonthTextColor,
            mCurrentMonthTextColor,
            mSelectedTextColor,
            mSelectedLunarTextColor,
            mCurMonthLunarTextColor,
            mOtherMonthLunarTextColor;
    private boolean preventLongPressedSelected;
    /**
     * 日历内部左右padding
     */
    private int mCalendarPadding;
    /**
     * 年视图字体大小
     */
    private int mYearViewMonthTextSize,
            mYearViewDayTextSize,
            mYearViewWeekTextSize;
    /**
     * 年视图月份高度和周的高度
     */
    private int mYearViewMonthHeight,
            mYearViewWeekHeight;
    /**
     * 年视图一些margin和padding
     */
    private int mYearViewPadding,
            mYearViewMonthMarginTop,
            mYearViewMonthMarginBottom;
    /**
     * 年视图字体和标记颜色
     */
    private int mYearViewMonthTextColor,
            mYearViewDayTextColor,
            mYearViewSchemeTextColor,
            mYearViewSelectTextColor,
            mYearViewCurDayTextColor,
            mYearViewWeekTextColor;
    /**
     * 星期栏的背景、线的背景、年份背景
     */
    private int mWeekLineBackground,
            mYearViewBackground,
            mWeekBackground;
    /**
     * 星期栏Line margin
     */
    private int mWeekLineMargin;
    /**
     * 星期栏字体大小
     */
    private int mWeekTextSize;
    /**
     * 标记的主题色和选中的主题色
     */
    private int mSchemeThemeColor, mSelectedThemeColor;
    /**
     * 自定义的日历路径
     */
    private String mMonthViewClassPath;
    /**
     * 月视图类
     */
    private Class<?> mMonthViewClass;
    /**
     * 自定义周视图路径
     */
    private String mWeekViewClassPath;
    /**
     * 周视图类
     */
    private Class<?> mWeekViewClass;
    /**
     * 自定义年视图路径
     */
    private String mYearViewClassPath;
    /**
     * 周视图类
     */
    private Class<?> mYearViewClass;
    /**
     * 自定义周栏路径
     */
    private String mWeekBarClassPath;
    /**
     * 自定义周栏
     */
    private Class<?> mWeekBarClass;
    /**
     * 标记文本
     */
    private String mSchemeText;
    /**
     * 最小年份和最大年份
     */
    private int mMinYear, mMaxYear;
    /**
     * 最小年份和最大年份对应最小月份和最大月份
     * when you want set 2015-07 to 2017-08
     */
    private int mMinYearMonth, mMaxYearMonth;
    /**
     * 最小年份和最大年份对应最小天和最大天数
     * when you want set like 2015-07-08 to 2017-08-30
     */
    private int mMinYearDay, mMaxYearDay;
    /**
     * 日期和农历文本大小
     */
    private int mDayTextSize, mLunarTextSize;
    /**
     * 日历卡的项高度
     */
    private int mCalendarItemHeight;
    /**
     * 星期栏的高度
     */
    private int mWeekBarHeight;
    /**
     * 今天的日子
     */
    private EmmCalendar mCurrentDate;
    private boolean mMonthViewScrollable,
            mWeekViewScrollable,
            mYearViewScrollable;
    private int mMaxMultiSelectSize;
    private int mMinSelectRange, mMaxSelectRange;

    CalendarViewDelegate(Context context, @Nullable AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CalendarView);

        LunarCalendar.init(context);

        mCalendarPadding = (int) array.getDimension(R.styleable.CalendarView_calendar_padding, 0);
        mSchemeTextColor = array.getColor(R.styleable.CalendarView_scheme_text_color, 0xFFFFFFFF);
        mSchemeLunarTextColor = array.getColor(R.styleable.CalendarView_scheme_lunar_text_color, 0xFFe1e1e1);
        mSchemeThemeColor = array.getColor(R.styleable.CalendarView_scheme_theme_color, 0x50CFCFCF);
        mMonthViewClassPath = array.getString(R.styleable.CalendarView_month_view);
        mYearViewClassPath = array.getString(R.styleable.CalendarView_year_view);
        mWeekViewClassPath = array.getString(R.styleable.CalendarView_week_view);
        mWeekBarClassPath = array.getString(R.styleable.CalendarView_week_bar_view);
        mWeekTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_week_text_size,
                CalendarUtil.dipToPx(context, 12));
        mWeekBarHeight = (int) array.getDimension(R.styleable.CalendarView_week_bar_height,
                CalendarUtil.dipToPx(context, 40));
        mWeekLineMargin = (int) array.getDimension(R.styleable.CalendarView_week_line_margin,
                CalendarUtil.dipToPx(context, 0));

        mSchemeText = array.getString(R.styleable.CalendarView_scheme_text);
        if (TextUtils.isEmpty(mSchemeText)) {
            mSchemeText = "";
        }

        mMonthViewScrollable = array.getBoolean(R.styleable.CalendarView_month_view_scrollable, true);
        mWeekViewScrollable = array.getBoolean(R.styleable.CalendarView_week_view_scrollable, true);
        mYearViewScrollable = array.getBoolean(R.styleable.CalendarView_year_view_scrollable, true);


        mMonthViewShowMode = array.getInt(R.styleable.CalendarView_month_view_show_mode, MODE_ALL_MONTH);
        mWeekStart = array.getInt(R.styleable.CalendarView_week_start_with, WEEK_START_WITH_SUN);
        mSelectMode = array.getInt(R.styleable.CalendarView_select_mode, SELECT_MODE_DEFAULT);
        mMaxMultiSelectSize = array.getInt(R.styleable.CalendarView_max_multi_select_size, Integer.MAX_VALUE);
        mMinSelectRange = array.getInt(R.styleable.CalendarView_min_select_range, -1);
        mMaxSelectRange = array.getInt(R.styleable.CalendarView_max_select_range, -1);
        setSelectRange(mMinSelectRange, mMaxSelectRange);

        mWeekBackground = array.getColor(R.styleable.CalendarView_week_background, Color.WHITE);
        mWeekLineBackground = array.getColor(R.styleable.CalendarView_week_line_background, Color.TRANSPARENT);
        mYearViewBackground = array.getColor(R.styleable.CalendarView_year_view_background, Color.WHITE);
        mWeekTextColor = array.getColor(R.styleable.CalendarView_week_text_color, 0xFF333333);

        mCurDayTextColor = array.getColor(R.styleable.CalendarView_current_day_text_color, Color.RED);
        mCurDayLunarTextColor = array.getColor(R.styleable.CalendarView_current_day_lunar_text_color, Color.RED);

        mSelectedThemeColor = array.getColor(R.styleable.CalendarView_selected_theme_color, 0x50CFCFCF);
        mSelectedTextColor = array.getColor(R.styleable.CalendarView_selected_text_color, 0xFF111111);

        mSelectedLunarTextColor = array.getColor(R.styleable.CalendarView_selected_lunar_text_color, 0xFF111111);
        mCurrentMonthTextColor = array.getColor(R.styleable.CalendarView_current_month_text_color, 0xFF111111);
        mOtherMonthTextColor = array.getColor(R.styleable.CalendarView_other_month_text_color, 0xFFe1e1e1);

        mCurMonthLunarTextColor = array.getColor(R.styleable.CalendarView_current_month_lunar_text_color, 0xffe1e1e1);
        mOtherMonthLunarTextColor = array.getColor(R.styleable.CalendarView_other_month_lunar_text_color, 0xffe1e1e1);
        mMinYear = array.getInt(R.styleable.CalendarView_min_year, 1971);
        mMaxYear = array.getInt(R.styleable.CalendarView_max_year, 2055);
        mMinYearMonth = array.getInt(R.styleable.CalendarView_min_year_month, 1);
        mMaxYearMonth = array.getInt(R.styleable.CalendarView_max_year_month, 12);
        mMinYearDay = array.getInt(R.styleable.CalendarView_min_year_day, 1);
        mMaxYearDay = array.getInt(R.styleable.CalendarView_max_year_day, -1);

        mDayTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_day_text_size,
                CalendarUtil.dipToPx(context, 16));
        mLunarTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_lunar_text_size,
                CalendarUtil.dipToPx(context, 10));
        mCalendarItemHeight = (int) array.getDimension(R.styleable.CalendarView_calendar_height,
                CalendarUtil.dipToPx(context, 56));

        //年视图相关
        mYearViewMonthTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_year_view_month_text_size,
                CalendarUtil.dipToPx(context, 18));
        mYearViewDayTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_year_view_day_text_size,
                CalendarUtil.dipToPx(context, 7));
        mYearViewMonthTextColor = array.getColor(R.styleable.CalendarView_year_view_month_text_color, 0xFF111111);
        mYearViewDayTextColor = array.getColor(R.styleable.CalendarView_year_view_day_text_color, 0xFF111111);
        mYearViewSchemeTextColor = array.getColor(R.styleable.CalendarView_year_view_scheme_color, mSchemeThemeColor);
        mYearViewWeekTextColor = array.getColor(R.styleable.CalendarView_year_view_week_text_color, 0xFF333333);
        mYearViewCurDayTextColor = array.getColor(R.styleable.CalendarView_year_view_current_day_text_color, mCurDayTextColor);
        mYearViewSelectTextColor = array.getColor(R.styleable.CalendarView_year_view_select_text_color, 0xFF333333);
        mYearViewWeekTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_year_view_week_text_size,
                CalendarUtil.dipToPx(context, 8));
        mYearViewMonthHeight = array.getDimensionPixelSize(R.styleable.CalendarView_year_view_month_height,
                CalendarUtil.dipToPx(context, 32));
        mYearViewWeekHeight = array.getDimensionPixelSize(R.styleable.CalendarView_year_view_week_height,
                CalendarUtil.dipToPx(context, 0));

        mYearViewPadding = (int) array.getDimension(R.styleable.CalendarView_year_view_padding,
                CalendarUtil.dipToPx(context, 6));
        mYearViewMonthMarginTop = (int) array.getDimension(R.styleable.CalendarView_year_view_month_margin_top,
                CalendarUtil.dipToPx(context, 4));
        mYearViewMonthMarginBottom = (int) array.getDimension(R.styleable.CalendarView_year_view_month_margin_bottom,
                CalendarUtil.dipToPx(context, 4));

        if (mMinYear <= MIN_YEAR) mMinYear = MIN_YEAR;
        if (mMaxYear >= MAX_YEAR) mMaxYear = MAX_YEAR;
        array.recycle();
        init();
    }

    private void init() {
        mCurrentDate = new EmmCalendar();
        Date d = new Date();
        mCurrentDate.setYear(CalendarUtil.getDate("yyyy", d));
        mCurrentDate.setMonth(CalendarUtil.getDate("MM", d));
        mCurrentDate.setDay(CalendarUtil.getDate("dd", d));
        mCurrentDate.setCurrentDay(true);
        LunarCalendar.setupLunarCalendar(mCurrentDate);
        setRange(mMinYear, mMinYearMonth, mMaxYear, mMaxYearMonth);

        try {
            mWeekBarClass = TextUtils.isEmpty(mWeekBarClassPath) ?
                    mWeekBarClass = WeekBar.class : Class.forName(mWeekBarClassPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mYearViewClass = TextUtils.isEmpty(mYearViewClassPath) ?
                    mYearViewClass = DefaultYearView.class : Class.forName(mYearViewClassPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mMonthViewClass = TextUtils.isEmpty(mMonthViewClassPath) ?
                    DefaultMonthView.class : Class.forName(mMonthViewClassPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mWeekViewClass = TextUtils.isEmpty(mWeekViewClassPath) ?
                    DefaultWeekView.class : Class.forName(mWeekViewClassPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setRange(int minYear, int minYearMonth,
                          int maxYear, int maxYearMonth) {
        this.mMinYear = minYear;
        this.mMinYearMonth = minYearMonth;
        this.mMaxYear = maxYear;
        this.mMaxYearMonth = maxYearMonth;
        if (this.mMaxYear < mCurrentDate.getYear()) {
            this.mMaxYear = mCurrentDate.getYear();
        }
        if (this.mMaxYearDay == -1) {
            this.mMaxYearDay = CalendarUtil.getMonthDaysCount(this.mMaxYear, mMaxYearMonth);
        }
        int y = mCurrentDate.getYear() - this.mMinYear;
        mCurrentMonthViewItem = 12 * y + mCurrentDate.getMonth() - this.mMinYearMonth;
    }

    void setRange(int minYear, int minYearMonth, int minYearDay,
                  int maxYear, int maxYearMonth, int maxYearDay) {
        this.mMinYear = minYear;
        this.mMinYearMonth = minYearMonth;
        this.mMinYearDay = minYearDay;
        this.mMaxYear = maxYear;
        this.mMaxYearMonth = maxYearMonth;
        this.mMaxYearDay = maxYearDay;
//        if (this.mMaxYear < mCurrentDate.getYear()) {
//            this.mMaxYear = mCurrentDate.getYear();
//        }
        if (this.mMaxYearDay == -1) {
            this.mMaxYearDay = CalendarUtil.getMonthDaysCount(this.mMaxYear, mMaxYearMonth);
        }
        int y = mCurrentDate.getYear() - this.mMinYear;
        mCurrentMonthViewItem = 12 * y + mCurrentDate.getMonth() - this.mMinYearMonth;
    }

    String getSchemeText() {
        return mSchemeText;
    }

    int getCurDayTextColor() {
        return mCurDayTextColor;
    }

    int getCurDayLunarTextColor() {
        return mCurDayLunarTextColor;
    }

    int getWeekTextColor() {
        return mWeekTextColor;
    }

    int getSchemeTextColor() {
        return mSchemeTextColor;
    }

    int getSchemeLunarTextColor() {
        return mSchemeLunarTextColor;
    }

    int getOtherMonthTextColor() {
        return mOtherMonthTextColor;
    }

    int getCurrentMonthTextColor() {
        return mCurrentMonthTextColor;
    }

    int getSelectedTextColor() {
        return mSelectedTextColor;
    }

    int getSelectedLunarTextColor() {
        return mSelectedLunarTextColor;
    }

    int getCurrentMonthLunarTextColor() {
        return mCurMonthLunarTextColor;
    }

    int getOtherMonthLunarTextColor() {
        return mOtherMonthLunarTextColor;
    }

    int getSchemeThemeColor() {
        return mSchemeThemeColor;
    }

    int getSelectedThemeColor() {
        return mSelectedThemeColor;
    }

    int getWeekBackground() {
        return mWeekBackground;
    }

    int getYearViewBackground() {
        return mYearViewBackground;
    }

    int getWeekLineBackground() {
        return mWeekLineBackground;
    }

    int getWeekLineMargin() {
        return mWeekLineMargin;
    }

    Class<?> getMonthViewClass() {
        return mMonthViewClass;
    }

    void setMonthViewClass(Class<?> monthViewClass) {
        this.mMonthViewClass = monthViewClass;
    }

    Class<?> getWeekViewClass() {
        return mWeekViewClass;
    }

    void setWeekViewClass(Class<?> weekViewClass) {
        this.mWeekViewClass = weekViewClass;
    }

    Class<?> getWeekBarClass() {
        return mWeekBarClass;
    }

    void setWeekBarClass(Class<?> weekBarClass) {
        this.mWeekBarClass = weekBarClass;
    }

    Class<?> getYearViewClass() {
        return mYearViewClass;
    }

    String getYearViewClassPath() {
        return mYearViewClassPath;
    }

    int getWeekBarHeight() {
        return mWeekBarHeight;
    }

    int getMinYear() {
        return mMinYear;
    }

    int getMaxYear() {
        return mMaxYear;
    }

    int getDayTextSize() {
        return mDayTextSize;
    }

    int getLunarTextSize() {
        return mLunarTextSize;
    }

    int getCalendarItemHeight() {
        return mCalendarItemHeight;
    }

    void setCalendarItemHeight(int height) {
        mCalendarItemHeight = height;
    }

    int getMinYearMonth() {
        return mMinYearMonth;
    }

    int getMaxYearMonth() {
        return mMaxYearMonth;
    }

    int getYearViewMonthTextSize() {
        return mYearViewMonthTextSize;
    }

    int getYearViewMonthTextColor() {
        return mYearViewMonthTextColor;
    }

    int getYearViewWeekTextSize() {
        return mYearViewWeekTextSize;
    }

    int getYearViewWeekTextColor() {
        return mYearViewWeekTextColor;
    }

    int getYearViewSelectTextColor() {
        return mYearViewSelectTextColor;
    }

    int getYearViewCurDayTextColor() {
        return mYearViewCurDayTextColor;
    }

    int getYearViewPadding() {
        return mYearViewPadding;
    }

    int getYearViewMonthMarginTop() {
        return mYearViewMonthMarginTop;
    }

    int getYearViewMonthMarginBottom() {
        return mYearViewMonthMarginBottom;
    }

    int getYearViewWeekHeight() {
        return mYearViewWeekHeight;
    }

    int getYearViewMonthHeight() {
        return mYearViewMonthHeight;
    }

    int getYearViewDayTextColor() {
        return mYearViewDayTextColor;
    }

    int getYearViewDayTextSize() {
        return mYearViewDayTextSize;
    }

    int getYearViewSchemeTextColor() {
        return mYearViewSchemeTextColor;
    }

    int getMonthViewShowMode() {
        return mMonthViewShowMode;
    }

    void setMonthViewShowMode(int monthViewShowMode) {
        this.mMonthViewShowMode = monthViewShowMode;
    }

    void setTextColor(int curDayTextColor, int curMonthTextColor, int otherMonthTextColor, int curMonthLunarTextColor, int otherMonthLunarTextColor) {
        mCurDayTextColor = curDayTextColor;
        mOtherMonthTextColor = otherMonthTextColor;
        mCurrentMonthTextColor = curMonthTextColor;
        mCurMonthLunarTextColor = curMonthLunarTextColor;
        mOtherMonthLunarTextColor = otherMonthLunarTextColor;
    }

    void setSchemeColor(int schemeColor, int schemeTextColor, int schemeLunarTextColor) {
        this.mSchemeThemeColor = schemeColor;
        this.mSchemeTextColor = schemeTextColor;
        this.mSchemeLunarTextColor = schemeLunarTextColor;
    }

    void setYearViewTextColor(int yearViewMonthTextColor, int yearViewDayTextColor, int yarViewSchemeTextColor) {
        this.mYearViewMonthTextColor = yearViewMonthTextColor;
        this.mYearViewDayTextColor = yearViewDayTextColor;
        this.mYearViewSchemeTextColor = yarViewSchemeTextColor;
    }

    void setSelectColor(int selectedColor, int selectedTextColor, int selectedLunarTextColor) {
        this.mSelectedThemeColor = selectedColor;
        this.mSelectedTextColor = selectedTextColor;
        this.mSelectedLunarTextColor = selectedLunarTextColor;
    }

    void setThemeColor(int selectedThemeColor, int schemeColor) {
        this.mSelectedThemeColor = selectedThemeColor;
        this.mSchemeThemeColor = schemeColor;
    }

    boolean isMonthViewScrollable() {
        return mMonthViewScrollable;
    }

    void setMonthViewScrollable(boolean monthViewScrollable) {
        this.mMonthViewScrollable = monthViewScrollable;
    }

    boolean isWeekViewScrollable() {
        return mWeekViewScrollable;
    }

    void setWeekViewScrollable(boolean weekViewScrollable) {
        this.mWeekViewScrollable = weekViewScrollable;
    }

    boolean isYearViewScrollable() {
        return mYearViewScrollable;
    }

    void setYearViewScrollable(boolean yearViewScrollable) {
        this.mYearViewScrollable = yearViewScrollable;
    }

    int getWeekStart() {
        return mWeekStart;
    }

    void setWeekStart(int mWeekStart) {
        this.mWeekStart = mWeekStart;
    }

    int getWeekTextSize() {
        return mWeekTextSize;
    }

    /**
     * 选择模式
     *
     * @return 选择模式
     */
    int getSelectMode() {
        return mSelectMode;
    }

    /**
     * 设置选择模式
     *
     * @param mSelectMode mSelectMode
     */
    void setSelectMode(int mSelectMode) {
        this.mSelectMode = mSelectMode;
    }

    int getMinSelectRange() {
        return mMinSelectRange;
    }

    int getMaxSelectRange() {
        return mMaxSelectRange;
    }

    int getMaxMultiSelectSize() {
        return mMaxMultiSelectSize;
    }

    void setMaxMultiSelectSize(int maxMultiSelectSize) {
        this.mMaxMultiSelectSize = maxMultiSelectSize;
    }

    final void setSelectRange(int minRange, int maxRange) {
        if (minRange > maxRange && maxRange > 0) {
            mMaxSelectRange = minRange;
            mMinSelectRange = minRange;
            return;
        }
        if (minRange <= 0) {
            mMinSelectRange = -1;
        } else {
            mMinSelectRange = minRange;
        }
        if (maxRange <= 0) {
            mMaxSelectRange = -1;
        } else {
            mMaxSelectRange = maxRange;
        }
    }

    EmmCalendar getCurrentDay() {
        return mCurrentDate;
    }

    void updateCurrentDay() {
        Date d = new Date();
        mCurrentDate.setYear(CalendarUtil.getDate("yyyy", d));
        mCurrentDate.setMonth(CalendarUtil.getDate("MM", d));
        mCurrentDate.setDay(CalendarUtil.getDate("dd", d));
        LunarCalendar.setupLunarCalendar(mCurrentDate);
    }

    int getCalendarPadding() {
        return mCalendarPadding;
    }

    boolean isPreventLongPressedSelected() {
        return preventLongPressedSelected;
    }

    void setPreventLongPressedSelected(boolean preventLongPressedSelected) {
        this.preventLongPressedSelected = preventLongPressedSelected;
    }

    void clearSelectedScheme() {
        mSelectedEmmCalendar.clearScheme();
    }

    int getMinYearDay() {
        return mMinYearDay;
    }

    int getMaxYearDay() {
        return mMaxYearDay;
    }

    final void updateSelectCalendarScheme() {
        if (mSchemeDatesMap != null && mSchemeDatesMap.size() > 0) {
            String key = mSelectedEmmCalendar.toString();
            if (mSchemeDatesMap.containsKey(key)) {
                EmmCalendar d = mSchemeDatesMap.get(key);
                mSelectedEmmCalendar.mergeScheme(d, getSchemeText());
            }
        } else {
            clearSelectedScheme();
        }
    }

    EmmCalendar createCurrentDate() {
        EmmCalendar emmCalendar = new EmmCalendar();
        emmCalendar.setYear(mCurrentDate.getYear());
        emmCalendar.setWeek(mCurrentDate.getWeek());
        emmCalendar.setMonth(mCurrentDate.getMonth());
        emmCalendar.setDay(mCurrentDate.getDay());
        emmCalendar.setCurrentDay(true);
        LunarCalendar.setupLunarCalendar(emmCalendar);
        return emmCalendar;
    }

    final EmmCalendar getMinRangeCalendar() {
        EmmCalendar emmCalendar = new EmmCalendar();
        emmCalendar.setYear(mMinYear);
        emmCalendar.setMonth(mMinYearMonth);
        emmCalendar.setDay(mMinYearDay);
        emmCalendar.setCurrentDay(emmCalendar.equals(mCurrentDate));
        LunarCalendar.setupLunarCalendar(emmCalendar);
        return emmCalendar;
    }

    @SuppressWarnings("unused")
    final EmmCalendar getMaxRangeCalendar() {
        EmmCalendar emmCalendar = new EmmCalendar();
        emmCalendar.setYear(mMaxYear);
        emmCalendar.setMonth(mMaxYearMonth);
        emmCalendar.setDay(mMaxYearDay);
        emmCalendar.setCurrentDay(emmCalendar.equals(mCurrentDate));
        LunarCalendar.setupLunarCalendar(emmCalendar);
        return emmCalendar;
    }

    /**
     * 添加事件标记，来自Map
     */
    final void addSchemesFromMap(List<EmmCalendar> mItems) {
        if (mSchemeDatesMap == null || mSchemeDatesMap.size() == 0) {
            return;
        }
        for (EmmCalendar a : mItems) {
            if (mSchemeDatesMap.containsKey(a.toString())) {
                EmmCalendar d = mSchemeDatesMap.get(a.toString());
                a.setScheme(TextUtils.isEmpty(d.getScheme()) ? getSchemeText() : d.getScheme());
                a.setSchemeColor(d.getSchemeColor());
                a.setSchemes(d.getSchemes());
            } else {
                a.setScheme("");
                a.setSchemeColor(0);
                a.setSchemes(null);
            }
        }
    }

    /**
     * 清楚选择
     */
    final void clearSelectRange() {
        mSelectedStartRangeEmmCalendar = null;
        mSelectedEndRangeEmmCalendar = null;
    }

    /**
     * 获得选中范围
     *
     * @return 选中范围
     */
    final List<EmmCalendar> getSelectCalendarRange() {
        if (mSelectMode != SELECT_MODE_RANGE) {
            return null;
        }
        List<EmmCalendar> emmCalendars = new ArrayList<>();
        if (mSelectedStartRangeEmmCalendar == null ||
                mSelectedEndRangeEmmCalendar == null) {
            return emmCalendars;
        }
        final long ONE_DAY = 1000 * 3600 * 24;
        java.util.Calendar date = java.util.Calendar.getInstance();

        date.set(mSelectedStartRangeEmmCalendar.getYear(),
                mSelectedStartRangeEmmCalendar.getMonth() - 1,
                mSelectedStartRangeEmmCalendar.getDay());//

        long startTimeMills = date.getTimeInMillis();//获得起始时间戳


        date.set(mSelectedEndRangeEmmCalendar.getYear(),
                mSelectedEndRangeEmmCalendar.getMonth() - 1,
                mSelectedEndRangeEmmCalendar.getDay());//
        long endTimeMills = date.getTimeInMillis();
        for (long start = startTimeMills; start <= endTimeMills; start += ONE_DAY) {
            date.setTimeInMillis(start);
            EmmCalendar emmCalendar = new EmmCalendar();
            emmCalendar.setYear(date.get(java.util.Calendar.YEAR));
            emmCalendar.setMonth(date.get(java.util.Calendar.MONTH) + 1);
            emmCalendar.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));
            if (mCalendarInterceptListener != null &&
                    mCalendarInterceptListener.onCalendarIntercept(emmCalendar)) {
                continue;
            }
            LunarCalendar.setupLunarCalendar(emmCalendar);
            emmCalendars.add(emmCalendar);
        }
        addSchemesFromMap(emmCalendars);
        return emmCalendars;
    }
}
