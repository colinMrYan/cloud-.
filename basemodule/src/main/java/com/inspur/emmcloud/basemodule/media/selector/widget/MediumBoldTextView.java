package com.inspur.emmcloud.basemodule.media.selector.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.inspur.emmcloud.basemodule.R;

/**
 * @author：luck
 * @date：2020/8/25 10:32 AM
 * @describe：MediumBoldTextView
 */
public class MediumBoldTextView extends AppCompatTextView {
    // 字体加宽不需要，去掉
    private float mStrokeWidth = 0.0F;

    public MediumBoldTextView(Context context) {
        this(context, null);
    }

    public MediumBoldTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediumBoldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PictureMediumBoldTextView, defStyleAttr, 0);
//        mStrokeWidth = a.getFloat(R.styleable.PictureMediumBoldTextView_stroke_Width, mStrokeWidth);
//        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        TextPaint paint = getPaint();
//        if (paint.getStrokeWidth() != mStrokeWidth) {
//            paint.setStrokeWidth(mStrokeWidth);
//            paint.setStyle(Paint.Style.FILL_AND_STROKE);
//        }
        super.onDraw(canvas);
    }

    public void setStrokeWidth(float mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
        invalidate();
    }
}
