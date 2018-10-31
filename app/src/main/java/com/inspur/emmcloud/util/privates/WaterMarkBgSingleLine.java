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

import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.common.LogUtils;

/**
 * Created by libaochao on 2018/10/31.
 */

public class WaterMarkBgSingleLine extends Drawable {

    private Paint mPaint = new Paint();
    private String mLabels;
    private Context mContext;
    private int mAngle;//角度
    private int mFontSize;//字体大小 单位sp
    private String mPaintColor ="#efefef";
    private Boolean mTextBoldState =true;
    private int mHighSizes =5;
    private int  mWidthSize =100;
    private int  mLeftBadingDp =10;
    private int  mTopBadingDp  =10;
    private int  mLevelInterval =50;
    private int  mVerticalInterval =50;

    public WaterMarkBgSingleLine(Context context, int Angle , int  FontSize, String  PaintColor , Boolean TextBoldState, int LeftBadingDp, int TopBadingDp, int Level, int vertical, String Lable){
        mAngle  = Angle;
        mFontSize =FontSize;
        mPaintColor =PaintColor;
        mTextBoldState =TextBoldState;
        mLeftBadingDp  = DensityUtil.dip2px(context,LeftBadingDp);
        mTopBadingDp  =  DensityUtil.dip2px(context,TopBadingDp) ;
        mLevelInterval = DensityUtil.dip2px(context,Level);
        mVerticalInterval = DensityUtil.dip2px(context,vertical);
        mLabels = Lable;
        mContext =context;
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
                    canvas.rotate(mAngle,positionX,positionY);
                    canvas.drawText(mLabels,positionX, positionY, mPaint);
                    canvas.rotate(-1*mAngle,positionX,positionY);
                LogUtils.LbcDebug("!!!!!!!!!!!!!!!!!!!!!");
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


    public  int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
