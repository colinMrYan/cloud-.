package com.inspur.mdm.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MDMUtils {
	/**
	 * 获取设备分辨率
	 * 
	 * @param context
	 * @return
	 */
	public static String getDeviceResolution(Context context) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		int width = displayMetrics.widthPixels;
		int height = getHeight(((Activity) context));
		return height + "*" + width;
	}

	/**
	 * 支持带有虚拟按键手机屏幕高度的计算
	 * 
	 * @param context
	 * @return
	 */
	private static int getHeight(Activity context) {
		int dpi = 0;
		Display display = context.getWindowManager().getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		Class c;
		try {
			c = Class.forName("android.view.Display");
			@SuppressWarnings("unchecked")
			Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
			method.invoke(display, dm);
			dpi = dm.heightPixels;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dpi;
	}

	/**
	 * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
	 * 
	 * @param context
	 * @return 平板返回 True，手机返回 False
	 */
	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * 获取应用版本号
	 * 
	 * @param context
	 * @return
	 */
	public static String getAppVersionCode(Context context) {
		String versionCode = null;
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			versionCode = info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionCode;
	}

//	/**
//	 * 获取设备UUID
//	 *
//	 * @param activity
//	 * @return
//	 */
//	public static String getMyUUID(Activity activity) {
//		final TelephonyManager tm = (TelephonyManager) activity
//				.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//		final String tmDevice, tmSerial, tmPhone, androidId;
//		tmDevice = "" + tm.getDeviceId();
//		tmSerial = "" + tm.getSimSerialNumber();
//		androidId = ""
//				+ android.provider.Settings.Secure.getString(
//						activity.getContentResolver(),
//						android.provider.Settings.Secure.ANDROID_ID);
//		UUID deviceUuid = new UUID(androidId.hashCode(),
//				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
//		String uniqueId = deviceUuid.toString();
//		return uniqueId;
//
//	}

	/**
	 * 获取手机号
	 * 
	 * @param context
	 * @return
	 */
	public static String getPhoneNum(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			String phoneNum = tm.getLine1Number();
			if (phoneNum != null && phoneNum.length() > 11) {
				phoneNum = phoneNum.substring(phoneNum.length() - 11,
						phoneNum.length());
			}
			return phoneNum;// 获取本机号码
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}

	}

	/**
	 * 判断输入手机号是否合格
	 * 
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobileNum(String mobiles) {
		Pattern p = Pattern.compile("^([1][345678])\\d{9}$");
		Matcher m = p.matcher(mobiles);
		System.out.println(m.matches() + "---");
		return m.matches();
	}

	/**
	 * 判断输入邮箱账号是否合格
	 * 
	 * @param mail
	 * @return
	 */
	public static boolean isEmail(String mail) {
		String malReg = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		return mail.matches(malReg);
	}

	// 判断是否有网络连接
	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
			return true;
		} else {
			Toast.makeText(
					context,
					context.getString(MDMResUtils
							.getStringID("network_exception")),
					Toast.LENGTH_SHORT).show();
			return false;
		}
	}
}
