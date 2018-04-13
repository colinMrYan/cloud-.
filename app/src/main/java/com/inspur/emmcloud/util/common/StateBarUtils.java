package com.inspur.emmcloud.util.common;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import com.inspur.emmcloud.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class StateBarUtils {

    /**
     * 求改状态栏颜色为默认颜色浪潮蓝
     *
     * @param activity
     */
    public static void changeStateBarColor(Activity activity) {
        activity.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //系统版本小于19的不再处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true, activity);
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.header_bg);// 通知栏所需颜色
        }
    }

    /**
     * 控制状态栏显示隐藏
     *
     * @param activity
     * @param showOrHide，true显示，false隐藏
     */
    public static void showOrHideStatusBar(Activity activity, boolean showOrHide) {
        activity.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //系统版本小于19的不再处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true, activity);
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(showOrHide);
        }
    }

    /**
     * 修改状态栏颜色，传入颜色为R.id形式
     *
     * @param activity
     * @param color
     */
    public static void changeStateBarColor(Activity activity, int color) {
        activity.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //系统版本小于19的不再处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true, activity);
            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(color);// 通知栏所需颜色
        }
    }

    @TargetApi(19)
    private static void setTranslucentStatus(boolean on, Activity activity) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStateBarHeight(Context context) {
        /**
         * 获取状态栏高度——方法1
         * */
        int statusBarHeight = -1;
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}
