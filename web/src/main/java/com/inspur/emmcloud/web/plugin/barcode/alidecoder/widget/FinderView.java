package com.inspur.emmcloud.web.plugin.barcode.alidecoder.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.barcode.alidecoder.Utils;


/**
 * 负责绘制扫码窗口、边角及周边阴影
 */
public class FinderView extends View {

    private static final int DEFAULT_SHADOW_COLOR = 0x96000000;

    private int scanWindowLeft, scanWindowTop, scanWindowRight, scanWindowBottom;
    private Bitmap leftTopCorner, rightTopCorner, leftBottomCorner, rightBottomCorner;
    private Paint paint;
    private int shadowColor;

    public FinderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public FinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        applyConfig(context, attrs);
        setVisibility(INVISIBLE);
        initCornerBitmap(context);

        paint = new Paint();
        paint.setAntiAlias(true);
    }

    private void applyConfig(Context context, AttributeSet attrs) {
        shadowColor = DEFAULT_SHADOW_COLOR;
    }

    private void initCornerBitmap(Context context) {
        Resources res = context.getResources();
        leftTopCorner = BitmapFactory.decodeResource(res, R.drawable.web_qrcode_scan_window_corner_left_top);
        rightTopCorner = BitmapFactory.decodeResource(res, R.drawable.web_qrcode_scan_window_corner_right_top);
        leftBottomCorner = BitmapFactory.decodeResource(res, R.drawable.web_qrcode_scan_window_corner_left_bottom);
        rightBottomCorner = BitmapFactory.decodeResource(res, R.drawable.web_qrcode_scan_window_corner_right_bottom);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawShadow(canvas);
        drawCorner(canvas);
    }

    private void drawCorner(Canvas canvas) {
        paint.setAlpha(255);
        canvas.drawBitmap(leftTopCorner, scanWindowLeft, scanWindowTop, paint);
        canvas.drawBitmap(rightTopCorner, scanWindowRight - rightTopCorner.getWidth(), scanWindowTop, paint);
        canvas.drawBitmap(leftBottomCorner, scanWindowLeft, scanWindowBottom - leftBottomCorner.getHeight(), paint);
        canvas.drawBitmap(rightBottomCorner, scanWindowRight - rightBottomCorner.getWidth(), scanWindowBottom - rightBottomCorner.getHeight(), paint);
    }

    private void drawShadow(Canvas canvas) {
        paint.setColor(shadowColor);
        canvas.drawRect(0, 0, getWidth(), scanWindowTop, paint);
        canvas.drawRect(0, scanWindowTop, scanWindowLeft, scanWindowBottom, paint);
        canvas.drawRect(scanWindowRight, scanWindowTop, getWidth(), scanWindowBottom, paint);
        canvas.drawRect(0, scanWindowBottom, getWidth(), getHeight(), paint);
    }

    /**
     * 根据 RayView 的位置决定扫码窗口的位置
     */
    public void setScanWindowLocation(int left, int top, int right, int bottom) {
        scanWindowLeft = left;
        scanWindowTop = top;
        scanWindowRight = right;
        scanWindowBottom = bottom;
        invalidate();
        setVisibility(VISIBLE);
    }

    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
    }

    public void setCornerColor(int angleColor) {
        leftTopCorner = Utils.changeBitmapColor(leftTopCorner, angleColor);
        rightTopCorner = Utils.changeBitmapColor(rightTopCorner, angleColor);
        leftBottomCorner = Utils.changeBitmapColor(leftBottomCorner, angleColor);
        rightBottomCorner = Utils.changeBitmapColor(rightBottomCorner, angleColor);
    }
}
