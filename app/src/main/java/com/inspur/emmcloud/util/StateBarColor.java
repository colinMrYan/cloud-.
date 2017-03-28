package com.inspur.emmcloud.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import com.inspur.emmcloud.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class StateBarColor {

	/**
	 * 求改状态栏颜色为默认颜色浪潮蓝
	 * @param activity
     */
	public static void changeStateBarColor(Activity activity) {
		activity.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true, activity);
		}
		SystemBarTintManager tintManager = new SystemBarTintManager(activity);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.header_bg);// 通知栏所需颜色
	}

	/**
	 * 修改状态栏颜色，传入颜色为R.id形式
	 * @param activity
	 * @param color
     */
	public static void changeStateBarColor(Activity activity,int color) {
		activity.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true, activity);
		}
		SystemBarTintManager tintManager = new SystemBarTintManager(activity);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(color);// 通知栏所需颜色
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


}
