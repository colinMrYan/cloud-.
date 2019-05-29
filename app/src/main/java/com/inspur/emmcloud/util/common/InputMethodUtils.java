package com.inspur.emmcloud.util.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.privates.AppUtils;

/**
 * 隐藏输入法弹出框
 */
public class InputMethodUtils {
    public static void hide(Activity activity) {
        try {
            //解决关闭软键盘抛出异常的问题
            View view = activity.getWindow().peekDecorView();
            if (view != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    /**
     * 显示虚拟键盘
     * @param v
     */
    public static void showKeyboard(View v) {
        InputMethodManager imm = ( InputMethodManager ) v.getContext( ).getSystemService( Context.INPUT_METHOD_SERVICE );
        imm.showSoftInput(v,InputMethodManager.SHOW_FORCED);
    }

    public static void display(final Activity activity, final EditText editText) {
        display(activity, editText, 300);
    }

    public static void display(final Activity activity, final EditText editText, int delay) {
        try {
            editText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    editText.requestFocus();
                    ((InputMethodManager) activity
                            .getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showSoftInput(editText, 0);
                }
            }, delay);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public static boolean isSoftInputShow(Activity activity) {
        return InputMethodUtils.getSupportSoftInputHeight(activity) > 0;
    }

    public static int getSupportSoftInputHeight(Activity activity) {
        Rect r = new Rect();
        activity.getWindow().getDecorView()
                .getWindowVisibleDisplayFrame(r);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        //消息手机需要特殊处理：如果小米手机隐藏了NavigationBar，就在获取到的高度基础上加上NavigationBar的高度
        if (AppUtils.getIsXiaoMi() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            boolean isHideNavigationBar = Settings.Global.getInt(activity.getContentResolver(), "force_fsg_nav_bar", 0) != 0;
            if (isHideNavigationBar) {
                int navigationBarHeight = ResolutionUtils.getNavigationBarHeight();
                screenHeight = screenHeight + navigationBarHeight;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            screenHeight = screenHeight + ResolutionUtils.getStatusBarHeightAboutAndroidP(activity);
        }
        int softInputHeight = screenHeight - r.bottom;
        if (softInputHeight < 200) {
            softInputHeight = 0;
            Log.w("EmotionInputDetector",
                    "Warning: value of softInputHeight is below zero!");
        }
        if (softInputHeight > 0) {
            PreferencesUtils.putInt(activity, Constant.PREF_SOFT_INPUT_HEIGHT, softInputHeight);
        }
        return softInputHeight;
    }


}
