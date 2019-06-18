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

package com.inspur.emmcloud.web.plugin.barcode.decoding;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.web.plugin.barcode.camera.CameraManager;
import com.inspur.emmcloud.web.plugin.barcode.camera.PlanarYUVLuminanceSource;
import com.inspur.emmcloud.web.plugin.barcode.scan.CaptureActivity;

import java.util.Hashtable;


final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final CaptureActivity activity;
    private final MultiFormatReader multiFormatReader;
    private QRCodeReader reader;
    private int scanCount = 0;
    private Hashtable<DecodeHintType, Object> hints;

    DecodeHandler(CaptureActivity activity, Hashtable<DecodeHintType, Object> hints) {
        this.hints = hints;
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        reader = new QRCodeReader();
        this.activity = activity;
        scanCount = 0;
    }

    /**
     * 转为二值图像
     *
     * @param bmp 原图bitmap
     * @return
     */
    public static Bitmap getBinaryBitmap(Bitmap bmp, int tmp) {
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
        // 设定二值化的域值，默认值为100
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int col = pixels[width * i + j];
                // 分离三原色
                int alpha = (col & 0xFF000000);
                int red = ((col & 0x00FF0000) >> 16);
                int green = ((col & 0x0000FF00) >> 8);
                int blue = (col & 0x000000FF);
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                int gray = (int) ((float) red * 0.2125 + (float) green * 0.7154 +
                        (float) blue * 0.0721);
                //对图像进行二值化处理
                if (gray <= tmp) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                // 新的ARGB
                pixels[width * i + j] = alpha | (gray << 16) | (gray << 8) | gray;
            }
        }
        // 新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        return newBmp;
    }

    @Override
    public void handleMessage(Message message) {
        if (Res.getWidgetID("web_decode") == message.what) {
            decode((byte[]) message.obj, message.arg1, message.arg2);
        } else if (Res.getWidgetID("web_quit") == message.what) {
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
        Result rawResult = null;
        byte[] rotatedData = rotateYUV420Degree90(data, width, height);
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
            sendDecodeSuccessHandlerMessage(rawResult);
        } else {
            Bitmap cropBitmap = source.getCropBitmap();
            if (scanCount > 7) {
                sendDecodeFailHandlerMessage(cropBitmap);
            } else {
                sendDecodeFailHandlerMessage(null);
            }
            rawResult = handleImgAndDecode(cropBitmap, 140);
            if (rawResult != null) {
                sendDecodeSuccessHandlerMessage(rawResult);
            } else {
                rawResult = handleImgAndDecode(cropBitmap, 160);
                if (rawResult != null) {
                    sendDecodeSuccessHandlerMessage(rawResult);
                } else {
                    rawResult = handleImgAndDecode(cropBitmap, 180);
                    if (rawResult != null) {
                        sendDecodeSuccessHandlerMessage(rawResult);
                    }
                }
            }

        }
    }

    private Result handleImgAndDecode(Bitmap cropBitmap, int tmp) {
        int cropBitmapHeight = cropBitmap.getHeight();
        int cropBitmapWidth = cropBitmap.getWidth();
        Bitmap handlerBitmap = getBinaryBitmap(cropBitmap, tmp);
        int[] bitmapPixels = new int[cropBitmapHeight * cropBitmapWidth];
        handlerBitmap.getPixels(bitmapPixels, 0, cropBitmapWidth, 0, 0, cropBitmapWidth, cropBitmapHeight);
        RGBLuminanceSource source = new RGBLuminanceSource(cropBitmapWidth, cropBitmapHeight, bitmapPixels);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result rawResult = null;
        try {
            rawResult = reader.decode(bitmap, hints);
        } catch (ReaderException re) {
            re.printStackTrace();
            // continue
        } finally {
            reader.reset();
        }
        return rawResult;
    }

    private void sendDecodeSuccessHandlerMessage(Result rawResult) {
        if (activity.getHandler() != null) {
            Message message = Message.obtain(activity.getHandler(), Res.getWidgetID("web_decode_succeeded"), rawResult);
            message.sendToTarget();
        }
    }

    private void sendDecodeFailHandlerMessage(Bitmap bitmap) {
        if (activity.getHandler() != null) {
            Message message = Message.obtain(activity.getHandler(), Res.getWidgetID("web_decode_failed"));
            if (bitmap != null) {
                message.obj = bitmap;
            }
            message.sendToTarget();
        }
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
}
