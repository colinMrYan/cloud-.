package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by libaochao on 2018/10/23.
 */

public class WaterMarkBg extends Drawable {
    private Paint mPaint = new Paint();
    private List<String> mLabels;
    private Context mContext;
    private int mAngle;//角度
    private int mFontSize;//字体大小 单位sp
    private String mPaintColor ="#efefef";
    private Boolean mTextBoldState =true;
    private int mHighSizes =5;
    private int  mWidthSize =100;
    private int mLeftBadingDp =10;


    /**
     * 初始化构造
     * @param context 上下文
     * @param labels 水印文字列表 多行显示支持
     * @param Angle 水印角度
     * @param fontSize 水印文字大小
     * @param leftBadingDp 左侧边距
     */
    public WaterMarkBg(Context context, List<String> labels, int Angle, int fontSize,int leftBadingDp) {
        this.mLabels = labels;
        this.mContext = context;
        this.mAngle = Angle;
        this.mFontSize = fontSize;
        this.mLeftBadingDp = leftBadingDp;
    }

    /**
     * 初始化构造
     * @param context 上下文
     * @param labels 水印文字列表 多行显示支持
     * @param Angle 水印角度
     * @param fontSize 水印文字大
     * @param leftBadingDp 左侧边距
     * @param PintColor 字体颜色
     */
    public WaterMarkBg(Context context, List<String> labels, int Angle, int fontSize,int leftBadingDp,String PintColor) {
        this.mLabels = labels;
        this.mContext = context;
        this.mAngle = Angle;
        this.mFontSize = fontSize;
        this.mLeftBadingDp = leftBadingDp;
        this.mPaintColor = PintColor;
    }

    /**
     * 初始化构造
     * @param context 上下文
     * @param labels 水印文字列表 多行显示支持
     * @param Angle 水印角度
     * @param fontSize 水印文字大小
     * @param PintColor 字体颜色
     */
    public WaterMarkBg(Context context, List<String> labels, int Angle, int fontSize,String PintColor,boolean TextBoldState) {
        this.mLabels = labels;
        this.mContext = context;
        this.mAngle = Angle;
        this.mFontSize = fontSize;
        this.mPaintColor = PintColor;
        this.mTextBoldState =TextBoldState;
    }

    /**
     * 初始化构造
     * @param context 上下文
     * @param labels 水印文字列表 多行显示支持
     * @param Angle 水印角度
     * @param fontSize 水印文字大小
     * @param PintColor 字体颜色
     * @param TextBoldState  字体是否加粗默认是加粗
     * @param HighSizes  屏幕垂直方向分成份数
     * @param WidethSizes 屏幕水平分为几分 （设置无效）
     */
    public WaterMarkBg(Context context, List<String> labels, int Angle, int fontSize,String PintColor,boolean TextBoldState,int HighSizes,int WidethSizes) {
        this.mLabels = labels;
        this.mContext = context;
        this.mAngle = Angle;
        this.mFontSize = fontSize;
        this.mPaintColor = PintColor;
        this.mTextBoldState =TextBoldState;
        this.mHighSizes = HighSizes;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        int width = getBounds().right;
        int height = getBounds().bottom;
        canvas.drawColor(Color.parseColor("#ffffff"));
        mPaint.setColor(Color.parseColor(mPaintColor));
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(sp2px(mContext, mFontSize));
        mPaint.setFakeBoldText(mTextBoldState);
        canvas.save();
        canvas.rotate(mAngle);
        float textWidth = mPaint.measureText(mLabels.get(0));
        int index = 0;
        for (int positionY = height / mHighSizes+sp2px(mContext, mLeftBadingDp); positionY <= height; positionY += height /mHighSizes) {
            float fromX = -width + (index++ % 2) * textWidth;
            for (float positionX = fromX; positionX < width; positionX += (textWidth * 2)) {
                int spacing  =0;//间距
                for(String label:mLabels){
                    canvas.drawText(label, positionX , positionY +spacing, mPaint);
                    spacing = spacing+50;
                }
            }
        }
        canvas.restore();
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
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
