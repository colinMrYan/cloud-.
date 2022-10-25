package com.inspur.emmcloud.ui.chat.emotion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

import com.amazonaws.mobile.auth.core.signin.ui.DisplayUtils;

import java.lang.ref.WeakReference;


class EmotionSpan extends DynamicDrawableSpan {

    private final Context mContext;

    private final int mResourceId;

    private final int mSize;

    private final int mTextSize;

    private int mHeight;

    private int mWidth;

    private int mTop;

    private Drawable mDrawable;

    private WeakReference<Drawable> mDrawableRef;

    // 手动偏移值
    private int mTranslateY = 0;
    private int mSelectColor = -1;
    private float mLineSpacing = -1;
    private boolean isLastLineSmile = false;

    public EmotionSpan(Context context, int resourceId, int size, int textSize) {
        super(DynamicDrawableSpan.ALIGN_BASELINE);
        mContext = context;
        mResourceId = resourceId;
        mWidth = mHeight = mSize = size;
        mTextSize = textSize;
    }

    public EmotionSpan(Context context, int resourceId, int size, int textSize, int selectColor, float lineSpacing, boolean isLastLineSmile) {
        super(DynamicDrawableSpan.ALIGN_BASELINE);
        mContext = context;
        mResourceId = resourceId;
        mWidth = mHeight = mSize = size;
        mTextSize = textSize;
        mSelectColor = selectColor;
        mLineSpacing = lineSpacing;
        this.isLastLineSmile = isLastLineSmile;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
//        return super.getSize(paint, text, start, end, fm);

        // fm 以 Paint 的 fm 为基准，避免 Span 改变了 fm 导致文字行高变化 -- chat
        Drawable d = getCachedDrawable();
        Rect rect = d.getBounds();

        if (fm != null) {
            Paint.FontMetricsInt pfm = paint.getFontMetricsInt();
            // keep it the same as paint's fm
            fm.ascent = pfm.ascent;
            fm.descent = pfm.descent;
            fm.top = pfm.top;
            fm.bottom = pfm.bottom;
        }

        return rect.right;
    }

    public Drawable getDrawable() {
        if (mDrawable == null) {
            try {
                mDrawable = mContext.getResources().getDrawable(mResourceId);
                if (mDrawable != null) {
                    mHeight = mSize;
                    mWidth = mHeight * mDrawable.getIntrinsicWidth() / mDrawable.getIntrinsicHeight();
                    mTop = (mTextSize - mHeight) / 2;
                    mDrawable.setBounds(0, mTop, mWidth, mTop + mHeight);
                }
            } catch (Exception e) {
                // swallow
            }
        }
        return mDrawable;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        //super.draw(canvas, text, start, end, x, top, y, bottom, paint);
        Drawable b = getCachedDrawable();
        int count = canvas.save();

//        int transY = fontMetricesBottom - b.getBounds().bottom;
//        int transY -= paint.getFontMetricsInt().descent;

        // 因为 TextView 加了 lineSpacing 之后会导致这里的 bottom、top 参数与单行情况不一样，所以不用 bottom、top，而使用 fontMetrics 的高度来计算
        int fontMetricesTop = y + paint.getFontMetricsInt().top;
        int fontMetricesBottom = fontMetricesTop + (paint.getFontMetricsInt().bottom - paint.getFontMetricsInt().top);
        int transY = fontMetricesTop + ((fontMetricesBottom - fontMetricesTop) / 2) - ((b.getBounds().bottom - b.getBounds().top) / 2) - mTop;

        //调整行间距
        if (!isLastLineSmile){
            transY += mTranslateY;
        }

        canvas.translate(x, transY);
        b.draw(canvas);

        //填充因lineSpacingMultiplier行间距所造成的 表情与文本选中后的高度空白。
        if (mSelectColor != -1 && mLineSpacing != -1) {
            if (!isLastLineSmile) {
                //选中颜色
                paint.setColor(mSelectColor);
                //填充高度 调整2倍像素高度
                paint.setStrokeWidth((float) (mHeight * (mLineSpacing - 1) - 2.8));
                //调整行间距
                canvas.drawLine(0, (float)(mHeight + mTranslateY * 1.4), mWidth, (float) (mHeight + mTranslateY * 1.4), paint);
            }
        }
        canvas.restoreToCount(count);
    }

    public Drawable getCachedDrawable() {
        if (mDrawableRef == null || mDrawableRef.get() == null) {
            mDrawableRef = new WeakReference<>(getDrawable());
        }
        return mDrawableRef.get();
    }

    public void setTranslateY(int translateY) {
        mTranslateY = translateY;
    }
}