package com.inspur.emmcloud.util.common;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
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
    public static void SetStateBarColor(Activity activity) {
        //系统版本小于19的不再处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS );
            setTranslucentStatus( true, activity );
            SystemBarTintManager tintManager = new SystemBarTintManager( activity );
            tintManager.setStatusBarTintEnabled( true );
            tintManager.setStatusBarTintResource( R.color.header_bg );// 通知栏所需颜色
        }
    }

    /**
     * 设置状态栏的沉浸式
     * @param activity
     */
    public static void translucent(Activity activity) {
        //系统版本小于19的不再处理
        int color = ResourceUtils.getValueOfColorAttr(activity,R.attr.header_bg_color);
        translucent( activity, color );
    }

    /**
     * 修改状态栏颜色，传入颜色为R.id形式
     * 设置状态栏的沉浸式
     * @param activity
     * @param color
     */
    public static void translucent(Activity activity, int color) {
        //系统版本小于19的不再处理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS );
            setTranslucentStatus( true, activity );
            SystemBarTintManager tintManager = new SystemBarTintManager( activity );
            tintManager.setStatusBarTintEnabled( true );
            tintManager.setStatusBarTintResource( color );// 通知栏所需颜色
            setTranslucentStateBar( activity );
        }
    }

    /**
     * 修改状态栏字体颜色，传入颜色为R.id形式
     *@param barTextIsBlack
     * @param activity
     */
    public static void setStateBarTextColor(Activity activity, boolean isStatusBarTextColorBlack) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
            Window window = activity.getWindow();
            window.clearFlags( WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS );
            window.addFlags( WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS );
            window.getDecorView().setSystemUiVisibility(isStatusBarTextColorBlack?View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR:View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            ViewGroup mContentView = (ViewGroup) window.findViewById( Window.ID_ANDROID_CONTENT );
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                ViewCompat.setFitsSystemWindows( mChildView, false );
                ViewCompat.requestApplyInsets( mChildView );
            }
        }
    }

    /**
     * 设置沉浸式状态栏（华为手机不显示沉浸状态）
     */
    public static void setTranslucentStateBar(Activity activity) {
        Window window = activity.getWindow();
        window.clearFlags( WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS );
        window.getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE );
        window.addFlags( WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor( Color.TRANSPARENT );
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
        win.setAttributes( winParams );
    }
}
