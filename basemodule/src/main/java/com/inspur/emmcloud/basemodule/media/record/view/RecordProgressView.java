package com.inspur.emmcloud.basemodule.media.record.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.inspur.emmcloud.basemodule.R;


/**
 * Date：2022/6/10
 * Author：wang zhen
 * Description 进度条
 */
public class RecordProgressView extends View {
    private Paint mPaint; // 进度条paint
    private float progress; // 进度
    private RectF processRect; // 矩阵
    private final float processWidth = 10; // 进度条宽度

    public RecordProgressView(Context context) {
        super(context);
        initViews();
    }

    public RecordProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public RecordProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        // 初始化进度条paint
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.white));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(processWidth);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (processRect != null) {
            canvas.drawArc(processRect, -90, progress, false, mPaint);
        }
    }

    public void setProgress(long milliSecond) {
        // 设置录制时长60秒
        progress = (float) milliSecond * 360 / 60000;
        invalidate();
    }

    public void createProcess() {
        processRect = new RectF(processWidth / 2, processWidth / 2, getWidth() - processWidth / 2, getWidth() - processWidth / 2);
    }

    public void stopDraw() {
//        processRect = new RectF(0, 0, 0, 0);
        processRect = null;
        invalidate();
    }
}
