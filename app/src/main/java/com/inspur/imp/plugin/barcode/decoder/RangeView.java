/*
 * Copyright (C) 2008 ZXing authors
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

package com.inspur.imp.plugin.barcode.decoder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.inspur.emmcloud.basemodule.util.Res;

import java.util.ArrayList;
import java.util.List;


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class RangeView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192,
            128, 64};
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final long ANIMATION_DELAY = 80L;
    private final Paint paint;
    private final int maskColor;
    private final int resultColor;
    private Bitmap resultBitmap;
    private int scannerAlpha;
    private List<ResultPoint> possibleResultPoints;

    private int i = 0;
    private Rect mRect;
    private Rect frame;
    private GradientDrawable mDrawable;
    private Drawable lineDrawable;
    private float density;
    private Context context;

    public RangeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(Res.getColorID("viewfinder_mask"));
        resultColor = resources.getColor(Res.getColorID("result_view"));

        mRect = new Rect();
        int left = getResources().getColor(Res.getColorID("lightgreen"));
        int center = getResources().getColor(Res.getColorID("green"));
        int right = getResources().getColor(Res.getColorID("lightgreen"));
        lineDrawable = getResources().getDrawable(Res.getDrawableID("icon_zx_code_line"));
        mDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[]{left,
                left, center, right, right});
        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<ResultPoint>(5);
    }

    public void setRange(Rect frame) {
        this.frame = frame;
    }


    @Override
    public void onDraw(Canvas canvas) {
        if (frame == null) {
            return;
        }

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
        canvas.drawRect(0, frame.bottom, width, height, paint);

        if (resultBitmap != null) {
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            paint.setColor(getResources().getColor(Res.getColorID("green")));
            canvas.drawRect(frame.left - 7, frame.top - 7, frame.left + 33,
                    frame.top, paint);
            canvas.drawRect(frame.left - 7, frame.top - 7, frame.left,
                    frame.top + 33, paint);
            canvas.drawRect(frame.right - 33, frame.top - 7, frame.right + 7,
                    frame.top, paint);
            canvas.drawRect(frame.right, frame.top - 7, frame.right + 7,
                    frame.top + 33, paint);
            canvas.drawRect(frame.left - 7, frame.bottom, frame.left + 33,
                    frame.bottom + 7, paint);
            canvas.drawRect(frame.left - 7, frame.bottom - 33, frame.left,
                    frame.bottom + 7, paint);
            canvas.drawRect(frame.right - 33, frame.bottom, frame.right + 7,
                    frame.bottom + 7, paint);
            canvas.drawRect(frame.right, frame.bottom - 33, frame.right + 7,
                    frame.bottom + 7, paint);


            paint.setColor(getResources().getColor(Res.getColorID("green")));
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            if ((i += 5) < frame.bottom - frame.top) {
                mRect.set(frame.left - 6, frame.top + i - 6, frame.right + 6,
                        frame.top + 6 + i);
                lineDrawable.setBounds(mRect);
                lineDrawable.draw(canvas);
                invalidate();
            } else {
                i = 0;
            }

            float textCenterX = 0.0f;
            float textBaselineY = 0.0f;
            density = context.getResources().getDisplayMetrics().density;
            paint.setColor(Color.WHITE);
            paint.setTextSize(15 * density);
            paint.setTypeface(Typeface.create("System", Typeface.NORMAL));
            FontMetrics fm = paint.getFontMetrics();
            float textWidth = paint.measureText(getResources().getString(Res.getStringID("put_qrcode_in_frame")));
            double textheight = Math.ceil(fm.descent - fm.ascent);
            textCenterX = (width - textWidth) / 2;
            canvas.drawText(getResources().getString(Res.getStringID("put_qrcode_in_frame")), textCenterX,
                    frame.bottom + (float) 30 * density, paint);
            textWidth = paint.measureText(getResources().getString(Res.getStringID("can_auto_scan")));
            textCenterX = (width - textWidth) / 2;
            canvas.drawText(getResources().getString(Res.getStringID("can_auto_scan")), textCenterX,
                    (float) (frame.bottom + (float) 30 * density + textheight), paint);


            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
                    frame.right, frame.bottom);
        }
    }


    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

    public void recycleLineDrawable() {
        if (mDrawable != null) {
            mDrawable.setCallback(null);
        }
        if (lineDrawable != null) {
            lineDrawable.setCallback(null);
        }
    }
}
