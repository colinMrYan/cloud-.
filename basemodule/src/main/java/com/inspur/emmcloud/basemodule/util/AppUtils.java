package com.inspur.emmcloud.basemodule.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AppOpsManager;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.github.zafarkhaja.semver.Version;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.EncryptUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.NotificationSetUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ResolutionUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.componentservice.web.WebService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    private static long lastTotalRxBytes = 0;
    private static long lastTimeStamp = System.currentTimeMillis();

    /**
     * 获取当前App网速
     *
     * @param uid
     * @return
     */
    public static String getNetSpeed(int uid) {
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0
                : (TrafficStats.getTotalRxBytes() / 1024);
        ;
        long nowTimeStamp = System.currentTimeMillis();
        long divide = nowTimeStamp - lastTimeStamp;
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (divide == 0 ? 1 : divide));// 毫秒转换
        lastTimeStamp = nowTimeStamp;
        return String.valueOf(speed) + " kb/s";
    }

    /**
     * 判断应用是否运行在设备的最前端
     **/
    public static boolean isAppOnForeground(Context context) {
        // TODO Auto-generated method stub
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
            if (appProcessInfos == null || appProcessInfos.size() == 0) {
                return false;
            }
            // 枚举进程
            for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
                if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    if (appProcessInfo.processName.equals(context.getApplicationInfo().processName)) {
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
            final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
            Cursor c = cr.query(CONTENT_URI, new String[]{"title", "iconResource"}, "title=?",
                    new String[]{context.getString(R.string.app_name)}, null);

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
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            // 截取正常版本号的方法
            versionCode = info.versionName;
            // versionCode = getNormalVersionCode(info.versionName);
        } catch (Exception e) {
            LogUtils.exceptionDebug(TAG, e.toString());
        }
        return versionCode;
    }

    /**
     * 获取应用包名
     *
     * @param context
     * @return
     */
    public static String getPackageName(Context context) {
        return context.getPackageName();
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
     * @param context
     * @return
     */
    public static boolean isAppHasUpgraded(Context context) {
        String previousVersionValue = PreferencesUtils.getString(context, Constant.PREF_APP_PREVIOUS_VERSION, "");
        String currentVersionValue = getVersion(context);
        try {
            if (!StringUtils.isBlank(previousVersionValue)) {
                Version previousVersion = Version.valueOf(previousVersionValue);
                Version currentVersion = Version.valueOf(currentVersionValue);
                return currentVersion.greaterThan(previousVersion);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isPreviousLitterThan(Context context, String version) {
        String previousVersionValue = PreferencesUtils.getString(context, Constant.PREF_APP_PREVIOUS_VERSION, "");
        try {
            if (!StringUtils.isBlank(previousVersionValue)) {
                Version previousVersion = Version.valueOf(previousVersionValue);
                Version targetVersion = Version.valueOf(version);
                return targetVersion.greaterThan(previousVersion);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
     * 获取设备UUID
     *
     * @param context
     * @return
     */
    public static String getMyUUID(Context context) {
        String uuid = getUUID(context);
        saveUUID(context, uuid);
        return uuid;
    }

    /**
     * 获取随机数
     *
     * @param length
     * @return
     */
    public static String getRandomStr(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        if (length <= 0) {
            length = 1;
        }
        Random random = new Random();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < length; i++) {
            res.append(str.charAt(random.nextInt(str.length())));
        }
        return res.toString();
    }

    /**
     * 获取UUID，类内部使用，不暴露给外部
     *
     * @param context
     * @return
     */
    private static String getUUID(Context context) {
        // 先读SharePreference，如果SharePreference里有UUID则返回SharePreference里的UUID，并判断SD卡里是否存在UUID文件，没有则存一份
        String uniqueId = PreferencesUtils.getString(context, "device_uuid", "");
        if (!StringUtils.isBlank(uniqueId)) {
            return uniqueId;
        }
        // 如果SharePreference里没有，则检查SD卡里有没有UUID文件，如果有则返回sd卡里的UUID，并向SharePreference里存一份
        uniqueId = getUUIDFromSDCardFile(context);
        if (!StringUtils.isBlank(uniqueId)) {
            return uniqueId;
        } else {
            if (FileUtils.isFileExist(Constant.CONCIG_CLOUD_PLUS_UUID_FILE)) {
                FileUtils.deleteFile(Constant.CONCIG_CLOUD_PLUS_UUID_FILE);
            }
        }
        // 如果前两个都没有，则生成一个UUID，并存到SharePreference和SD卡文件里
        if (StringUtils.isBlank(uniqueId)) {
            uniqueId = getDeviceUUID(context);
        }
        return uniqueId;
    }

    /**
     * 存UUID
     *
     * @param context
     */
    private static void saveUUID(Context context, String uuid) {
        PreferencesUtils.putString(context, "device_uuid", uuid);
        if (isHasSDCard(context) && !FileUtils.isFileExist(Constant.CONCIG_CLOUD_PLUS_UUID_FILE)) {
            saveDeviceUUID2SDCardFile(context, uuid);
        }
    }

    /**
     * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
     *
     * @param context
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
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
        return AppUtils.GetChangShang().toLowerCase().startsWith(Constant.HUAWEI_FLAG);
    }

    public static boolean getIsXiaoMi() {
        return AppUtils.GetChangShang().toLowerCase().startsWith(Constant.XIAOMI_FLAG);
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
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);
        if (myList == null || myList.size() <= 0) {
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
        } catch (Exception e) {
            installed = false;
        }
        return installed;
    }

    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAvilibleByPackageName(Context context, String packageName) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
            List<String> packageNames = new ArrayList<String>();
            if (packageInfos != null) {
                for (int i = 0; i < packageInfos.size(); i++) {
                    String packName = packageInfos.get(i).packageName;
                    packageNames.add(packName);
                }
            }
            // 判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
            return packageNames.contains(packageName);
        } catch (Exception e) {
            return false;
        }
    }


    // /**
    // * 打开APK文件（安装APK应用）
    // *
    // * @param context
    // * @param file
    // */
    // public static void openAPKFile(Activity context, File file) {
    // Intent intent =new Intent(Intent.ACTION_VIEW);
    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // //判断是否是AndroidN以及更高的版本
    // if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N) {
    // Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".fileprovider",file);
    // intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    // intent.setDataAndType(contentUri, FileUtils.getMimeType(file));
    // }else{
    // intent.setDataAndType(Uri.fromFile(file),FileUtils.getMimeType(file));
    // }
    // if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
    // context.startActivityForResult(intent, ImpActivity.DO_NOTHING_RESULTCODE);
    // }
    // }

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
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取sdk版本如19,24等
     *
     * @return
     */
    public static int getSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 判断应用是否已经启动
     *
     * @param context     一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processInfos.size(); i++) {
            if (processInfos.get(i).processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否有SD卡
     *
     * @param context
     * @return
     */
    public static boolean isHasSDCard(Context context) {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

    }

    /**
     * 调用文件系统
     */
    public static void openFileSystem(Activity activity, int requestCode) {
        openFileSystem(activity, requestCode, 1);
    }

    /**
     * 调用文件系统
     */
    public static void openFileSystem(Activity activity, int requestCode, int maximum) {
        Bundle bundle = new Bundle();
        bundle.putInt("extra_maximum", maximum);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_FILEMANAGER).with(bundle).navigation(activity, requestCode);
    }

    /**
     * 调用文件系统
     */
    public static void openFileSystemWithVolume(Activity activity, int requestCode, int maximum) {
        Bundle bundle = new Bundle();
        bundle.putInt("extra_maximum", maximum);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_FILEMANAGER_WITH_VOLUME).with(bundle).navigation(activity, requestCode);
    }

    /**
     * 调用图库
     */
    public static void openGallery(Activity activity, int limit, int requestCode) {
        openGallery(activity, limit, requestCode, false);
    }

    /**
     * 沟通调用图库
     */
    public static void openGallery(Activity activity, int limit, int requestCode, boolean isSupportOrigin) {
        Router router = Router.getInstance();
        if (router.getService(WebService.class) != null) {
            WebService service = router.getService(WebService.class);
            service.openGallery(activity, limit, requestCode, isSupportOrigin);
        }
    }

    /**
     * 调用系统相机
     *
     * @param activity
     * @param picPath
     * @param requestCode
     */
    public static void openCamera(final Activity activity, final String picPath, final int requestCode) {

        Router router = Router.getInstance();
        if (router.getService(WebService.class) != null) {
            WebService service = router.getService(WebService.class);
            service.openCamera(activity, picPath, requestCode);
        }


    }

    public static void openScanCode(final Activity activity, final int requestCode) {
        Router router = Router.getInstance();
        if (router.getService(WebService.class) != null) {
            WebService service = router.getService(WebService.class);
            service.openScanCode(activity, requestCode);
        }
    }


    public static void openScanCode(final Fragment fragment, final int requestCode) {
        Router router = Router.getInstance();
        if (router.getService(WebService.class) != null) {
            WebService service = router.getService(WebService.class);
            service.openScanCode(fragment, requestCode);
        }
    }


    /**
     * 发短信
     *
     * @param activity
     * @param phoneNum
     * @param requestCode
     */
    public static void sendSMS(Activity activity, String phoneNum, int requestCode) {
        Uri smsToUri = Uri.parse("smsto:" + phoneNum);
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        activity.startActivityForResult(intent, requestCode);

    }

    /**
     * 打电话
     *
     * @param activity
     * @param phoneNum
     * @param requestCode
     */
    public static void call(final Activity activity, final String phoneNum, final int requestCode) {
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(activity, Permissions.CALL_PHONE,
                new PermissionRequestCallback() {
                    @Override
                    public void onPermissionRequestSuccess(List<String> permissions) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
                            activity.startActivityForResult(intent, requestCode);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                            activity.finish();
                        }

                    }

                    @Override
                    public void onPermissionRequestFail(List<String> permissions) {
                        ToastUtils.show(activity,
                                PermissionRequestManagerUtils.getInstance().getPermissionToast(activity, permissions));
                        activity.finish();
                    }
                });
    }

    /**
     * 发邮件
     *
     * @param activity
     * @param mail
     * @param requestCode
     */
    public static void sendMail(Activity activity, String mail, int requestCode) {
        Uri uri = Uri.parse("mailto:" + mail);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        activity.startActivityForResult(
                Intent.createChooser(intent, activity.getString(R.string.please_select_app_of_mail)), 1);
    }


    /**
     * 获取SD卡文件里的UUID
     *
     * @param context
     * @return
     */
    private static String getUUIDFromSDCardFile(Context context) {
        if (!isHasSDCard(context) || !FileUtils.isFileExist(Constant.CONCIG_CLOUD_PLUS_UUID_FILE)) {
            return "";
        }
        try {
            return EncryptUtils.decode(FileUtils.readFile(Constant.CONCIG_CLOUD_PLUS_UUID_FILE, "utf-8").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取设备UUID，动态获取
     * 影响要素有设备序列号，sim卡串号，系统id
     *
     * @param context
     * @return
     */
    private static String getDeviceUUID(final Context context) {
        try {
            final String[] uniqueId = new String[1];
            String androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
            String deviceInfo = "1234" + Build.BOARD.length() % 10
                    + Build.BRAND.length() % 10
                    + Build.DEVICE.length() % 10 + Build.DISPLAY.length() % 10
                    + Build.HOST.length() % 10 + Build.ID.length() % 10
                    + Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10
                    + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10
                    + Build.TYPE.length() % 10 + Build.USER.length() % 10;// 12 位
            UUID deviceUuid = new UUID(androidId.hashCode(), deviceInfo.hashCode());
            uniqueId[0] = deviceUuid.toString();
            return uniqueId[0];
        } catch (SecurityException e) {
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 存储uuid到SD卡文件系统，不分租户用户
     *
     * @param context
     * @param uuid
     * @return
     */
    private static void saveDeviceUUID2SDCardFile(Context context, String uuid) {
        try {
            uuid = EncryptUtils.encode(uuid);
            FileUtils.writeFile(Constant.CONCIG_CLOUD_PLUS_UUID_FILE, uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取设备名称
     *
     * @param context
     * @return
     */
    public static String getDeviceName(Context context) {
        boolean isTelbet = AppUtils.isTablet(context);
        String username = PreferencesUtils.getString(context, "userRealName");
        return username + (isTelbet ? "的平板电脑" : "的手机");
    }


    /**
     * 设置添加屏幕的背景透明度
     *
     * @param activity
     * @param bgAlpha
     */
    public static void setWindowBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha; // 0.0-1.0
        activity.getWindow().setAttributes(lp);
    }

    /**
     * app是否是标准版
     *
     * @return
     */
    public static boolean isAppVersionStandard() {
        boolean isAppVersionStandard = true;
        String appFirstLoadAlis = PreferencesUtils.getString(BaseApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS);
        if (appFirstLoadAlis != null && !appFirstLoadAlis.equals("Standard")) {
            isAppVersionStandard = false;
        }
        return isAppVersionStandard;

    }

    /**
     * 获取版本名
     *
     * @param context
     * @return
     */
    public static String getManifestAppVersionFlag(Context context) {
        return getManifestMetadata(context, "FLAG_APP_VERSION_TYPE");
    }

    /**
     * 获取manifest中的metadata值
     *
     * @param context
     * @param key
     * @return
     */
    public static String getManifestMetadata(Context context, String key) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            return appInfo.metaData.getString(key);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            int appNameRes = -1;
            if (isAppVersionStandard()) {
                appNameRes = R.string.app_name;
            } else {
                String appFirstLoadAlis =
                        PreferencesUtils.getString(BaseApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS);
                appNameRes = Res.getStringID("app_name_" + appFirstLoadAlis);
            }
            return context.getResources().getString(appNameRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context.getResources().getString(R.string.app_name);
    }

    /**
     * 获取app图标
     *
     * @param context
     * @return
     */
    public static int getAppIconRes(Context context) {
        int appIconRes = -1;
        if (isAppVersionStandard()) {
            appIconRes = R.drawable.ic_launcher;
        } else {
            String appFirstLoadAlis =
                    PreferencesUtils.getString(BaseApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS);
            appIconRes = Res.getDrawableID("ic_launcher_" + appFirstLoadAlis);
        }
        return appIconRes;
    }

    /**
     * 获取app图标资源名称
     *
     * @param context
     * @return
     */
    public static String getAppIconResName(Context context) {
        String appIconResName = "ic_launcher";
        if (!isAppVersionStandard()) {
            String appFirstLoadAlis =
                    PreferencesUtils.getString(BaseApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS);
            appIconResName = "ic_launcher_" + appFirstLoadAlis;
        }
        return appIconResName;
    }

    /**
     * 判断权限集合
     * permissions 权限数组
     * return true-表示没有改权限  false-表示权限已开启
     */
    public static boolean lacksPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (lacksPermission(context, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否缺少权限
     */
    private static boolean lacksPermission(Context mContexts, String permission) {
        return ContextCompat.checkSelfPermission(mContexts, permission) == PackageManager.PERMISSION_DENIED;
    }

    /**
     * 获取是否开启语音转字
     *
     * @return
     */
    public static boolean getIsVoiceWordOpen() {
        return PreferencesByUserAndTanentUtils.getBoolean(BaseApplication.getInstance(),
                Constant.PREF_APP_OPEN_VOICE_WORD_SWITCH, false) && !LanguageManager.getInstance().isAppLanguageEnglish();
    }

    /**
     * 随机生成文件的名称
     *
     * @return
     */
    public static String generalFileName() {
        // TODO Auto-generated method stub
        return UUID.randomUUID().toString();
    }

    /**
     * 判断 悬浮窗口权限是否打开
     *
     * @param context
     * @return true 允许  false禁止
     */
    public static boolean getAppOps(Context context) {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = Integer.valueOf(24);
            arrayOfObject1[1] = Integer.valueOf(Binder.getCallingUid());
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1)).intValue();
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            LogUtils.YfcDebug("判断悬浮窗权限异常：" + e.getMessage());
        }
        return false;
    }

    // /**
    // * 安装apk
    // * @param context
    // * @param apkFilePath
    // */
    // public static void installApk(Context context,String apkFilePath,String apkFileName){
    // File apkFile = new File(apkFilePath, apkFileName);
    // if (!apkFile.exists()) {
    // ToastUtils.show(context, R.string.update_fail);
    // return;
    // }
    // Intent intent =new Intent(Intent.ACTION_VIEW);
    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // //判断是否是AndroidN以及更高的版本
    // if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N) {
    // Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".fileprovider",apkFile);
    // intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    // intent.setDataAndType(contentUri, FileUtils.getMimeType(apkFile));
    // }else{
    // intent.setDataAndType(Uri.fromFile(apkFile),FileUtils.getMimeType(apkFile));
    // }
    // if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
    // context.startActivity(intent);
    // }
    // }

    /**
     * 获取kb或者mb格式的数字
     *
     * @param data
     * @return
     */
    public static String getKBOrMBFormatString(long data) {
        double MBDATA = 1048576.0;
        double KBDATA = 1024.0;
        if (data < KBDATA) {
            return data + "B";
        } else if (data < MBDATA) {
            return new DecimalFormat(("####0.00")).format(data / KBDATA) + "KB";
        } else {
            return new DecimalFormat(("####0.00")).format(data / MBDATA) + "MB";
        }
    }

    /**
     * copy到剪切板
     *
     * @param context
     * @param textView
     */
    public static void copyContentToPasteBoard(Context context, TextView textView) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText(null, textView.getText().toString()));
        ToastUtils.show(context, R.string.copyed_to_paste_board);
    }

    public static void copyContentToPasteBoard(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText(null, content));
        ToastUtils.show(context, R.string.copyed_to_paste_board);
    }

    /**
     * MIUI判断是否有后台弹出窗口的权限，亲测华为mate9，vivo Z1使用这个方法会报内容为null的异常
     * 华为手机没有后台弹出权限，vivo Z1有但是不开没有影响，所以如果不是小米手机直接返回了true
     *
     * @param context
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean canBackgroundStart(Context context) {
        if (getIsXiaoMi()) {
            AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                int op = 10021; // >= 23
                // ops.checkOpNoThrow(op, uid, packageName)
                Method method = ops.getClass().getMethod("checkOpNoThrow", new Class[]
                        {int.class, int.class, String.class}
                );
                Integer result = (Integer) method.invoke(ops, op, Process.myUid(), context.getPackageName());
                return result == AppOpsManager.MODE_ALLOWED;
            } catch (Exception e) {
                LogUtils.YfcDebug("异常：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 小米后台锁屏显示检测方法
     * @param context
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean canShowLockView(Context context) {
        if (getIsXiaoMi()){
            AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);;
            try {
                int op = 10020; // >= 23
                Method method = ops.getClass().getMethod("checkOpNoThrow", new Class[]
                        {int.class, int.class, String.class}
                );
                Integer result = (Integer) method.invoke(ops, op, android.os.Process.myUid(), context.getPackageName());
                return result == AppOpsManager.MODE_ALLOWED;
            } catch (Exception e) {
                LogUtils.YfcDebug("异常：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 判断耳机
     *
     * @return
     */
    public static boolean isHeadsetExists() {
        char[] buffer = new char[1024];
        int newState = 0;
        try {
            FileReader file = new FileReader("/sys/class/switch/h2w/state");
            int len = file.read(buffer, 0, 1024);
            newState = Integer.valueOf((new String(buffer, 0, len)).trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newState != 0;
    }

    /**
     * 是否正在使用电话
     */
    public static boolean isPhoneInUse() {
        try {
            TelephonyManager mTelephonyManager = (TelephonyManager) BaseApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
            int state = mTelephonyManager.getCallState();
            return state != TelephonyManager.CALL_STATE_IDLE;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据规则添加token
     * 当URL主域名是Constant.INSPUR_HOST_URL
     * 或者Constant.INSPURONLINE_HOST_URL结尾时添加token
     * 或者以强哥的特殊路由
     * 或者以路由开始的需要加token
     */
    public static boolean needAuthorizationToken(String url) {
        //检查每一个路由是否，三方应用不传云+token。先判断是否为空字符串，再判断是否是三方url
        WebServiceRouterManager manager = WebServiceRouterManager.getInstance();
        if (url.startsWith(manager.getClusterEcm()) ||
                url.startsWith(manager.getClusterChat()) ||
                url.startsWith(manager.getClusterSchedule()) ||
                url.startsWith(manager.getClusterDistribution()) ||
                url.startsWith(manager.getClusterNews()) ||
                url.startsWith(manager.getClusterCloudDrive()) ||
                url.startsWith(manager.getClusterStorageLegacy()) ||
                url.startsWith(manager.getClusterChatSocket()) ||
                url.startsWith(manager.getClusterEmm()) ||
                url.startsWith(manager.getClusterClientRegistry()) ||
                url.startsWith(manager.getClusterBot())) {
            return true;
        }
        URL urlHost = null;
        try {
            urlHost = new URL(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String urlHostPath = urlHost.getHost();
        return (urlHostPath.endsWith(Constant.INSPUR_HOST_URL)) || urlHostPath.endsWith(Constant.INSPURONLINE_HOST_URL) || urlHost.getPath().endsWith("/app/mdm/v3.0/loadForRegister");
    }

    /**
     * 通知图库更新图片
     *
     * @param context
     * @param filePath
     */
    public static void refreshMedia(Context context, final String filePath) {
        if (AppUtils.getIsXiaoMi()) {
            File file = new File(filePath);
            // 其次把文件插入到系统图库
            try {
                MediaStore.Images.Media.insertImage(context.getContentResolver(),
                        file.getAbsolutePath(), file.getName(), null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // 最后通知图库更新
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } else {
            new MediaScanner(context).scanFile(filePath);
        }


    }

    /**
     * 路径为Picture时通知图库更新图片
     *
     * @param context
     * @param fileCursorPath
     */
    public static String refreshMediaInSystemStorage(Context context, final String fileCursorPath) {
        if (StringUtils.isEmpty(fileCursorPath)) return null;
        ContentResolver cr = context.getContentResolver();
        Uri photoUri = Uri.parse(fileCursorPath);
        String[] projection = new String[]{MediaStore.Audio.Media.DATA};
        Cursor cursor = cr.query(photoUri, projection, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }
        int index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        String photoPath = cursor.getString(index);
        cursor.close();
        if (!StringUtils.isEmpty(photoPath)) {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, photoUri));
            MediaScannerConnection.scanFile(context, new String[]{photoPath}, null, null);
        }
        return photoPath;
    }

    /**
     * 开始推送
     *
     * @param context
     */
    public static void judgeAndStartPush(Context context) {
        if (BaseApplication.getInstance().isHaveLogin() && NotificationSetUtils.isNotificationEnabled(context) &&
                (PreferencesByUserAndTanentUtils.getBoolean(context, Constant.PUSH_SWITCH_FLAG, true))) {
            PushManagerUtils.getInstance().startPush();
        }
    }

    /**
     * 判断定位服务是否开启
     *
     * @param
     * @return true 表示开启
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isLocationEnabled();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !StringUtils.isEmpty(locationProviders);
        }
    }

    /**
     * 直接跳转至位置信息设置界面
     */
    public static void openLocationSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    /**
     * 让用户去打开wifi
     */
    public static void openWifiSetting(Context context) {
        //第一种
//      Intent intent = new Intent();
//      intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
//      startActivity(intent);

        //第二种
//      Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
//      startActivity(wifiSettingsIntent);

        //第三种
//      Intent intent = new Intent();
//      if(android.os.Build.VERSION.SDK_INT >= 11){
//          //Honeycomb
//          intent.setClassName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity");
//       }else{
//          //other versions
//           intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings");
//       }
//       startActivity(intent);
        //第四种
        Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
        context.startActivity(wifiSettingsIntent);
    }

    /**
     * 打开url
     *
     * @param context
     * @param uri
     */
    public static void openUrl(Activity context, String uri) {
        openUrl(context, uri, "  ", true);
    }

    /**
     * 打开url
     *
     * @param context
     * @param uri
     */
    public static void openUrl(Activity context, String uri, String appName, boolean isHaveNavBar) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        LogUtils.jasonDebug("uri===" + uri);
        bundle.putString("appName", appName);
        bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, isHaveNavBar);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
    }

    /**
     * 通过获取应用信息捕获异常来发现是否安装了某个应用
     *
     * @param context
     * @param pkgName
     * @return
     */
    public static boolean checkAppInstalled(Context context, String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (Exception e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;//true为安装了，false为未安装
        }
    }

    /**
     * 通过检查已经安装的应用列表是否包含传入应用包名检查是否安装
     *
     * @param context
     * @param pkgName
     * @return
     */
    public static boolean checkAppInstalledByApplist(Context context, String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> info = packageManager.getInstalledPackages(0);
        if (info == null || info.isEmpty())
            return false;
        for (int i = 0; i < info.size(); i++) {
            if (pkgName.equals(info.get(i).packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据包名获取应用名
     *
     * @param context
     * @param packageName
     * @return
     */
    public static String getApplicationNameByPackageName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        String name;
        try {
            name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            name = "";
        }
        return name;
    }


}
