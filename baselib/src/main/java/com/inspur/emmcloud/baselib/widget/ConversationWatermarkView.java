package com.inspur.emmcloud.baselib.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class ConversationWatermarkView extends View {

    private TextPaint mTextPaint = new TextPaint();
    private String[] mText;
    private int mDegrees = 35;
    private int mTextColor = Color.parseColor("#66666666");
    private int mTextSize = 70;
    private int mDx = 100;
    private int mDy = 130;
    private int textWidth, textHeight;

    public ConversationWatermarkView(Context context) {
        super(context, null);
    }

    public ConversationWatermarkView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mText == null || mText.length <= 0) {
            return;
        }
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredWidth == 0 || measuredHeight == 0) {
            return;
        }
        int canvasLength = Math.max(measuredWidth, measuredHeight);
        canvas.save();
        canvas.rotate(mDegrees, measuredWidth >> 1, measuredHeight >> 1);

        canvas.save();
        int y = 0;
        boolean odd = true;

        while (y < canvasLength + textHeight) {
            int x = odd ? -(textWidth + mDx) : -(textWidth + mDx) / 2 * 3;
            while (x < canvasLength + textWidth) {
                drawTexts(mText, mTextPaint, canvas, x, y);
                x = x + textWidth + mDx;
            }
            y = y + textHeight + mDy;
            odd = !odd;
        }
        canvas.restore();
    }


    /**
     * 设置水印文字内容
     *
     * @param text 文字内容
     */
    public void setText(String... text) {
        mText = text;
        textWidth = 0;
        textHeight = 0;
        if (mText != null && mText.length > 0) {
            for (String s : mText) {
                Rect tvRect = new Rect();
                mTextPaint.getTextBounds(s, 0, s.length(), tvRect);
                textWidth = Math.max(textWidth, tvRect.width());
                textHeight += (tvRect.height() + 10);
            }
        }
        postInvalidate();
    }

    private void drawTexts(String[] texts, Paint paint, Canvas canvas, int x, int y) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        int length = texts.length;
        float total = (length - 1) * (bottom - top) + (fontMetrics.descent - fontMetrics.ascent);
        float offset = total / 2 - bottom;
        for (int i = 0; i < length; i++) {
            float yAxis = -(length - i - 1) * (bottom - top) + offset;
            canvas.drawText(texts[i], x, y + yAxis + 10, paint);
        }
    }


    public void setDegrees(int mDegrees) {
        this.mDegrees = mDegrees;
    }

    public void setTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

    public void setTextSize(int mTextSize) {
        this.mTextSize = mTextSize;
    }

    public void setDx(int mDx) {
        this.mDx = mDx;
    }

    public void setDy(int mDy) {
        this.mDy = mDy;
    }
}
