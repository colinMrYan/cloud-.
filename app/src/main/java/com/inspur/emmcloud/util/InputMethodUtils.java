package com.inspur.emmcloud.util;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 
 * 隐藏输入法弹出框
 *
 */
public class InputMethodUtils {
	public static void hide(Activity activity) {
		try {
			((InputMethodManager) activity
					.getSystemService(activity.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(activity.getCurrentFocus()
							.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	public static void display(final Activity activity, final EditText editText) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					((InputMethodManager) activity
							.getSystemService(activity.INPUT_METHOD_SERVICE))
							.showSoftInput(editText, 0);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}, 998);

	}
}
