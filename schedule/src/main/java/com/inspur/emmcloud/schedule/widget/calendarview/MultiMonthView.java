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

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * 多选月视图
 * Created by huanghaibin on 2018/9/11.
 */
public abstract class MultiMonthView extends BaseMonthView {

    public MultiMonthView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLineCount == 0)
            return;
        mItemWidth = (getWidth() - 2 * mDelegate.getCalendarPadding()) / 7;
        onPreviewHook();
        int count = mLineCount * 7;
        int d = 0;
        for (int i = 0; i < mLineCount; i++) {
            for (int j = 0; j < 7; j++) {
                EmmCalendar emmCalendar = mItems.get(d);
                if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH) {
                    if (d > mItems.size() - mNextDiff) {
                        return;
                    }
                    if (!emmCalendar.isCurrentMonth()) {
                        ++d;
                        continue;
                    }
                } else if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_FIT_MONTH) {
                    if (d >= count) {
                        return;
                    }
                }
                draw(canvas, emmCalendar, i, j);
                ++d;
            }
        }
    }

    /**
     * 开始绘制
     *
     * @param canvas      canvas
     * @param emmCalendar 对应日历
     * @param i           i
     * @param j           j
     */
    private void draw(Canvas canvas, EmmCalendar emmCalendar, int i, int j) {
        int x = j * mItemWidth + mDelegate.getCalendarPadding();
        int y = i * mItemHeight;
        onLoopStart(x, y);
        boolean isSelected = isCalendarSelected(emmCalendar);
        boolean hasScheme = emmCalendar.hasScheme();
        boolean isPreSelected = isSelectPreCalendar(emmCalendar);
        boolean isNextSelected = isSelectNextCalendar(emmCalendar);

        if (hasScheme) {
            //标记的日子
            boolean isDrawSelected = false;//是否继续绘制选中的onDrawScheme
            if (isSelected) {
                isDrawSelected = onDrawSelected(canvas, emmCalendar, x, y, true, isPreSelected, isNextSelected);
            }
            if (isDrawSelected || !isSelected) {
                //将画笔设置为标记颜色
                mSchemePaint.setColor(emmCalendar.getSchemeColor() != 0 ? emmCalendar.getSchemeColor() : mDelegate.getSchemeThemeColor());
                onDrawScheme(canvas, emmCalendar, x, y, true);
            }
        } else {
            if (isSelected) {
                onDrawSelected(canvas, emmCalendar, x, y, false, isPreSelected, isNextSelected);
            }
        }
        onDrawText(canvas, emmCalendar, x, y, hasScheme, isSelected);
    }

    /**
     * 日历是否被选中
     *
     * @param emmCalendar calendar
     * @return 日历是否被选中
     */
    protected boolean isCalendarSelected(EmmCalendar emmCalendar) {
        return !onCalendarIntercept(emmCalendar) && mDelegate.mSelectedCalendars.containsKey(emmCalendar.toString());
    }

    @Override
    public void onClick(View v) {
        if (!isClick) {
            return;
        }
        EmmCalendar emmCalendar = getIndex();

        if (emmCalendar == null) {
            return;
        }

        if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH
                && !emmCalendar.isCurrentMonth()) {
            return;
        }

        if (onCalendarIntercept(emmCalendar)) {
            mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(emmCalendar, true);
            return;
        }

        if (!isInRange(emmCalendar)) {
            if (mDelegate.mCalendarMultiSelectListener != null) {
                mDelegate.mCalendarMultiSelectListener.onCalendarMultiSelectOutOfRange(emmCalendar);
            }
            return;
        }

        String key = emmCalendar.toString();

        if (mDelegate.mSelectedCalendars.containsKey(key)) {
            mDelegate.mSelectedCalendars.remove(key);
        } else {
            if (mDelegate.mSelectedCalendars.size() >= mDelegate.getMaxMultiSelectSize()) {
                if (mDelegate.mCalendarMultiSelectListener != null) {
                    mDelegate.mCalendarMultiSelectListener.onMultiSelectOutOfSize(emmCalendar,
                            mDelegate.getMaxMultiSelectSize());
                }
                return;
            }
            mDelegate.mSelectedCalendars.put(key, emmCalendar);
        }

        mCurrentItem = mItems.indexOf(emmCalendar);

        if (!emmCalendar.isCurrentMonth() && mMonthViewPager != null) {
            int cur = mMonthViewPager.getCurrentItem();
            int position = mCurrentItem < 7 ? cur - 1 : cur + 1;
            mMonthViewPager.setCurrentItem(position);
        }

        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onMonthDateSelected(emmCalendar, true);
        }

        if (mParentLayout != null) {
            if (emmCalendar.isCurrentMonth()) {
                mParentLayout.updateSelectPosition(mItems.indexOf(emmCalendar));
            } else {
                mParentLayout.updateSelectWeek(CalendarUtil.getWeekFromDayInMonth(emmCalendar, mDelegate.getWeekStart()));
            }
        }
        if (mDelegate.mCalendarMultiSelectListener != null) {
            mDelegate.mCalendarMultiSelectListener.onCalendarMultiSelect(
                    emmCalendar,
                    mDelegate.mSelectedCalendars.size(),
                    mDelegate.getMaxMultiSelectSize());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    /**
     * 上一个日期是否选中
     *
     * @param emmCalendar 当前日期
     * @return 上一个日期是否选中
     */
    protected final boolean isSelectPreCalendar(EmmCalendar emmCalendar) {
        return !onCalendarIntercept(emmCalendar) &&
                isCalendarSelected(CalendarUtil.getPreCalendar(emmCalendar));
    }

    /**
     * 下一个日期是否选中
     *
     * @param emmCalendar 当前日期
     * @return 下一个日期是否选中
     */
    protected final boolean isSelectNextCalendar(EmmCalendar emmCalendar) {
        return !onCalendarIntercept(emmCalendar) &&
                isCalendarSelected(CalendarUtil.getNextCalendar(emmCalendar));
    }

    /**
     * 绘制选中的日期
     *
     * @param canvas         canvas
     * @param emmCalendar    日历日历calendar
     * @param x              日历Card x起点坐标
     * @param y              日历Card y起点坐标
     * @param hasScheme      hasScheme 非标记的日期
     * @param isSelectedPre  上一个日期是否选中
     * @param isSelectedNext 下一个日期是否选中
     * @return 是否继续绘制onDrawScheme，true or false
     */
    protected abstract boolean onDrawSelected(Canvas canvas, EmmCalendar emmCalendar, int x, int y, boolean hasScheme,
                                              boolean isSelectedPre, boolean isSelectedNext);

    /**
     * 绘制标记的日期,这里可以是背景色，标记色什么的
     *
     * @param canvas      canvas
     * @param emmCalendar 日历calendar
     * @param x           日历Card x起点坐标
     * @param y           日历Card y起点坐标
     * @param isSelected  是否选中
     */
    protected abstract void onDrawScheme(Canvas canvas, EmmCalendar emmCalendar, int x, int y, boolean isSelected);


    /**
     * 绘制日历文本
     *
     * @param canvas      canvas
     * @param emmCalendar 日历calendar
     * @param x           日历Card x起点坐标
     * @param y           日历Card y起点坐标
     * @param hasScheme   是否是标记的日期
     * @param isSelected  是否选中
     */
    protected abstract void onDrawText(Canvas canvas, EmmCalendar emmCalendar, int x, int y, boolean hasScheme, boolean isSelected);
}
