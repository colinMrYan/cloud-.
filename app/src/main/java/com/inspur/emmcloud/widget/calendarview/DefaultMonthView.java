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
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * 默认高仿魅族日历布局
 * Created by huanghaibin on 2017/11/15.
 */

public final class DefaultMonthView extends MonthView {

    private Paint mTextPaint = new Paint();
    private Paint mSchemeBasicPaint = new Paint();
    private float mRadio;
    private int mPadding;
    private float mSchemeBaseLine;

    public DefaultMonthView(Context context) {
        super(context);

        mTextPaint.setTextSize(CalendarUtil.dipToPx(context, 8));
        mTextPaint.setColor(0xff36A5F6);
        mTextPaint.setAntiAlias(true);

        mSchemeBasicPaint.setAntiAlias(true);
        mSchemeBasicPaint.setStyle(Paint.Style.FILL);
        mSchemeBasicPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeBasicPaint.setColor(0xffed5353);
        mSchemeBasicPaint.setFakeBoldText(true);
        mRadio = CalendarUtil.dipToPx(getContext(), 7);
        mPadding = CalendarUtil.dipToPx(getContext(), 4);
        Paint.FontMetrics metrics = mSchemeBasicPaint.getFontMetrics();
        mSchemeBaseLine = mRadio - metrics.descent + (metrics.bottom - metrics.top) / 2 + CalendarUtil.dipToPx(getContext(), 1);

    }

    /**
     * @param canvas      canvas
     * @param emmCalendar 日历日历calendar
     * @param x           日历Card x起点坐标
     * @param y           日历Card y起点坐标
     * @param hasScheme   hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    @Override
    protected boolean onDrawSelected(Canvas canvas, EmmCalendar emmCalendar, int x, int y, boolean hasScheme) {
        mSelectedPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x + mPadding + 20, y + mPadding, x + mItemWidth - mPadding - 20, y + mItemHeight - mPadding, mSelectedPaint);
        return true;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, EmmCalendar emmCalendar, int x, int y) {
        mSchemeBasicPaint.setColor(emmCalendar.getSchemeColor());

        canvas.drawCircle(x + mItemWidth - mPadding - mRadio / 2, y + mPadding + mRadio, mRadio, mSchemeBasicPaint);

        canvas.drawText(emmCalendar.getScheme(),
                x + mItemWidth - mPadding - mRadio / 2 - getTextWidth(emmCalendar.getScheme()) / 2,
                y + mPadding + mSchemeBaseLine, mTextPaint);
        canvas.drawCircle(x + mItemWidth / 2, y + mItemHeight - 10, 5, mSchemeBasicPaint);
    }


    /**
     * 获取字体的宽
     *
     * @param text text
     * @return return
     */
    private float getTextWidth(String text) {
        return mTextPaint.measureText(text);
    }

    @Override
    protected void onDrawText(Canvas canvas, EmmCalendar emmCalendar, int x, int y, boolean hasScheme, boolean isSelected) {
        int cx = x + mItemWidth / 2;
        int top = y - mItemHeight / 6;

        if (isSelected) {
            canvas.drawText(String.valueOf(emmCalendar.getDay()), cx, mTextBaseLine + top,
                    mSelectTextPaint);
            canvas.drawText(emmCalendar.getLunar(), cx, mTextBaseLine + y + mItemHeight / 10, mSelectedLunarTextPaint);
        } else if (hasScheme) {
            canvas.drawText(String.valueOf(emmCalendar.getDay()), cx, mTextBaseLine + top,
                    emmCalendar.isCurrentDay() ? mCurDayTextPaint :
                            emmCalendar.isCurrentMonth() ? mSchemeTextPaint : mOtherMonthTextPaint);

            canvas.drawText(emmCalendar.getLunar(), cx, mTextBaseLine + y + mItemHeight / 10,
                    emmCalendar.isCurrentDay() ? mCurDayLunarTextPaint : mSchemeLunarTextPaint);
        } else {
            canvas.drawText(String.valueOf(emmCalendar.getDay()), cx, mTextBaseLine + top,
                    emmCalendar.isCurrentDay() ? mCurDayTextPaint :
                            emmCalendar.isCurrentMonth() ? mCurMonthTextPaint : mOtherMonthTextPaint);
            canvas.drawText(emmCalendar.getLunar(), cx, mTextBaseLine + y + mItemHeight / 10,
                    emmCalendar.isCurrentDay() ? mCurDayLunarTextPaint :
                            emmCalendar.isCurrentMonth() ? mCurMonthLunarTextPaint : mOtherMonthLunarTextPaint);
        }
    }
}
