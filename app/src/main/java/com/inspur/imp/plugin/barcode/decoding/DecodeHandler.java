/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inspur.imp.plugin.barcode.decoding;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.inspur.imp.api.Res;
import com.inspur.imp.plugin.barcode.camera.CameraManager;
import com.inspur.imp.plugin.barcode.camera.PlanarYUVLuminanceSource;
import com.inspur.imp.plugin.barcode.scan.CaptureActivity;

import java.util.Hashtable;


final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final CaptureActivity activity;
    private final MultiFormatReader multiFormatReader;
    private int scanCount=0;

    DecodeHandler(CaptureActivity activity, Hashtable<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.activity = activity;
        scanCount =0;
    }

    @Override
    public void handleMessage(Message message) {
        if (Res.getWidgetID("decode") == message.what) {
            decode((byte[]) message.obj, message.arg1, message.arg2);
        } else if (Res.getWidgetID("quit") == message.what) {
            Looper.myLooper().quit();
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        scanCount++;
        long start = System.currentTimeMillis();
        Result rawResult = null;
//        byte[] rotatedData = new byte[data.length];
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++)
//                rotatedData[x * height + height - y - 1] = data[x + y * width];
//        }
        byte[] rotatedData = rotateYUV420Degree90(data,width,height);
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;

        PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, width, height);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = multiFormatReader.decodeWithState(bitmap);
        } catch (ReaderException re) {
            // continue
        } finally {
            multiFormatReader.reset();
        }
        if (rawResult != null) {
            long end = System.currentTimeMillis();
            Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n" + rawResult.toString());
            Message message = Message.obtain(activity.getHandler(), Res.getWidgetID("decode_succeeded"), rawResult);
            message.sendToTarget();
        } else {
            if (scanCount >25){
                Bitmap cropBitmap = source.getCropBitmap();
                Bitmap saturationBitmap = getBinaryBitmap(setSaturation(cropBitmap,2.0f),120);
                int saturationBitmapWidth = saturationBitmap.getWidth(); // 获取位图的宽
                int saturationBitmapHeight = saturationBitmap.getHeight(); // 获取位图的高
                int[] saturationBitmapPixels = new int[saturationBitmapWidth * saturationBitmapHeight];
                saturationBitmap.getPixels(saturationBitmapPixels, 0, saturationBitmapWidth, 0, 0, saturationBitmapWidth, saturationBitmapHeight);
                RGBLuminanceSource source2 = new RGBLuminanceSource(saturationBitmapWidth, saturationBitmapHeight, saturationBitmapPixels);
                BinaryBitmap bitmap2 = new BinaryBitmap(new HybridBinarizer(source2));
                QRCodeReader reader = new QRCodeReader();
                try {
                    rawResult = reader.decode(bitmap2);
                } catch (ReaderException re) {
                    re.printStackTrace();
                    // continue
                } finally {
                    reader.reset();
                }
                if (rawResult != null){
                    Message message = Message.obtain(activity.getHandler(), Res.getWidgetID("decode_succeeded"), rawResult);
                    message.sendToTarget();
                }else {
                    Message message = Message.obtain(activity.getHandler(), Res.getWidgetID("decode_failed"));
                    message.obj = cropBitmap;
                    message.sendToTarget();
                }

            }else {
                Message message = Message.obtain(activity.getHandler(), Res.getWidgetID("decode_failed"));
                message.sendToTarget();
            }
        }
    }

    /**
     * 改变图片饱和度
     * @param srcBitmap
     * @param saturation
     * @return
     */
    public static Bitmap setSaturation(Bitmap srcBitmap, float saturation) {

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(saturation);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        //显示图片
        Matrix matrix = new Matrix();
        Bitmap createBmp = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), srcBitmap.getConfig());
        Canvas canvas = new Canvas(createBmp);
        canvas.drawBitmap(srcBitmap, matrix, paint);

        return createBmp;
    }


    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }


    /**
     * 转为二值图像
     *
     * @param bmp
     *            原图bitmap
     * @return
     */
    public static Bitmap getBinaryBitmap(Bitmap bmp,int tmp) {
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int w = width;
        int h = height;
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
        // 设定二值化的域值，默认值为100
        //tmp = 180;
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                // 分离三原色
                alpha = ((grey & 0xFF000000) >> 24);
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                if (red > tmp) {
                    red = 255;
                } else {
                    red = 0;
                }
                if (blue > tmp) {
                    blue = 255;
                } else {
                    blue = 0;
                }
                if (green > tmp) {
                    green = 255;
                } else {
                    green = 0;
                }
                pixels[width * i + j] = alpha << 24 | red << 16 | green << 8
                        | blue;
                if (pixels[width * i + j] == -1) {
                    pixels[width * i + j] = -1;
                } else {
                    pixels[width * i + j] = -16777216;
                }
            }
        }
        // 新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, w, h);
        return resizeBmp;
    }
}
