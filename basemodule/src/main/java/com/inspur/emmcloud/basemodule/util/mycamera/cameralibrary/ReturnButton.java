package com.inspur.emmcloud.basemodule.util.mycamera.cameralibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class ReturnButton extends View {

    Path path;
    private int size;
    private int center_X;
    private int center_Y;
    private float strokeWidth;
    private Paint paint;

    public ReturnButton(Context context, int size) {
        this(context);
        this.size = size;
        center_X = size / 2;
        center_Y = size / 2;

        strokeWidth = size / 15f;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);

        path = new Path();
    }

    public ReturnButton(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widgetSize = (int) (size * 1.5);
        setMeasuredDimension(widgetSize, widgetSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int moveX = size / 5;
        int moveY = (int) (size / 2.5);
        path.moveTo(strokeWidth + moveX, strokeWidth / 2 + moveY);
        path.lineTo(center_X + moveX, center_Y - strokeWidth / 2 + moveY);
        path.lineTo(size - strokeWidth + moveX, strokeWidth / 2 + moveY);
        canvas.drawPath(path, paint);
    }
}
