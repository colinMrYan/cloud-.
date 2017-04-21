package com.inspur.emmcloud.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.inspur.emmcloud.R;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 有关应用的一些方法
 * 
 * @author Administrator
 *
 */
public class AppUtils {

	private static final String TAG = "AppUtils";

	/** 判断应用是否运行在设备的最前端 **/
	public static boolean isAppOnForeground(Context context) {
		// TODO Auto-generated method stub
		try {
			ActivityManager activityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager
					.getRunningAppProcesses();
			if(appProcessInfos == null || appProcessInfos.size() == 0){
				return false;
			}
			// 枚举进程
			for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
				if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					if (appProcessInfo.processName.equals(context
							.getApplicationInfo().processName)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return false;

	}

	/**
	 * 
	 *
	 * @param context
	 * @return
	 */
	public static boolean isApkDebugable(Context context) {
		try {
			ApplicationInfo info = context.getApplicationInfo();
			return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/** 判断是否添加了快捷方式 **/
	public static boolean isHasShortCut(Context context) {

		boolean isInstallShortcut = false;
		try {
			final ContentResolver cr = context.getContentResolver();
			String AUTHORITY = "com.android.launcher2.settings";
			final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
					+ "/favorites?notify=true");
			Cursor c = cr
					.query(CONTENT_URI,
							new String[] { "title", "iconResource" },
							"title=?",
							new String[] { context.getString(R.string.app_name) },
							null);

			if (c != null && c.getCount() > 0) {
				isInstallShortcut = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return isInstallShortcut;
	}

	/** 获取版本号 **/
	public static String getVersion(Context context) {
		String versionCode = null;
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			// 截取正常版本号的方法
			// versionCode = info.versionName;
			versionCode = getNormalVersionCode(info.versionName);
		} catch (Exception e) {
			LogUtils.exceptionDebug(TAG, e.toString());
		}
		return versionCode;
	}

	/**
	 * 如果包含Beta则截取bata前的版本号信息
	 * 
	 * @param versionCode
	 * @return
	 */
	private static String getNormalVersionCode(String versionCode) {
		if (versionCode.contains("Beta")) {
			int betaLoction = versionCode.indexOf(" Beta");
			versionCode = versionCode.substring(0, betaLoction);
		}
		return versionCode;
	}

	/**
	 * 判断应用是否进行了版本升级
	 * 
	 * @param savedVersion
	 * @param currentVersion
	 * @return
	 */
	public static boolean isAppHasUpgraded(String savedVersion,
			String currentVersion) {
		if (savedVersion != null || currentVersion != null) {
			String[] savedArray = savedVersion.split("\\.");
			String[] currentArray = currentVersion.split("\\.");
try{
	String saveVersionCode = getNormalVersionCode(savedArray[2]);
	savedArray[2] = saveVersionCode;
	String currentVersionCode = getNormalVersionCode(savedArray[2]);
	currentArray[2] = currentVersionCode;
}catch (Exception e){
	LogUtils.YfcDebug("捕获版本异常："+e.getMessage());
	e.printStackTrace();
}


			if (savedArray.length != 3) {
				return false;
			} else if (currentArray.length != 3) {
				return false;
			} else if (Integer.parseInt(savedArray[0]) != Integer
					.parseInt(currentArray[0])
					|| Integer.parseInt(savedArray[1]) != Integer
							.parseInt(currentArray[1])) {
				return false;
			} else if (Integer.parseInt(savedArray[2]) < Integer
					.parseInt(currentArray[2])) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * 从字符串中截取连续6位数字 用于从短信中获取动态密码
	 * 
	 * @param str
	 *            短信内容
	 * @return 截取得到的6位动态密码
	 */
	public static String getDynamicPassword(String str) {
		Pattern continuousNumberPattern = Pattern.compile("[0-9\\.]+");
		Matcher m = continuousNumberPattern.matcher(str);
		String dynamicPassword = "";
		while (m.find()) {
			if (m.group().length() > 3) {
				dynamicPassword = m.group();
			}
		}

		return dynamicPassword;
	}

	/**
	 * 获取设备UUID
	 * 
	 * @param context
	 * @return
	 */
	public static String getMyUUID(Context context) {
		final TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		final String tmDevice, tmSerial, tmPhone, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = ""
				+ android.provider.Settings.Secure.getString(
						context.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
		UUID deviceUuid = new UUID(androidId.hashCode(),
				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String uniqueId = deviceUuid.toString();
		return uniqueId;

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

	public static int getTextViewHeight(float fontSize) {
		Paint paint = new Paint();
		paint.setTextSize(fontSize);
		FontMetrics fm = paint.getFontMetrics();
		return (int) Math.ceil(fm.bottom - fm.top);
	}

	public static int getSDKVersionNumber() {
		int sdkVersion;
		try {
			sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			sdkVersion = 0;
		}
		return sdkVersion;
	}

	public static String getReleaseVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	public static String GetChangShang() {

		String manString = android.os.Build.MANUFACTURER;
		if (TextUtils.isEmpty(manString)) {
			return "UNKNOWN";
		}
		return manString;
	}

	public static String GetModel() {
		String modelStr = android.os.Build.MODEL;
		modelStr = modelStr.replace(" ", "-");
		if (TextUtils.isEmpty(modelStr)) {
			return "UNKNOWN";
		}
		return modelStr;
	}

	/**
	 * 判断服务是否存在
	 * 
	 * @param mContext
	 * @param serviceName
	 * @return
	 */
	public static boolean isServiceWork(Context mContext, String serviceName) {
		boolean isWork = false;
		ActivityManager myAM = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> myList = myAM
				.getRunningServices(Integer.MAX_VALUE);
		if (myList.size() <= 0) {
			return false;
		}
		for (int i = 0; i < myList.size(); i++) {
			String mName = myList.get(i).service.getClassName().toString();
			if (mName.equals(serviceName)) {
				isWork = true;
				break;
			}
		}
		return isWork;
	}
	
}
