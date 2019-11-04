package com.inspur.emmcloud.baselib.widget.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
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
    Status mStatus = Status.End;
    private int triangleLength;
    private Path mPath;
    private int mRadius = DensityUtil.dip2px(13);


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
        mPaint.setColor(mUnReachedBarColor);
        mPaint.setStrokeWidth(mUnReachedProgressBarHeight);
        canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);
        // draw reached bar
        mPaint.setColor(mReachedBarColor);
        mPaint.setStrokeWidth(mReachedProgressBarHeight);
        float sweepAngle = getProgress() * 1.0f / getMax() * 360 - 90;
        canvas.drawArc(new RectF(0, 0, mRadius * 2, mRadius * 2), -90,
                sweepAngle, false, mPaint);
        canvas.restore();
        mPath = new Path();//need path to draw triangle

        triangleLength = mRadius;
        float leftX = (float) ((2 * mRadius - Math.sqrt(3.0) / 2 * triangleLength) / 2);
        float realX = (float) (leftX + leftX * 0.2);
        mPath.moveTo(realX, mRadius - (triangleLength / 2));
        mPath.lineTo(realX, mRadius + (triangleLength / 2));
        mPath.lineTo((float) (realX + Math.sqrt(3.0) / 2 * triangleLength), mRadius);
        mPath.lineTo(realX, mRadius - (triangleLength / 2));

        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());

        if (mStatus == Status.End || mStatus == Status.Pause) {//未开始状态，画笔填充
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(mPath, mPaint);//直接drawPath
        } else if (mStatus == Status.Starting || mStatus == Status.Uploading || mStatus == Status.Downloading) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(DensityUtil.dip2px(2));
            canvas.drawLine(mRadius * 2 / 3, mRadius * 2 / 3, mRadius * 2 / 3, 2 * mRadius * 2 / 3, mPaint);
            canvas.drawLine(2 * mRadius - (mRadius * 2 / 3), mRadius * 2 / 3, 2 * mRadius - (mRadius * 2 / 3), 2 * mRadius * 2 / 3, mPaint);
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
        Uploading,
        Downloading,
        Pause,
        End
    }

}
