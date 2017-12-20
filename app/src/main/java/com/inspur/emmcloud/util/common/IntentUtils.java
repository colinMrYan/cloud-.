package com.inspur.emmcloud.util.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class IntentUtils {

	/**
	 * 
	 *
	 * @param context
	 * @param clz
	 */
	public static void startActivity(Activity context, Class<?> clz) {
		startActivity(context, clz, null, false);
	}

	/**
	 * 
	 *
	 * @param context
	 * @param clz
	 */
	public static void startActivity(Activity context, Class<?> clz,
			boolean isFinishCurrentActivity) {
		startActivity(context, clz, null, isFinishCurrentActivity);
	}

	/**
	 * 
	 *
	 * @param context
	 * @param clz
	 * @param bundle
	 */
	public static void startActivity(Activity context, Class<?> clz,
			Bundle bundle) {
		startActivity(context, clz, bundle, false);
	}

	/**
	 * 
	 *
	 * @param context
	 * @param clz
	 * @param bundle
	 * @param isFinishCurrentActivity
	 *            是否关闭当前Activity
	 */
	public static void startActivity(Activity context, Class<?> clz,
			Bundle bundle, boolean isFinishCurrentActivity) {
		Intent intent = new Intent();
		intent.setClass(context, clz);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		context.startActivity(intent);
		if (isFinishCurrentActivity) {
			((Activity) context).finish();
		}
	}

//	/**
//	 * 
//	 *
//	 * @param context
//	 * @param clz
//	 * @param requestCode
//	 */
//	public static void startActivityForResult(Activity context, Class<?> clz,
//			int requestCode) {
//		startActivityForResult(context, clz, null, requestCode);
//	}
//
//	/**
//	 * 
//	 *
//	 * @param context
//	 * @param clz
//	 * @param bundle
//	 * @param requestCode
//	 */
//	public static void startActivityForResult(Activity context, Class<?> clz,
//			Bundle bundle, int requestCode) {
//		Intent intent = new Intent();
//		intent.setClass(context, clz);
//		if (bundle != null) {
//			intent.putExtras(bundle);
//		}
//		 context.startActivityForResult(intent, requestCode);
//	}

}
