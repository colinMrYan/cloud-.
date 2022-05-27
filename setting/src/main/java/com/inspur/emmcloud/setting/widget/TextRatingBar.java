package com.inspur.emmcloud.setting.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.ui.DarkUtil;
import com.inspur.emmcloud.setting.R;


public class TextRatingBar extends View {

    private static final String TAG = "TextRatingBar";
    //paddingLeft
    private int mLeft;
    //paddingTop
    private int mTop;
    //当前rating
    private int mRating;
    //总raring数
    private int mCount;
    //rating文字
    private String[] texts = {"A", "标准", "", "", "A"};
    //相邻raring的距离
    private int mUnitSize;
    //bar到底部的距离
    private int mYOffset;
    //小竖条的一半长度
    private int mMarkSize;

    private int mRoundRectPadding;

    Paint mTextPaint = new Paint();
    Paint mCirclePaintBig = new Paint();
    Paint mCirclePaintSmall = new Paint();
    Paint mTextBgPaint = new Paint();

    public TextRatingBar(Context context) {
        this(context, null);
    }

    public TextRatingBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextRatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCount = 5;
        mRating = 0;
        mMarkSize = DensityUtil.dip2px(8);
        mCirclePaintBig.setColor(Color.parseColor("#EBEFF5"));
        mCirclePaintSmall.setColor(Color.parseColor("#388EFF"));
        mTextBgPaint.setColor(Color.parseColor(DarkUtil.isDarkTheme() ? "#292929" : "#F5F5F5"));
        mTextPaint.setStrokeWidth(DensityUtil.dip2px(3));
        mTextPaint.setColor(Color.GRAY);
        mRoundRectPadding = DensityUtil.dip2px(2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LogUtils.debug(TAG, getMeasuredWidth() + " " + getMeasuredHeight());
        mLeft = (getPaddingLeft() + getPaddingRight()) / 2;
        mTop = getPaddingTop();
        int barWidth = getMeasuredWidth() - 2 * mLeft;
        mUnitSize = barWidth / (mCount - 1);
        mYOffset = getMeasuredHeight() - getPaddingBottom();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        //1.画横线
        canvas.drawLine(mLeft, mYOffset, mLeft + mRating * mUnitSize, mYOffset, mTextPaint);
        canvas.drawLine(mLeft + mRating * mUnitSize, mYOffset, mLeft + (mCount - 1) * mUnitSize, mYOffset, mTextPaint);

        //2.画竖线
        for (int i = 0; i < mCount; i++) {
            canvas.drawLine(mLeft + i * mUnitSize, mYOffset - mMarkSize, mLeft + i * mUnitSize, mYOffset + mMarkSize, mTextPaint);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
        }
        //3.画文字
        mTextPaint.setTextSize(DensityUtil.dip2px(14));
        canvas.drawText(texts[0], mLeft, mTop, mTextPaint);

        if (isTextEnglish()) {
            canvas.drawRoundRect(new RectF(mLeft + mUnitSize - 12 * mRoundRectPadding, mTop - 7 * mRoundRectPadding, mLeft + mUnitSize + 12 * mRoundRectPadding, mTop + 2 * mRoundRectPadding),
                    mRoundRectPadding, mRoundRectPadding, mTextBgPaint);
        } else {
            canvas.drawRoundRect(new RectF(mLeft + mUnitSize - 8 * mRoundRectPadding, mTop - 7 * mRoundRectPadding, mLeft + mUnitSize + 8 * mRoundRectPadding, mTop + 2 * mRoundRectPadding),
                    mRoundRectPadding, mRoundRectPadding, mTextBgPaint);
        }

        canvas.drawText((String) getResources().getText(R.string.news_font_normal), mLeft + mUnitSize, mTop, mTextPaint);

        mTextPaint.setTextSize(DensityUtil.dip2px(20));
        canvas.drawText(texts[4], mLeft + 4 * mUnitSize, mTop, mTextPaint);

        //4.画选中logo
        canvas.drawCircle(mLeft + mRating * mUnitSize, mYOffset, DensityUtil.dip2px(12), mCirclePaintBig);
        canvas.drawCircle(mLeft + mRating * mUnitSize, mYOffset, DensityUtil.dip2px(6), mCirclePaintSmall);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            for (int i = 0; i < mCount; i++) {
                float distance = mLeft + i * mUnitSize - x;
                if (Math.abs(distance) < 100) {
                    setRating(i);
                    if (onRatingListener != null) {
                        onRatingListener.onRating(mRating);
                    }
                    break;
                }
            }
        }
        return true;
    }

    private boolean isTextEnglish() {
        return getResources().getString(R.string.news_font_normal).equals("Normal");
    }


    public void setRating(int rating) {
        mRating = rating;
        invalidate();
    }

    private OnRatingListener onRatingListener;

    public void setOnRatingListener(OnRatingListener onRatingListener) {
        this.onRatingListener = onRatingListener;
    }

    public interface OnRatingListener {
        void onRating(int rating);
    }
}
