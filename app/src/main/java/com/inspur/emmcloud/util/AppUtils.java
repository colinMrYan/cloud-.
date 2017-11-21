package com.inspur.emmcloud.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.inspur.emmcloud.R;
import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 有关应用的一些方法
 *
 * @author Administrator
 */
public class AppUtils {

    private static final String TAG = "AppUtils";

    /**
     * 判断应用是否运行在设备的最前端
     **/
    public static boolean isAppOnForeground(Context context) {
        // TODO Auto-generated method stub
        try {
            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager
                    .getRunningAppProcesses();
            if (appProcessInfos == null || appProcessInfos.size() == 0) {
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

    /**
     * 判断是否添加了快捷方式
     **/
    public static boolean isHasShortCut(Context context) {

        boolean isInstallShortcut = false;
        try {
            final ContentResolver cr = context.getContentResolver();
            String AUTHORITY = "com.android.launcher2.settings";
            final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
                    + "/favorites?notify=true");
            Cursor c = cr
                    .query(CONTENT_URI,
                            new String[]{"title", "iconResource"},
                            "title=?",
                            new String[]{context.getString(R.string.app_name)},
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

    /**
     * 获取版本号
     **/
    public static String getVersion(Context context) {
        String versionCode = null;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),
                    0);
            // 截取正常版本号的方法
            versionCode = info.versionName;
//			versionCode = getNormalVersionCode(info.versionName);
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
            try {
                String saveVersionCode = getNormalVersionCode(savedArray[2]);
                savedArray[2] = saveVersionCode;
                String currentVersionCode = getNormalVersionCode(currentArray[2]);
                currentArray[2] = currentVersionCode;
            } catch (Exception e) {
                LogUtils.YfcDebug("捕获版本异常：" + e.getMessage());
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
     * @param str 短信内容
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
     * 获取IMEI号
     *
     * @param context
     * @return
     */
    public static String getIMEICode(Context context) {
    /**
     * 唯一的设备ID：
     * GSM手机的 IMEI 和 CDMA手机的 MEID.
     * Return null if device ID is not available.
     */
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();//String
        if (StringUtils.isBlank(imei)) {
            return "";
        }
        return imei;
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
        String uuid = PreferencesUtils.getString(context, "device_uuid", "");
        if (StringUtils.isBlank(uuid)) {
            String tmDevice, tmSerial, androidId;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = ""
                    + android.provider.Settings.Secure.getString(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
            UUID deviceUuid = new UUID(androidId.hashCode(),
                    ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            uuid = deviceUuid.toString();
            PreferencesUtils.putString(context, "device_uuid", uuid);
        }

        return uuid;

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

    /**
     * 获取厂商名称如HUAWEI，Xiaomi
     *
     * @return
     */
    public static String GetChangShang() {

        String manString = android.os.Build.MANUFACTURER;
        if (TextUtils.isEmpty(manString)) {
            return "UNKNOWN";
        }
        return manString;
    }

    /**
     * 获取是否华为手机
     *
     * @return
     */
    public static boolean getIsHuaWei() {
        return  AppUtils.GetChangShang().toLowerCase().startsWith("huawei");
    }

    /**
     * 获取手机型号如华为下某型号（MHA-AL00）
     *
     * @return
     */
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

    /**
     * 判断app是否已安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        boolean installed = false;
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    /**
     * 打开APK文件（安装APK应用）
     *
     * @param context
     * @param file
     */
    public static void openAPKFile(Activity context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        context.startActivityForResult(intent, ImpActivity.DO_NOTHING_RESULTCODE);
    }

    /**
     * 获取手机dpi的方法 返回整型值
     *
     * @return
     */
    public static int getScreenDpi(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.densityDpi;
    }

    /**
     * 获取屏幕密度的方法 返回浮点值
     *
     * @param activity
     * @return
     */
    public static float getDensity(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }

    /**
     * 获取屏幕类型
     *
     * @param activity
     * @return
     */
    public static String getScreenType(Activity activity) {
        int kkhdpi = 2560 * 1600;
        int xxHdpi = 1920 * 1080;
        int xhdpi = 1080 * 720;
        int screenSize = ResolutionUtils.getResolution(activity);
        if (screenSize >= kkhdpi) {
            return "2k";
        } else if (screenSize >= xxHdpi) {
            return "xxhdpi";
        } else if (screenSize >= xhdpi) {
            return "xhdpi";
        } else {
            return "hdpi";
        }
    }

    /**
     * 获取当前手机系统版本号如6.0,7.0
     *
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取sdk版本如19,24等
     * @return
     */
    public static int getSDKVersion(){
        return Build.VERSION.SDK_INT;
    }

    /**
     * 判断应用是否已经启动
     * @param context 一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName){
        ActivityManager activityManager =
                (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
        for(int i = 0; i < processInfos.size(); i++){
            if(processInfos.get(i).processName.equals(packageName)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否有SD卡
     * @param context
     * @return
     */
    public static boolean isHasSDCard(Context context){
        if ( Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)){
            return true;
        }
        ToastUtils.show(context,
                R.string.filetransfer_sd_not_exist);
        return false;

    }


    /**
     * 调用文件系统
     */
    public static void openFileSystem(Activity activity,int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(
                Intent.createChooser(intent,
                        activity.getString(R.string.file_upload_tips)),
                requestCode);
    }

    /**
     * 调用图库
     */
    public static void openGallery(Activity activity,int limit,int requestCode) {
        initImagePicker(limit);
        Intent intent = new Intent(activity,
                ImageGridActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 初始化图片选择控件
     */
    private static void initImagePicker(int limit) {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(ImageDisplayUtils.getInstance()); // 设置图片加载器
        imagePicker.setShowCamera(false); // 显示拍照按钮
        imagePicker.setCrop(false); // 允许裁剪（单选才有效）
        imagePicker.setSelectLimit(limit);
//		imagePicker.setSaveRectangle(true); // 是否按矩形区域保存
        imagePicker.setMultiMode(true);
//		imagePicker.setStyle(CropImageView.Style.RECTANGLE); // 裁剪框的形状
//		imagePicker.setFocusWidth(1000); // 裁剪框的宽度。单位像素（圆形自动取宽高最小值）
//		imagePicker.setFocusHeight(1000); // 裁剪框的高度。单位像素（圆形自动取宽高最小值）
//		imagePicker.setOutPutX(1000); // 保存文件的宽度。单位像素
//		imagePicker.setOutPutY(1000); // 保存文件的高度。单位像素
    }



}
