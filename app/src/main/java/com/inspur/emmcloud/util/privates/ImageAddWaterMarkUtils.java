package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;

import com.inspur.emmcloud.util.common.LogUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static com.inspur.emmcloud.util.common.DensityUtil.sp2px;

/**
 * Created by libaochao on 2018/10/25.
 */

public class ImageAddWaterMarkUtils {

    static   int     mFontSize =23;
    static   String  mColor    = "#ff0000";
    static   float   mDegree   = -35;
    static   int     mHighSizes=  10;
    static   boolean mRecycle  = true;
    static   boolean mTextBoldState = true;
    static   int     mAlpha    = 80;

    /**
     * @param FrontSize 字体大小
     * @param Alpha     透明度(0~225)越小越颜色越浅
     * @param TextBoldState  字体是否加粗 true为加粗 默认为粗
     * @param Color     字体颜色 默认 #dfdfdf
     * @param Degree    水印旋转角度 默认0
     * @param HeighSizes  垂直方向文本个数
     * @param Recycle     默认为true
     * */
    public  void  setWaterMarkAttribute(int FrontSize,String Color, float Degree, int HeighSizes, Boolean Recycle,Boolean TextBoldState,int  Alpha ) {
        mFontSize =FrontSize;
        mColor    = Color;
        mDegree   = Degree;
        mHighSizes=  HeighSizes;
        mRecycle  = Recycle;
        mTextBoldState =TextBoldState;
        mAlpha    = Alpha;
    }

    public static void merge(Context mContext, final String path, final List<String> Lables) {

        final Context context  = mContext;
        final List<String> lables  =Lables;
        new Thread(new Runnable() {
            @Override
            public void run() {
                File pictureFile = new File(path);
                try {
                    Bitmap bitMapOrg = BitmapFactory.decodeStream(new FileInputStream(pictureFile));
                    //中间高度位置添加水印文字。
                    Bitmap bitMap = addTextWatermark(context,bitMapOrg,lables,mFontSize,mColor,mDegree,mHighSizes,mRecycle,mTextBoldState,mAlpha);
                    save(bitMap, pictureFile, Bitmap.CompressFormat.PNG, true);
                    // 其次把文件插入到系统图库
                    try {
                        MediaStore.Images.Media.insertImage(context.getContentResolver(),
                                pictureFile.getAbsolutePath(), pictureFile.getName(), null);
                        // 最后通知图库更新
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 给一张Bitmap添加水印文字。
     *
     * @param src      源图片
     * @param context  上下文
     * @param labels  水印文本
     * @param fontSize 水印字体大小 ，单位pix。
     * @param color    水印字体颜色 "#efefef"。
     * @param HighSizes 屏幕内垂直方向文本个数 int
     * @param Alpha     不透明度（0~255）
     * @param textBoldState
     * @param Degree   旋转角度
     * @param recycle  是否回收
     * @return 已经添加水印后的Bitmap。
     */
    public static   Bitmap addTextWatermark(Context context ,Bitmap src, List<String> labels, int fontSize, String color, float Degree ,int  HighSizes,boolean recycle,boolean textBoldState,int Alpha) {
        if (isEmptyBitmap(src) ||labels == null||labels.size()==0) {
            return null;
        }
        int   width = 0;
        int   height =0;
        float degree =Degree;

        Bitmap ret = src.copy(src.getConfig(), true);
        height =  ret.getHeight();
        width  =  ret.getWidth();
        Canvas canvas = new Canvas(ret);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor(color));
        paint.setAntiAlias(true);
        paint.setAlpha(Alpha);
        paint.setTextSize(sp2px(context,fontSize));
        paint.setFakeBoldText(textBoldState);

        canvas.save();
        canvas.rotate(degree);
        float textWidth = paint.measureText(labels.get(0));
        int index = 0;
        for (int positionY = height / HighSizes; positionY <= height; positionY += height /HighSizes+80) {
            float fromX = -width + (index++ % 2) * textWidth;
            for (float positionX = fromX; positionX < width; positionX += textWidth * 2) {
                int spacing  =0;//间距
                for(String label:labels){
                    canvas.drawText(label, positionX, positionY+spacing, paint);
                    spacing = spacing+50;
                }
            }
        }
        if (recycle && !src.isRecycled()) {
            src.recycle();
        }
        return ret;
    }

    /**
     * 保存图片到文件File。
     *
     * @param src     源图片
     * @param file    要保存到的文件
     * @param format  格式
     * @param recycle 是否回收
     * @return true 成功 false 失败
     */
    public  static   boolean save(Bitmap src, File file, Bitmap.CompressFormat format, boolean recycle) {
        if (isEmptyBitmap(src))
            return false;

        OutputStream os;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ret = src.compress(format, 100, os);
            if (recycle && !src.isRecycled())
                src.recycle();
        } catch (IOException e) {
            LogUtils.LbcDebug("保存失败");
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Bitmap对象是否为空。
     */
    public  static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }


}
