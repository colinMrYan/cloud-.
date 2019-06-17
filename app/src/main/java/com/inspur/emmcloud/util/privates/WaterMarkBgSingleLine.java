package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.inspur.emmcloud.baselib.util.DensityUtil;

/**
 * Created by libaochao on 2018/10/31.
 */

public class WaterMarkBgSingleLine extends Drawable {

    private Paint mPaint = new Paint();
    private String mLabels;
    private Context mContext;
    private int mAngle = -20;//角度
    private int mFontSize = 30;//字体大小 单位sp
    private String mPaintColor = "#dfE7E7E7";
    private Boolean mTextBoldState = true;
    private int mLeftBadingDp = 0;
    private int mTopBadingDp = 0;
    private int mLevelInterval = 50;
    private int mVerticalInterval = 50;

    public WaterMarkBgSingleLine(Context context, int Angle, int FontSize, String PaintColor, Boolean TextBoldState, int LeftBadingDp, int TopBadingDp, int Level, int vertical, String Lable) {
        mAngle = Angle;
        mFontSize = FontSize;
        mPaintColor = PaintColor;
        mTextBoldState = TextBoldState;
        mLeftBadingDp = DensityUtil.dip2px(context, LeftBadingDp);
        mTopBadingDp = DensityUtil.dip2px(context, TopBadingDp);
        mLevelInterval = DensityUtil.dip2px(context, Level);
        mVerticalInterval = DensityUtil.dip2px(context, vertical);
        mLabels = Lable;
        mContext = context;
    }


    public WaterMarkBgSingleLine(Context context, String Lable) {
        mLabels = Lable;
        mContext = context;
        mAngle = -20;
        mFontSize = 30;
        mPaintColor = "#dfE7E7E7";
        mTextBoldState = true;
        mLeftBadingDp = DensityUtil.dip2px(context, 36);
        mTopBadingDp = DensityUtil.dip2px(context, 73);
        mLevelInterval = DensityUtil.dip2px(context, 244);
        mVerticalInterval = DensityUtil.dip2px(context, 116);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int width = getBounds().right;
        int height = getBounds().bottom;
        canvas.drawColor(Color.parseColor("#ffffff"));
        mPaint.setColor(Color.parseColor(mPaintColor));
        mPaint.setAntiAlias(true);
        mPaint.setTextSize((float) sp2px(mContext, mFontSize));
        mPaint.setFakeBoldText(mTextBoldState);
        canvas.save();
        for (int positionY = mTopBadingDp; positionY <= height; positionY += mVerticalInterval) {

            for (float positionX = mLeftBadingDp; positionX < width; positionX += mLevelInterval) {
                canvas.rotate(mAngle, positionX, positionY);
                canvas.drawText(mLabels, positionX, positionY, mPaint);
                canvas.rotate(-1 * mAngle, positionX, positionY);
            }
        }
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }


    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
