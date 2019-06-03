/*
 * Copyright 2009 ZXing authors
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

package com.inspur.imp.plugin.barcode.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.google.zxing.LuminanceSource;
import com.inspur.emmcloud.baselib.util.LogUtils;

import java.io.ByteArrayOutputStream;

/**
 * This object extends LuminanceSource around an array of YUV data returned from the camera driver,
 * with the option to crop to a rectangle within the full data. This can be used to exclude
 * superfluous pixels around the perimeter and speed up decoding.
 * <p>
 * It works for any pixel format where the Y channel is planar and appears first, including
 * YCbCr_420_SP and YCbCr_422_SP.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class PlanarYUVLuminanceSource extends LuminanceSource {
    private final byte[] yuvData;
    private final int dataWidth;
    private final int dataHeight;
    private final int left;
    private final int top;

    public PlanarYUVLuminanceSource(byte[] yuvData, int dataWidth, int dataHeight, int left, int top,
                                    int width, int height) {
        super(width, height);

//    if (left + width > dataWidth || top + height > dataHeight) {
//      throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
//    }

        this.yuvData = yuvData;
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        this.left = left;
        this.top = top;
    }


    @Override
    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        int offset = (y + top) * dataWidth + left;
        System.arraycopy(yuvData, offset, row, 0, width);
        return row;
    }

    @Override
    public byte[] getMatrix() {
        int width = getWidth();
        int height = getHeight();

        // If the caller asks for the entire underlying image, save the copy and give them the
        // original data. The docs specifically warn that result.length must be ignored.
        if (width == dataWidth && height == dataHeight) {
            return yuvData;
        }

        int area = width * height;
        byte[] matrix = new byte[area];
        int inputOffset = top * dataWidth + left;

        // If the width matches the full width of the underlying data, perform a single copy.
        if (width == dataWidth) {
            System.arraycopy(yuvData, inputOffset, matrix, 0, area);
            return matrix;
        }

        // Otherwise copy one cropped row at a time.
        byte[] yuv = yuvData;
        for (int y = 0; y < height; y++) {
            int outputOffset = y * width;
            System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
            inputOffset += dataWidth;
        }
        return matrix;
    }

    public byte[] getCropMatrix() {
        int mWidth = dataWidth;
        int mHeight = dataHeight;
        LogUtils.jasonDebug("mWidth=" + mWidth);
        LogUtils.jasonDebug("mHeight=" + mHeight);
        int mYLenght = mWidth * mHeight;
        int mCropHeight = getHeight();
        int mCropWidth = getWidth();

        LogUtils.jasonDebug("mCropHeight=" + mCropHeight);
        LogUtils.jasonDebug("mCropWidth=" + mCropWidth);

        int mCropLeft = left;
        int mCropTop = top;
        byte[] mData = new byte[mCropHeight * mCropWidth * 3 / 2];


        int index = 0;
        int start = mCropLeft + mCropTop * mWidth;
        int end = mWidth * (mCropTop + mCropHeight);
        int oriStep = mWidth;
        int newstep = mCropWidth;
        //copy y;
        for (int i = start; i < end; i += oriStep) {
            System.arraycopy(yuvData, i, mData, index, newstep);
            index += newstep;
        }
        //copy u
        start = mYLenght + (mWidth * mCropTop / 4 + mCropLeft / 2);
        end = mYLenght + mWidth * (mCropTop + mCropHeight) / 4;
        oriStep = mWidth / 2;
        newstep = mCropWidth / 2;
        for (int i = start; i < end; i += oriStep) {
            System.arraycopy(yuvData, i, mData, index, newstep);
            index += newstep;
        }
        //copy v
        start = mYLenght / 4 * 5 + mWidth * mCropTop / 4 + mCropLeft / 2;
        end = mYLenght / 4 * 5 + mWidth * (mCropTop + mCropHeight) / 4;
        for (int i = start; i < end; i += oriStep) {
            System.arraycopy(yuvData, i, mData, index, newstep);
            index += newstep;
        }
        return mData;
    }


    /**
     * 对于高通或者MTK平台，只支持宽高为16的整数备的数据
     *
     * @param size
     * @param limit
     * @return
     */
    private int roundTo16(int size, int limit) {
        if (size >= limit) {
            return limit;
        }
        float f = size / 16f;
        int m;
        if (f - (int) f > 0.5f) {
            m = ((int) (f + 0.5)) * 16;
        } else {
            m = (int) f * 16;
        }
        return m < 16 ? 16 : m;
    }

    @Override
    public boolean isCropSupported() {
        return true;
    }

    public int getDataWidth() {
        return dataWidth;
    }

    public int getDataHeight() {
        return dataHeight;
    }

    public Bitmap renderCroppedGreyscaleBitmap() {
        int width = getWidth();
        int height = getHeight();
        int[] pixels = new int[width * height];
        byte[] yuv = yuvData;
        int inputOffset = top * dataWidth + left;

        for (int y = 0; y < height; y++) {
            int outputOffset = y * width;
            for (int x = 0; x < width; x++) {
                int grey = yuv[inputOffset + x] & 0xff;
                pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
            }
            inputOffset += dataWidth;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public Bitmap getCropBitmap() {
        Bitmap barcode = null;
        Bitmap originBitmap = byte2Bitmap(yuvData, dataWidth, dataHeight);
        barcode = Bitmap.createBitmap(originBitmap, left, top, getWidth(), getHeight());

        return barcode;
    }


    public Bitmap byte2Bitmap(byte[] data, int width, int height) {
        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, width, height, null);
            if (image != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, width, height), 90, stream);

                Bitmap originBitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                stream.close();
                return originBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}
