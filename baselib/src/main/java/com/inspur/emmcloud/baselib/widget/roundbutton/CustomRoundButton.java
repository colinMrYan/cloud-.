package com.inspur.emmcloud.baselib.widget.roundbutton;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;

import com.inspur.baselib.R;


public class CustomRoundButton extends AppCompatButton {
    public CustomRoundButton(Context context) {
        super(context);
        this.init(context, (AttributeSet) null, 0);
    }

    public CustomRoundButton(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.CustomButtonStyle);
        this.init(context, attrs, R.attr.CustomButtonStyle);
    }

    public CustomRoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        CustomRoundButtonDrawable bg = CustomRoundButtonDrawable.fromAttributeSet(context, attrs, defStyleAttr);
        setBackgroundKeepingPadding(this, bg);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBackgroundKeepingPadding(View view, Drawable drawable) {
        int[] padding = new int[]{view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom()};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }

        view.setPadding(padding[0], padding[1], padding[2], padding[3]);
    }
}
