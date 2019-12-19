package com.inspur.emmcloud.baselib.widget.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.inspur.baselib.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;

public class CircleProgressBar extends ProgressBar {

    private static final int DEFAULT_TEXT_COLOR = 0XFFd5d5d5;
    private static final int DEFAULT_COLOR_UNREACHED_COLOR = 0xFFd3d6da;
    private static final int DEFAULT_HEIGHT_REACHED_PROGRESS_BAR = 2;
    private static final int DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR = 2;
    protected int mReachedProgressBarHeight = DensityUtil.dip2px(DEFAULT_HEIGHT_REACHED_PROGRESS_BAR);
    protected int mUnReachedBarColor = DEFAULT_COLOR_UNREACHED_COLOR;
    protected int mUnReachedProgressBarHeight = DensityUtil.dip2px(DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR);
    protected int mReachedBarColor = DEFAULT_TEXT_COLOR;
    Paint mPaint = new Paint();
    Status mStatus = Status.Success;
    private int triangleLength;
    private Path mPath;
    private int mRadius = DensityUtil.dip2px(13);
    private Bitmap failedBitmap;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CusCircleProgressBar);

        mReachedBarColor = array.getColor(
                R.styleable.CusCircleProgressBar_circle_progress_reached_color,
                Color.BLUE);
        mUnReachedBarColor = array.getColor(
                R.styleable.CusCircleProgressBar_circle_progress_unreached_color,
                DEFAULT_COLOR_UNREACHED_COLOR);
        mReachedProgressBarHeight = (int) array.getDimension(
                R.styleable.CusCircleProgressBar_circle_progress_reached_bar_height,
                mReachedProgressBarHeight);
        mUnReachedProgressBarHeight = (int) array.getDimension(
                R.styleable.CusCircleProgressBar_circle_progress_unreached_bar_height,
                mUnReachedProgressBarHeight);

        mRadius = (int) array.getDimension(
                R.styleable.CusCircleProgressBar_circle_progress_radius, mRadius);
        triangleLength = mRadius;
        array.recycle();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mPath = new Path();//need path to draw triangle
        initPath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = true;
        failedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ee_1, options);
    }

    private void initPath() {

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int paintWidth = Math.max(mReachedProgressBarHeight,
                mUnReachedProgressBarHeight);
        if (heightMode != MeasureSpec.EXACTLY) {
            int exceptHeight = (int) (getPaddingTop() + getPaddingBottom()
                    + mRadius * 2 + paintWidth);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(exceptHeight,
                    MeasureSpec.EXACTLY);
        }
        if (widthMode != MeasureSpec.EXACTLY) {
            int exceptWidth = (int) (getPaddingLeft() + getPaddingRight()
                    + mRadius * 2 + paintWidth);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(exceptWidth,
                    MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        mPaint.setStyle(Paint.Style.STROKE);
        // draw unreaded bar
        if (mStatus == Status.Success) {
            mPaint.setColor(mReachedBarColor);
        } else if (mStatus == Status.Fail) {
            mPaint.setColor(Color.parseColor("#FFFFCC00"));
            mPaint.setStyle(Paint.Style.FILL);
        } else {
            mPaint.setColor(mUnReachedBarColor);
        }
        mPaint.setStrokeWidth(mUnReachedProgressBarHeight);
        canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);
        if (mStatus != Status.Fail) {
            // draw reached bar
            mPaint.setColor(mReachedBarColor);
            mPaint.setStrokeWidth(mReachedProgressBarHeight);
            float sweepAngle = getProgress() * 1.0f / getMax() * 360;
            canvas.drawArc(new RectF(0, 0, mRadius * 2, mRadius * 2), -90,
                    sweepAngle, false, mPaint);
        }
        canvas.restore();
        mPath = new Path();//need path to draw triangle

        triangleLength = mRadius;
        float leftX = (float) ((2 * mRadius - Math.sqrt(3.0) / 2 * triangleLength) / 2);
        float realX = (float) (leftX + leftX * 0.2);

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());

        if (mStatus == Status.Pause) {//未开始状态，画笔填充
            mPath.moveTo(realX, mRadius - (triangleLength / 2));
            mPath.lineTo(realX, mRadius + (triangleLength / 2));
            mPath.lineTo((float) (realX + Math.sqrt(3.0) / 2 * triangleLength), mRadius);
            mPath.lineTo(realX, mRadius - (triangleLength / 2));

            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(mPath, mPaint);
        } else if (mStatus == Status.Starting || mStatus == Status.Loading) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(DensityUtil.dip2px(2));
            canvas.drawLine(mRadius * 2 / 3, mRadius * 2 / 3, mRadius * 2 / 3, 2 * mRadius * 2 / 3, mPaint);
            canvas.drawLine(2 * mRadius - (mRadius * 2 / 3), mRadius * 2 / 3, 2 * mRadius - (mRadius * 2 / 3), 2 * mRadius * 2 / 3, mPaint);
        } else if (mStatus == Status.Success) {
            mPath.moveTo(realX - mRadius / 7, mRadius);
            mPath.lineTo(realX + mRadius / 4, mRadius * 4 / 3);
            mPath.lineTo(realX + mRadius * 4 / 5, mRadius - (triangleLength / 4));
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(DensityUtil.dip2px(3));
            canvas.drawPath(mPath, mPaint);
        } else if (mStatus == Status.Fail) {
//            mPath.moveTo(mRadius - mRadius / 3, mRadius - mRadius / 3);
//            mPath.lineTo(mRadius + mRadius / 3, mRadius + mRadius / 3 );
//            mPath.moveTo(mRadius - mRadius / 3, mRadius + mRadius / 3);
//            mPath.lineTo(mRadius + mRadius / 3, mRadius - mRadius / 3);
//            mPaint.setColor(Color.RED);
//            mPaint.setStyle(Paint.Style.STROKE);
//            mPaint.setStrokeWidth(DensityUtil.dip2px(3));
//            canvas.drawPath(mPath, mPaint);
//            canvas.translate(-getPaddingLeft(), -getPaddingTop());
//            mPaint.setAntiAlias(true);
//            mPaint.setFilterBitmap(true);
//
//            canvas.drawBitmap(failedBitmap, 0, 0, mPaint);
//            canvas.translate(-getPaddingLeft(), -getPaddingTop());
            mPaint.setColor(Color.WHITE);
            mPaint.setTextSize(DensityUtil.dip2px(20));
            canvas.drawText("!", mRadius * 4 / 5, mRadius * 8 / 5, mPaint);
        }
        canvas.restore();
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        mStatus = status;
        invalidate();
    }

    public enum Status {
        Starting,
        Loading,
        Pause,
        Success,
        Fail
    }
}
