package com.inspur.emmcloud.widget.calendarview.bussiness;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.inspur.emmcloud.widget.calendarview.EmmCalendar;
import com.inspur.emmcloud.widget.calendarview.WeekView;


/**
 * 魅族周视图
 * Created by huanghaibin on 2017/11/29.
 */

public class MeizuWeekView extends WeekView {
    private Paint mTextPaint = new Paint();

    private Paint mSchemeBasicPaint = new Paint();
    private float mRadio;
    private int mPadding;
    private float mSchemeBaseLine;

    public MeizuWeekView(Context context) {
        super(context);

        mTextPaint.setTextSize(dipToPx(context, 8));
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFakeBoldText(true);

        mSchemeBasicPaint.setAntiAlias(true);
        mSchemeBasicPaint.setStyle(Paint.Style.FILL);
        mSchemeBasicPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeBasicPaint.setColor(0xffed5353);
        mSchemeBasicPaint.setFakeBoldText(true);
        mRadio = dipToPx(getContext(), 7);
        mPadding = dipToPx(getContext(), 4);
        Paint.FontMetrics metrics = mSchemeBasicPaint.getFontMetrics();
        mSchemeBaseLine = mRadio - metrics.descent + (metrics.bottom - metrics.top) / 2 + dipToPx(getContext(), 1);

        //兼容硬件加速无效的代码
        setLayerType(View.LAYER_TYPE_SOFTWARE, mSchemeBasicPaint);
        //4.0以上硬件加速会导致无效
        mSchemeBasicPaint.setMaskFilter(new BlurMaskFilter(25, BlurMaskFilter.Blur.SOLID));
    }

    /**
     * dp转px
     *
     * @param context context
     * @param dpValue dp
     * @return px
     */
    private static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * @param canvas    canvas
     * @param emmCalendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    @Override
    protected boolean onDrawSelected(Canvas canvas, EmmCalendar emmCalendar, int x, boolean hasScheme) {
        mSelectedPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x + mPadding, mPadding, x + mItemWidth - mPadding, mItemHeight - mPadding, mSelectedPaint);
        return true;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, EmmCalendar emmCalendar, int x) {
        mSchemeBasicPaint.setColor(emmCalendar.getSchemeColor());

        canvas.drawCircle(x + mItemWidth - mPadding - mRadio / 2, mPadding + mRadio, mRadio, mSchemeBasicPaint);

        canvas.drawText(emmCalendar.getScheme(),
                x + mItemWidth - mPadding - mRadio / 2 - getTextWidth(emmCalendar.getScheme()) / 2,
                mPadding + mSchemeBaseLine, mTextPaint);
    }

    private float getTextWidth(String text) {
        return mTextPaint.measureText(text);
    }

    @Override
    protected void onDrawText(Canvas canvas, EmmCalendar emmCalendar, int x, boolean hasScheme, boolean isSelected) {
        int cx = x + mItemWidth / 2;
        int top = -mItemHeight / 6;

        boolean isInRange = isInRange(emmCalendar);

        if (isSelected) {
            canvas.drawText(String.valueOf(emmCalendar.getDay()), cx, mTextBaseLine + top,
                    mSelectTextPaint);
            canvas.drawText(emmCalendar.getLunar(), cx, mTextBaseLine + mItemHeight / 10, mSelectedLunarTextPaint);
        } else if (hasScheme) {
            canvas.drawText(String.valueOf(emmCalendar.getDay()), cx, mTextBaseLine + top,
                    emmCalendar.isCurrentMonth() && isInRange ? mSchemeTextPaint : mOtherMonthTextPaint);

            canvas.drawText(emmCalendar.getLunar(), cx, mTextBaseLine + mItemHeight / 10, mCurMonthLunarTextPaint);
        } else {
            canvas.drawText(String.valueOf(emmCalendar.getDay()), cx, mTextBaseLine + top,
                    emmCalendar.isCurrentDay() && isInRange ? mCurDayTextPaint :
                            emmCalendar.isCurrentMonth() && isInRange ? mCurMonthTextPaint : mOtherMonthTextPaint);
            canvas.drawText(emmCalendar.getLunar(), cx, mTextBaseLine + mItemHeight / 10,
                    emmCalendar.isCurrentDay() && isInRange ? mCurDayLunarTextPaint :
                            emmCalendar.isCurrentMonth() ? mCurMonthLunarTextPaint : mOtherMonthLunarTextPaint);
        }
    }
}
