package com.inspur.emmcloud.util.common;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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
        try {
            editText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    editText.requestFocus();
                    ((InputMethodManager) activity
                            .getSystemService(activity.INPUT_METHOD_SERVICE))
                            .showSoftInput(editText, 0);
                }
            }, 300);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
