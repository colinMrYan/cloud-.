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
    private Paint paint = new Paint();
    private List<String> labels;
    private Context context;
    private int degress;//角度
    private int fontSize;//字体大小 单位sp
    private String paintColor ="#efefef";
    private Boolean textBoldState =true;
    private int     highSizes =5;
    private int     widthSize =100;

    /**
     * 初始化构造
     * @param context 上下文
     * @param labels 水印文字列表 多行显示支持
     * @param degress 水印角度
     * @param fontSize 水印文字大小
     */
    public WaterMarkBg(Context context, List<String> labels, int degress, int fontSize) {
        this.labels = labels;
        this.context = context;
        this.degress = degress;
        this.fontSize = fontSize;
    }

    /**
     * 初始化构造
     * @param context 上下文
     * @param labels 水印文字列表 多行显示支持
     * @param degress 水印角度
     * @param fontSize 水印文字大小
     * @param PintColor 字体颜色
     */
    public WaterMarkBg(Context context, List<String> labels, int degress, int fontSize,String PintColor) {
        this.labels = labels;
        this.context = context;
        this.degress = degress;
        this.fontSize = fontSize;
        this.paintColor = PintColor;
    }

    /**
     * 初始化构造
     * @param context 上下文
     * @param labels 水印文字列表 多行显示支持
     * @param degress 水印角度
     * @param fontSize 水印文字大小
     * @param PintColor 字体颜色
     */
    public WaterMarkBg(Context context, List<String> labels, int degress, int fontSize,String PintColor,boolean TextBoldState) {
        this.labels = labels;
        this.context = context;
        this.degress = degress;
        this.fontSize = fontSize;
        this.paintColor = PintColor;
        this.textBoldState =TextBoldState;
    }

    /**
     * 初始化构造
     * @param context 上下文
     * @param labels 水印文字列表 多行显示支持
     * @param degress 水印角度
     * @param fontSize 水印文字大小
     * @param PintColor 字体颜色
     * @param TextBoldState  字体是否加粗默认是加粗
     * @param HighSizes  屏幕垂直方向分成份数
     * @param WidethSizes 屏幕水平分为几分 （设置无效）
     */
    public WaterMarkBg(Context context, List<String> labels, int degress, int fontSize,String PintColor,boolean TextBoldState,int HighSizes,int WidethSizes) {
        this.labels = labels;
        this.context = context;
        this.degress = degress;
        this.fontSize = fontSize;
        this.paintColor = PintColor;
        this.textBoldState =TextBoldState;
        this.highSizes = HighSizes;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        int width = getBounds().right;
        int height = getBounds().bottom;

        canvas.drawColor(Color.parseColor("#ffffff"));
        paint.setColor(Color.parseColor(paintColor));
        paint.setAntiAlias(true);
        paint.setTextSize(sp2px(context,fontSize));
        paint.setFakeBoldText(textBoldState);

        canvas.save();
        canvas.rotate(degress);
        float textWidth = paint.measureText(labels.get(0));
        int index = 0;
        for (int positionY = height / highSizes; positionY <= height; positionY += height /highSizes+80) {
            float fromX = -width + (index++ % 2) * textWidth;
            for (float positionX = fromX; positionX < width; positionX += textWidth * 2) {
                int spacing  =0;//间距
                for(String label:labels){
                    canvas.drawText(label, positionX, positionY+spacing, paint);
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
