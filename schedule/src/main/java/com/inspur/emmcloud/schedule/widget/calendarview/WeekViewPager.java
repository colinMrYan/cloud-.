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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * 周视图滑动ViewPager，需要动态固定高度
 * 周视图是连续不断的视图，因此不能简单的得出每年都有52+1周，这样会计算重叠的部分
 * WeekViewPager需要和CalendarView关联:
 */

public final class WeekViewPager extends ViewPager {
    /**
     * 日历布局，需要在日历下方放自己的布局
     */
    CalendarLayout mParentLayout;
    private boolean isUpdateWeekView;
    private int mWeekCount;
    private CalendarViewDelegate mDelegate;
    /**
     * 是否使用滚动到某一天
     */
    private boolean isUsingScrollToCalendar = false;

    public WeekViewPager(Context context) {
        this(context, null);
    }

    public WeekViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setup(CalendarViewDelegate delegate) {
        this.mDelegate = delegate;
        init();
    }

    private void init() {
        mWeekCount = CalendarUtil.getWeekCountBetweenBothCalendar(
                mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                mDelegate.getMaxYear(),
                mDelegate.getMaxYearMonth(),
                mDelegate.getMaxYearDay(),
                mDelegate.getWeekStart());
        setAdapter(new WeekViewPagerAdapter());
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //默认的显示星期四，周视图切换就显示星期4
                if (getVisibility() != VISIBLE) {
                    isUsingScrollToCalendar = false;
                    return;
                }
                if (isUsingScrollToCalendar) {
                    isUsingScrollToCalendar = false;
                    return;
                }
                BaseWeekView view = (BaseWeekView) findViewWithTag(position);
                if (view != null) {
                    view.performClickCalendar(mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_DEFAULT ?
                            mDelegate.mIndexEmmCalendar : mDelegate.mSelectedEmmCalendar, !isUsingScrollToCalendar);
                    if (mDelegate.mWeekChangeListener != null) {
                        mDelegate.mWeekChangeListener.onWeekChange(getCurrentWeekCalendars());
                    }
                }
                isUsingScrollToCalendar = false;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 获取当前周数据
     *
     * @return 获取当前周数据
     */
    List<EmmCalendar> getCurrentWeekCalendars() {
        List<EmmCalendar> emmCalendars = CalendarUtil.getWeekCalendars(mDelegate.mSelectedEmmCalendar,  // mDelegate.mIndexEmmCalendar =>mDelegate.mSelectedEmmCalendar
                mDelegate);                                                                             // 解决跨月选择日期时，数据显示异常问题
        mDelegate.addSchemesFromMap(emmCalendars);
        return emmCalendars;
    }


    /**
     * 更新周视图
     */
    void notifyDataSetChanged() {
        mWeekCount = CalendarUtil.getWeekCountBetweenBothCalendar(
                mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                mDelegate.getMaxYear(),
                mDelegate.getMaxYearMonth(),
                mDelegate.getMaxYearDay(),
                mDelegate.getWeekStart());
        getAdapter().notifyDataSetChanged();
    }

    /**
     * 更新周视图布局
     */
    void updateWeekViewClass() {
        isUpdateWeekView = true;
        getAdapter().notifyDataSetChanged();
        isUpdateWeekView = false;
    }

    /**
     * 更新日期范围
     */
    void updateRange() {
        isUpdateWeekView = true;
        notifyDataSetChanged();
        isUpdateWeekView = false;
        if (getVisibility() != VISIBLE) {
            return;
        }
        isUsingScrollToCalendar = true;
        EmmCalendar emmCalendar = mDelegate.mSelectedEmmCalendar;
        updateSelected(emmCalendar, false);
        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onWeekDateSelected(emmCalendar, false);
        }

        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(emmCalendar, false);
        }

        int i = CalendarUtil.getWeekFromDayInMonth(emmCalendar, mDelegate.getWeekStart());
        mParentLayout.updateSelectWeek(i);
    }

    /**
     * 滚动到指定日期
     *
     * @param year  年
     * @param month 月
     * @param day   日
     */
    void scrollToCalendar(int year, int month, int day, boolean smoothScroll) {
        isUsingScrollToCalendar = true;
        EmmCalendar emmCalendar = new EmmCalendar();
        emmCalendar.setYear(year);
        emmCalendar.setMonth(month);
        emmCalendar.setDay(day);
        emmCalendar.setCurrentDay(emmCalendar.equals(mDelegate.getCurrentDay()));
        LunarCalendar.setupLunarCalendar(emmCalendar);
        mDelegate.mIndexEmmCalendar = emmCalendar;
        mDelegate.mSelectedEmmCalendar = emmCalendar;
        mDelegate.updateSelectCalendarScheme();
        updateSelected(emmCalendar, smoothScroll);
        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onWeekDateSelected(emmCalendar, false);
        }
        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(emmCalendar, false);
        }
        int i = CalendarUtil.getWeekFromDayInMonth(emmCalendar, mDelegate.getWeekStart());
        mParentLayout.updateSelectWeek(i);
    }

    /**
     * 滚动到当前
     */
    void scrollToCurrent(boolean smoothScroll) {
        isUsingScrollToCalendar = true;
        int position = CalendarUtil.getWeekFromCalendarStartWithMinCalendar(mDelegate.getCurrentDay(),
                mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                mDelegate.getWeekStart()) - 1;
        int curItem = getCurrentItem();
        if (curItem == position) {
            isUsingScrollToCalendar = false;
        }
        setCurrentItem(position, smoothScroll);
        BaseWeekView view = (BaseWeekView) findViewWithTag(position);
        if (view != null) {
            view.performClickCalendar(mDelegate.getCurrentDay(), false);
            view.setSelectedCalendar(mDelegate.getCurrentDay());
            view.invalidate();
        }

        if (mDelegate.mCalendarSelectListener != null && getVisibility() == VISIBLE) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(mDelegate.mSelectedEmmCalendar, false);
        }

        if (getVisibility() == VISIBLE) {
            mDelegate.mInnerListener.onWeekDateSelected(mDelegate.getCurrentDay(), false);
        }
        int i = CalendarUtil.getWeekFromDayInMonth(mDelegate.getCurrentDay(), mDelegate.getWeekStart());
        mParentLayout.updateSelectWeek(i);
    }

    /**
     * 更新任意一个选择的日期
     */
    void updateSelected(EmmCalendar emmCalendar, boolean smoothScroll) {
        int position = CalendarUtil.getWeekFromCalendarStartWithMinCalendar(emmCalendar,
                mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                mDelegate.getWeekStart()) - 1;
        int curItem = getCurrentItem();
        isUsingScrollToCalendar = curItem != position;
        setCurrentItem(position, smoothScroll);
        BaseWeekView view = (BaseWeekView) findViewWithTag(position);
        if (view != null) {
            view.setSelectedCalendar(emmCalendar);
            view.invalidate();
        }
    }


    /**
     * 更新单选模式
     */
    void updateSingleSelect() {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.updateSingleSelect();
        }
    }

    /**
     * 更新为默认选择模式
     */
    void updateDefaultSelect() {
        BaseWeekView view = (BaseWeekView) findViewWithTag(getCurrentItem());
        if (view != null) {
            view.setSelectedCalendar(mDelegate.mSelectedEmmCalendar);
            view.invalidate();
        }
    }

    /**
     * 更新选择效果
     */
    void updateSelected() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.setSelectedCalendar(mDelegate.mSelectedEmmCalendar);
            view.invalidate();
        }
    }


    /**
     * 更新标记日期
     */
    void updateScheme() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.update();
        }
    }

    /**
     * 更新当前日期，夜间过度的时候调用这个函数，一般不需要调用
     */
    void updateCurrentDate() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.updateCurrentDate();
        }
    }

    /**
     * 更新显示模式
     */
    void updateShowMode() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.updateShowMode();
        }
    }

    /**
     * 更新周起始
     */
    void updateWeekStart() {
        int count = getAdapter().getCount();
        mWeekCount = CalendarUtil.getWeekCountBetweenBothCalendar(
                mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                mDelegate.getMaxYear(),
                mDelegate.getMaxYearMonth(),
                mDelegate.getMaxYearDay(),
                mDelegate.getWeekStart());
        /*
         * 如果count发生变化，意味着数据源变化，则必须先调用notifyDataSetChanged()，
         * 否则会抛出异常
         */
        if (count != mWeekCount) {
            isUpdateWeekView = true;
            getAdapter().notifyDataSetChanged();
        }
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.updateWeekStart();
        }
        isUpdateWeekView = false;
        updateSelected(mDelegate.mSelectedEmmCalendar, false);
    }

    /**
     * 更新高度
     */
    final void updateItemHeight() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.updateItemHeight();
            view.requestLayout();
        }
    }

    /**
     * 清除选择范围
     */
    final void clearSelectRange() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.invalidate();
        }
    }

    final void clearSingleSelect() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.mCurrentItem = -1;
            view.invalidate();
        }
    }

    final void clearMultiSelect() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseWeekView view = (BaseWeekView) getChildAt(i);
            view.mCurrentItem = -1;
            view.invalidate();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDelegate.isWeekViewScrollable() && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDelegate.isWeekViewScrollable() && super.onInterceptTouchEvent(ev);
    }

    /**
     * 周视图的高度应该与日历项的高度一致
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mDelegate.getCalendarItemHeight(), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 周视图切换
     */
    private class WeekViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mWeekCount;
        }

        @Override
        public int getItemPosition(Object object) {
            return isUpdateWeekView ? POSITION_NONE : super.getItemPosition(object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            EmmCalendar emmCalendar = CalendarUtil.getFirstCalendarStartWithMinCalendar(mDelegate.getMinYear(),
                    mDelegate.getMinYearMonth(),
                    mDelegate.getMinYearDay(),
                    position + 1,
                    mDelegate.getWeekStart());
            BaseWeekView view;
            try {
                Constructor constructor = mDelegate.getWeekViewClass().getConstructor(Context.class);
                view = (BaseWeekView) constructor.newInstance(getContext());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            view.mParentLayout = mParentLayout;
            view.setup(mDelegate);
            view.setup(emmCalendar);
            view.setTag(position);
            view.setSelectedCalendar(mDelegate.mSelectedEmmCalendar);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            BaseWeekView view = (BaseWeekView) object;
            if (view == null) {
                return;
            }
            view.onDestroy();
            container.removeView(view);
        }

    }
}
