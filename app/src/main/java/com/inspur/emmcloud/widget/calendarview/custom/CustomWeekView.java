package com.inspur.emmcloud.widget.calendarview.custom;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.View;

import com.inspur.emmcloud.widget.calendarview.EmmCalendar;
import com.inspur.emmcloud.widget.calendarview.WeekView;

/**
 * 演示一个变态需求的周视图
 * Created by huanghaibin on 2018/2/9.
 */

public class CustomWeekView extends WeekView {


    private int mRadius;

    /**
     * 自定义魅族标记的文本画笔
     */
    private Paint mTextPaint = new Paint();


    /**
     * 24节气画笔
     */
    private Paint mSolarTermTextPaint = new Paint();

    /**
     * 背景圆点
     */
    private Paint mPointPaint = new Paint();

    /**
     * 今天的背景色
     */
    private Paint mCurrentDayPaint = new Paint();


    /**
     * 圆点半径
     */
    private float mPointRadius;

    private int mPadding;

    private float mCircleRadius;
    /**
     * 自定义魅族标记的圆形背景
     */
    private Paint mSchemeBasicPaint = new Paint();

    private float mSchemeBaseLine;

    public CustomWeekView(Context context) {
        super(context);
        mTextPaint.setTextSize(dipToPx(context, 8));
        mTextPaint.setColor(0x00000000);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFakeBoldText(true);


        mSolarTermTextPaint.setColor(0xff489dff);
        mSolarTermTextPaint.setAntiAlias(true);
        mSolarTermTextPaint.setTextAlign(Paint.Align.CENTER);

        mSchemeBasicPaint.setAntiAlias(true);
        mSchemeBasicPaint.setStyle(Paint.Style.FILL);
        mSchemeBasicPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeBasicPaint.setFakeBoldText(true);
        mSchemeBasicPaint.setColor(Color.WHITE);

        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setTextAlign(Paint.Align.CENTER);
        mPointPaint.setColor(Color.RED);


        mCurrentDayPaint.setAntiAlias(true);
        mCurrentDayPaint.setStyle(Paint.Style.FILL);
        mCurrentDayPaint.setColor(0xFFeaeaea);


        mCircleRadius = dipToPx(getContext(), 7);

        mPadding = dipToPx(getContext(), 0);

        mPointRadius = dipToPx(context, 2.5f);

        Paint.FontMetrics metrics = mSchemeBasicPaint.getFontMetrics();
        mSchemeBaseLine = mCircleRadius - metrics.descent + (metrics.bottom - metrics.top) / 2 + dipToPx(getContext(), 1);

//        //兼容硬件加速无效的代码
//        setLayerType(View.LAYER_TYPE_SOFTWARE, mSelectedPaint);
//        //4.0以上硬件加速会导致无效
//        mSelectedPaint.setMaskFilter(new BlurMaskFilter(28, BlurMaskFilter.Blur.SOLID));

        setLayerType(View.LAYER_TYPE_SOFTWARE, mSchemeBasicPaint);
        mSchemeBasicPaint.setMaskFilter(new BlurMaskFilter(28, BlurMaskFilter.Blur.SOLID));
    }


    @Override
    protected void onPreviewHook() {
        mSolarTermTextPaint.setTextSize(mCurMonthLunarTextPaint.getTextSize());
        mRadius = Math.min(mItemWidth, mItemHeight) / 13 * 5;
    }


    @Override
    protected boolean onDrawSelected(Canvas canvas, EmmCalendar calendar, int x, boolean hasScheme) {
        int cx = x + mItemWidth / 2;
        int cy = mItemHeight / 2;
        canvas.drawCircle(cx, cy, mRadius, mSelectedPaint);
        return true;
    }

    @Override
    protected void onDrawScheme(Canvas canvas, EmmCalendar calendar, int x) {

        mPointPaint.setColor(0xFF95B0C5);
        canvas.drawCircle(x + mItemWidth / 2, mItemHeight - mPointRadius, mPointRadius, mPointPaint);
    }

    @Override
    protected void onDrawText(Canvas canvas, EmmCalendar calendar, int x, boolean hasScheme, boolean isSelected) {
        int cx = x + mItemWidth / 2;
        int top = -(int)(mItemHeight / 5.5);
        String day =calendar.isCurrentDay()?"今":String.valueOf(calendar.getDay());
        if(hasScheme){
            mTextPaint.setColor(calendar.getSchemeColor());
            canvas.drawText(calendar.getScheme(), x + mItemWidth - mPadding - mCircleRadius, mPadding + mSchemeBaseLine+dipToPx(getContext(),6 ), mTextPaint);
        }
        if (isSelected) {
            canvas.drawText(day, cx, mTextBaseLine + top,
                    mSelectTextPaint);
            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + mItemHeight / 15, mSelectedLunarTextPaint);
        } else if (hasScheme) {

            canvas.drawText(day, cx, mTextBaseLine + top,
                    calendar.isCurrentMonth() ? mSchemeTextPaint : mOtherMonthTextPaint);

            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + mItemHeight / 15,
                    !TextUtils.isEmpty(calendar.getSolarTerm()) ? mSolarTermTextPaint : mSchemeLunarTextPaint);
        } else {
            canvas.drawText(day, cx, mTextBaseLine + top,
                    calendar.isCurrentDay() ? mCurDayTextPaint :
                            calendar.isCurrentMonth() ? mCurMonthTextPaint : mOtherMonthTextPaint);

            canvas.drawText(calendar.getLunar(), cx, mTextBaseLine + mItemHeight / 15,
                    calendar.isCurrentDay() ? mCurDayLunarTextPaint :
                            !TextUtils.isEmpty(calendar.getSolarTerm()) ? mSolarTermTextPaint :
                                    calendar.isCurrentMonth() ?
                                            mCurMonthLunarTextPaint : mOtherMonthLunarTextPaint);
        }
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
}
