package com.inspur.emmcloud.widget.roundbutton;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.inspur.emmcloud.R;

public class CustomRoundButtonDrawable extends GradientDrawable {
    private boolean mRadiusAdjustBounds = true;
    private ColorStateList mFillColors;
    private int mStrokeWidth = 0;
    private ColorStateList mStrokeColors;

    public CustomRoundButtonDrawable() {
    }

    public static CustomRoundButtonDrawable fromAttributeSet(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CusRoundButton, defStyleAttr, 0);
        ColorStateList colorBg = typedArray.getColorStateList(R.styleable.CusRoundButton_cus_backgroundColor);
        ColorStateList colorBorder = typedArray.getColorStateList(R.styleable.CusRoundButton_cus_borderColor);
        int borderWidth = typedArray.getDimensionPixelSize(R.styleable.CusRoundButton_cus_borderWidth, 0);
        boolean isRadiusAdjustBounds = typedArray.getBoolean(R.styleable.CusRoundButton_cus_isRadiusAdjustBounds, false);
        int mRadius = typedArray.getDimensionPixelSize(R.styleable.CusRoundButton_cus_radius, 0);
        int mRadiusTopLeft = typedArray.getDimensionPixelSize(R.styleable.CusRoundButton_cus_radiusTopLeft, 0);
        int mRadiusTopRight = typedArray.getDimensionPixelSize(R.styleable.CusRoundButton_cus_radiusTopRight, 0);
        int mRadiusBottomLeft = typedArray.getDimensionPixelSize(R.styleable.CusRoundButton_cus_radiusBottomLeft, 0);
        int mRadiusBottomRight = typedArray.getDimensionPixelSize(R.styleable.CusRoundButton_cus_radiusBottomRight, 0);
        typedArray.recycle();
        CustomRoundButtonDrawable bg = new CustomRoundButtonDrawable();
        bg.setBgData(colorBg);
        bg.setStrokeData(borderWidth, colorBorder);
        if (mRadiusTopLeft <= 0 && mRadiusTopRight <= 0 && mRadiusBottomLeft <= 0 && mRadiusBottomRight <= 0) {
            bg.setCornerRadius((float) mRadius);
            if (mRadius > 0) {
                isRadiusAdjustBounds = false;
            }
        } else {
            float[] radii = new float[]{(float) mRadiusTopLeft, (float) mRadiusTopLeft, (float) mRadiusTopRight, (float) mRadiusTopRight, (float) mRadiusBottomRight, (float) mRadiusBottomRight, (float) mRadiusBottomLeft, (float) mRadiusBottomLeft};
            bg.setCornerRadii(radii);
            isRadiusAdjustBounds = false;
        }

        bg.setIsRadiusAdjustBounds(isRadiusAdjustBounds);
        return bg;
    }

    public void setBgData(@Nullable ColorStateList colors) {
        if (this.hasNativeStateListAPI()) {
            super.setColor(colors);
        } else {
            this.mFillColors = colors;
            int currentColor;
            if (colors == null) {
                currentColor = 0;
            } else {
                currentColor = colors.getColorForState(this.getState(), 0);
            }

            this.setColor(currentColor);
        }

    }

    public void setStrokeData(int width, @Nullable ColorStateList colors) {
        if (this.hasNativeStateListAPI()) {
            super.setStroke(width, colors);
        } else {
            this.mStrokeWidth = width;
            this.mStrokeColors = colors;
            int currentColor;
            if (colors == null) {
                currentColor = 0;
            } else {
                currentColor = colors.getColorForState(this.getState(), 0);
            }

            this.setStroke(width, currentColor);
        }

    }

    private boolean hasNativeStateListAPI() {
        return Build.VERSION.SDK_INT >= 21;
    }

    public void setIsRadiusAdjustBounds(boolean isRadiusAdjustBounds) {
        this.mRadiusAdjustBounds = isRadiusAdjustBounds;
    }

    protected boolean onStateChange(int[] stateSet) {
        boolean superRet = super.onStateChange(stateSet);
        int color;
        if (this.mFillColors != null) {
            color = this.mFillColors.getColorForState(stateSet, 0);
            this.setColor(color);
            superRet = true;
        }

        if (this.mStrokeColors != null) {
            color = this.mStrokeColors.getColorForState(stateSet, 0);
            this.setStroke(this.mStrokeWidth, color);
            superRet = true;
        }

        return superRet;
    }

    public boolean isStateful() {
        return this.mFillColors != null && this.mFillColors.isStateful() || this.mStrokeColors != null && this.mStrokeColors.isStateful() || super.isStateful();
    }

    protected void onBoundsChange(Rect r) {
        super.onBoundsChange(r);
        if (this.mRadiusAdjustBounds) {
            this.setCornerRadius((float) (Math.min(r.width(), r.height()) / 2));
        }

    }
}
