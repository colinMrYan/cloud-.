package com.inspur.emmcloud.application.widget;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

/**
 * Created by yufuchang on 2017/8/18.
 */

public class GradientDrawableBuilder {

    private int cornerRadius = 5;
    private int strokeWith = 1;
    private int strokeColor = Color.parseColor("#cccccc");
    private int backgroundColor = Color.parseColor("#eeeeee");

    public GradientDrawableBuilder setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        return this;
    }

    public GradientDrawableBuilder setStrokeWith(int strokeWith) {
        this.strokeWith = strokeWith;
        return this;
    }

    public GradientDrawableBuilder setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public GradientDrawableBuilder setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public GradientDrawable build() {
        GradientDrawable drawable = new GradientDrawable();
//        drawable.setCornerRadius(cornerRadius);
        drawable.setStroke(strokeWith, strokeColor);
        drawable.setGradientRadius(cornerRadius);
        drawable.setCornerRadius(cornerRadius);
        drawable.setUseLevel(false);
        drawable.setShape(GradientDrawable.RECTANGLE);
//        drawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        drawable.setColor(backgroundColor);
        return drawable;
    }
}
