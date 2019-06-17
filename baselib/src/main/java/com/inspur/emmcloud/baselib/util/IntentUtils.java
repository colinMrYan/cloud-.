package com.inspur.emmcloud.baselib.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

public class IntentUtils {

    /**
     * @param context
     * @param clz
     */
    public static void startActivity(Activity context, Class<?> clz) {
        startActivity(context, clz, null, false);
    }

    /**
     * @param context
     * @param clz
     */
    public static void startActivity(Activity context, Class<?> clz,
                                     boolean isFinishCurrentActivity) {
        startActivity(context, clz, null, isFinishCurrentActivity);
    }

    /**
     * @param context
     * @param clz
     * @param bundle
     */
    public static void startActivity(Activity context, Class<?> clz,
                                     Bundle bundle) {
        startActivity(context, clz, bundle, false);
    }

    /**
     * @param context
     * @param clz
     * @param bundle
     * @param isFinishCurrentActivity 是否关闭当前Activity
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
            context.finish();
        }
    }

    public static void startActivity(Activity context, String scheme) {
        try {
//			Uri uri = Uri.parse("scheme://host/path?param1=abc&param2=cde");
            Uri uri = Uri.parse(scheme);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = intent.resolveActivity(packageManager);
            if (componentName != null) {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
