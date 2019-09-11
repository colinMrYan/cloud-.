package com.inspur.emmcloud.baselib.util;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.baselib.R;
import com.inspur.emmcloud.baselib.util.toast.CusToastUtils;
import com.inspur.emmcloud.baselib.util.toast.style.ToastBlackStyle;

/**
 * CusToastUtils
 *
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-12-9
 */
public class ToastUtils {

    public static void init(Application application) {
        CusToastUtils.init(application, new ToastBlackStyle());
        CusToastUtils.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, DensityUtil.dip2px(50));
        CusToastUtils.setView(createTextView(application));
    }

    private ToastUtils() {
        throw new AssertionError();
    }

    public static void show(int resId){
        CusToastUtils.show(resId);
    }

    public static void show(String text){
        CusToastUtils.show(text);
    }

    public static void show(Context context, int resId) {
        show(context, context.getResources().getText(resId), Toast.LENGTH_SHORT);
    }

    public static void show(Context context, int resId, int duration) {
        show(context, context.getResources().getText(resId), duration);
    }

    public static void show(Context context, CharSequence text) {
        show(context, text, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, CharSequence text, int duration) {
//        Toast.makeText(context, text, duration).show();
        CusToastUtils.show(text);
    }

    public static void show(Context context, int resId, Object... args) {
        show(context, String.format(context.getResources().getString(resId), args), Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String format, Object... args) {
        show(context, String.format(format, args), Toast.LENGTH_SHORT);
    }

    public static void show(Context context, int resId, int duration, Object... args) {
        show(context, String.format(context.getResources().getString(resId), args), duration);
    }

    public static void show(Context context, String format, int duration, Object... args) {
        show(context, String.format(format, args), duration);
    }

    /**
     * 设置统一的提示TextView
     */
    public static TextView createTextView(Context context) {
        GradientDrawable drawable = new GradientDrawable();
        // 设置背景色
        drawable.setColor(0x88000000);
        // 设置圆角大小
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DensityUtil.dip2px(2), context.getResources().getDisplayMetrics()));
        TextView textView = new TextView(context);
        textView.setId(android.R.id.message);
        textView.setTextColor(context.getResources().getColor(R.color.white));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, context.getResources().getDisplayMetrics()));
        textView.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DensityUtil.dip2px(5), context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DensityUtil.dip2px(2), context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DensityUtil.dip2px(5), context.getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DensityUtil.dip2px(2), context.getResources().getDisplayMetrics()));
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // setBackground API 版本兼容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setBackground(drawable);
        } else {
            textView.setBackgroundDrawable(drawable);
        }
        // 设置 Z 轴阴影
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setZ(DensityUtil.dip2px(10));
        }
        textView.setMaxLines(3);
        return textView;
    }
}
