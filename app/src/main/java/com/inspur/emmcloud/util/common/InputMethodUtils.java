package com.inspur.emmcloud.util.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.inspur.emmcloud.config.Constant;

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
                            .getSystemService(activity.INPUT_METHOD_SERVICE))
                            .showSoftInput(editText, 0);
                }
            }, delay);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public static boolean isSoftInputShow(Activity activity) {
        return InputMethodUtils.getSupportSoftInputHeight(activity) != 0;
    }

    public static int getSupportSoftInputHeight(Activity activity) {
        Rect r = new Rect();
        activity.getWindow().getDecorView()
                .getWindowVisibleDisplayFrame(r);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int softInputHeight = screenHeight - r.bottom;
        if (softInputHeight < 0) {
            Log.w("EmotionInputDetector",
                    "Warning: value of softInputHeight is below zero!");
        }
        if (softInputHeight > 0) {
            PreferencesUtils.putInt(activity, Constant.PREF_SOFT_INPUT_HEIGHT, softInputHeight);
        }
        return softInputHeight;
    }
}
