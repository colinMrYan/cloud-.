package com.inspur.emmcloud.util.privates;

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
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.mine.Language;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.chat.DisplayMediaVoiceMsg;
import com.inspur.emmcloud.ui.chat.MembersActivity;
import com.inspur.emmcloud.util.common.EncryptUtils;
import com.inspur.emmcloud.util.common.FileUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ResolutionUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.api.Res;
import com.inspur.imp.plugin.camera.imagepicker.ImagePicker;
import com.inspur.imp.plugin.camera.imagepicker.ui.ImageGridActivity;
import com.inspur.imp.plugin.camera.mycamera.MyCameraActivity;

import java.io.File;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
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
     * @param uid
     * @return
     */
    public static String getNetSpeed(int uid) {
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);;
        long nowTimeStamp = System.currentTimeMillis();
        long divide = nowTimeStamp - lastTimeStamp;
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (divide == 0? 1:divide));//毫秒转换
        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        return String.valueOf(speed) + " kb/s";
    }

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
     * 获取当前应用语言
     * @param context
     * @return
     */
    public static String getCurrentAppLanguage(Context context){
        String languageJson = PreferencesUtils.getString(
                context, MyApplication.getInstance().getTanent() + "appLanguageObj");
        if (languageJson != null) {
            Language language = new Language(languageJson);
            return language.getIana();
        }
        return "zh-Hans";
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
     * 获取应用包名
     * @param context
     * @return
     */
    public static String getPackageName(Context context){
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
        String savedVersion = PreferencesUtils.getString(context,
                "previousVersion", "");
        String currentVersion = getVersion(context);
        if (!StringUtils.isBlank(savedVersion)) {
            String[] savedArray = savedVersion.split("\\.");
            String[] currentArray = currentVersion.split("\\.");
            try {
                String saveVersionCode = getNormalVersionCode(savedArray[2]);
                savedArray[2] = saveVersionCode;
                String currentVersionCode = getNormalVersionCode(currentArray[2]);
                currentArray[2] = currentVersionCode;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (savedArray.length == 3 && currentArray.length == 3) {
                int savedVersionNum = Integer.parseInt(savedArray[0]) * 1000000 + Integer.parseInt(savedArray[1]) * 1000 + Integer.parseInt(savedArray[2]);
                int currentVersionNum = Integer.parseInt(currentArray[0]) * 1000000 + Integer.parseInt(currentArray[1]) * 1000 + Integer.parseInt(currentArray[2]);
                return currentVersionNum > savedVersionNum;
            }
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
        String uuid = getUUID(context);
        saveUUID(context, uuid);
        return uuid;
    }

    /**
     * 获取随机数
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
        //先读SharePreference，如果SharePreference里有UUID则返回SharePreference里的UUID，并判断SD卡里是否存在UUID文件，没有则存一份
        String uniqueId = PreferencesUtils.getString(context, "device_uuid", "");
        if (!StringUtils.isBlank(uniqueId)) {
            return uniqueId;
        }
        //如果SharePreference里没有，则检查SD卡里有没有UUID文件，如果有则返回sd卡里的UUID，并向SharePreference里存一份
        uniqueId = getUUIDFromSDCardFile(context);
        if (!StringUtils.isBlank(uniqueId)) {
            return uniqueId;
        }
        //如果前两个都没有，则生成一个UUID，并存到SharePreference和SD卡文件里
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
        return AppUtils.GetChangShang().toLowerCase().startsWith("huawei");
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
        if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            context.startActivityForResult(intent, ImpActivity.DO_NOTHING_RESULTCODE);
        }
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
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
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
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);

    }


    /**
     * 调用文件系统
     */
    public static void openFileSystem(Activity activity, int requestCode) {
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
    public static void openGallery(Activity activity, int limit, int requestCode) {
        initImagePicker(limit);
        Intent intent = new Intent(activity,
                ImageGridActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 根据channelId打开相应的频道
     * @param activity
     * @param channelId
     */
    public static void openChannelMemeberSelect(Activity activity,String channelId,int requestCode){
        Intent intent = new Intent();
        intent.setClass(activity, MembersActivity.class);
        intent.putExtra("title",activity.getString(R.string.voice_communication_choice_members));
        intent.putExtra(MembersActivity.MEMBER_PAGE_STATE, MembersActivity.SELECT_STATE);
        intent.putExtra("cid",channelId);
        activity.startActivity(intent);
    }

    /**
     * 调用系统相机
     *
     * @param activity
     * @param fileName
     * @param requestCode
     */
    public static void openCamera(Activity activity, String fileName, int requestCode) {
        // 判断存储卡是否可以用，可用进行存储
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File appDir = new File(Environment.getExternalStorageDirectory(),
                    "DCIM");
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            Intent intent = new Intent();
            intent.putExtra(MyCameraActivity.EXTRA_PHOTO_DIRECTORY_PATH, appDir.getAbsolutePath());
            intent.putExtra(MyCameraActivity.EXTRA_PHOTO_NAME,fileName);
            intent.setClass(activity,MyCameraActivity.class);
            activity.startActivityForResult(intent, requestCode);
        } else {
            ToastUtils.show(activity, R.string.filetransfer_sd_not_exist);
        }
    }

    /**
     * 发短信
     * @param activity
     * @param phoneNum
     * @param requestCode
     */
    public static void sendSMS(Activity activity,String phoneNum,int requestCode){
        Uri smsToUri = Uri.parse("smsto:" + phoneNum);
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        activity.startActivityForResult(intent, requestCode);

    }

    /**
     * 打电话
     * @param activity
     * @param phoneNum
     * @param requestCode
     */
    public static void call(Activity activity,String phoneNum,int requestCode){
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                + phoneNum));
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 发邮件
     * @param activity
     * @param mail
     * @param requestCode
     */
    public static void sendMail(Activity activity,String mail,int requestCode){
        Uri uri = Uri.parse("mailto:" + mail);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        activity.startActivityForResult(Intent.createChooser(intent,
                activity.getString(R.string.please_select_app_of_mail)), 1);
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
    private static String getDeviceUUID(Context context) {
        final TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
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
     * 通过厂商确定pushId
     *
     * @return
     */
    public static String getPushId(Context context) {
        String pushId = "";
        if (AppUtils.getIsHuaWei() && canConnectHuawei(context)) {
            //需要对华为单独推送的时候解开这里
            String hwtoken = PreferencesUtils.getString(context, "huawei_push_token", "");
            if (!StringUtils.isBlank(hwtoken)) {
                pushId = hwtoken + "@push.huawei.com";
            }
        } else {
            pushId = PreferencesUtils.getString(context, "JpushRegId", "");
        }
        if (StringUtils.isBlank(pushId)){
            pushId = "UNKNOWN";
        }
        return pushId;
    }

    /**
     * 获取pushProvider
     *
     * @param context
     * @return
     */
    public static String getPushProvider(Context context) {
        // 华为  com.hicloud.push
        // 极光  cn.jpush
        // 小米  com.xiaomi.xmpush
        // 魅族  com.meizu.api - push
        String pushProvider = "";
        String pushFlag = PreferencesUtils.getString(context, "pushFlag", "");
        switch (pushFlag) {
            case "huawei":
                pushProvider = "com.hicloud.push";
                break;
            case "xiaomi":
                pushProvider = "com.xiaomi.xmpush";
                break;
            case "meizu":
                pushProvider = "com.meizu.api-push";
                break;
            default:
                pushProvider = "cn.jpush";
                break;
        }
        return pushProvider;
    }

    /**
     * 判断是否可以连接华为推了送
     *
     * @return
     */
    private static boolean canConnectHuawei(Context context) {
        String pushFlag = PreferencesUtils.getString(context, "pushFlag", "");
        return StringUtils.isBlank(pushFlag) || pushFlag.equals("huawei");
    }


    /**
     * 设置添加屏幕的背景透明度
     *
     * @param activity
     * @param bgAlpha
     */
    public static void setWindowBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        activity.getWindow().setAttributes(lp);
    }

    /**
     * app是否是标准版
     *
     * @return
     */
    public static boolean isAppVersionStandard() {
        boolean isAppVersionStandard = true;
        String appFirstLoadAlis = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS);
        if (appFirstLoadAlis != null && !appFirstLoadAlis.equals("Standard")){
            isAppVersionStandard = false;
        }
        return isAppVersionStandard;

    }

    /**
     * 获取版本名
     * @param context
     * @return
     */
    public static String getAppVersionFlag(Context context){
        return getManifestMetadata(context,"FLAG_APP_VERSION_TYPE");
    }

    /**
     * 获取manifest中的metadata值
     * @param context
     * @param key
     * @return
     */
    public static String getManifestMetadata(Context context,String key){
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
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
    public static String getAppName(Context context)
    {
        try
        {
            int appNameRes = -1;
            if (isAppVersionStandard()){
                appNameRes = R.string.app_name;
            }else {
                String appFirstLoadAlis = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS);
                appNameRes = Res.getStringID("app_name_"+appFirstLoadAlis);
            }
            return context.getResources().getString(appNameRes);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return context.getResources().getString(R.string.app_name);
    }

    /**
     * 获取app图标
     * @param context
     * @return
     */
    public static int getAppIconRes(Context context){
        int appIconRes = -1;
        if (isAppVersionStandard()){
            appIconRes = R.drawable.ic_launcher;
        }else {
            String appFirstLoadAlis = PreferencesUtils.getString(MyApplication.getInstance(), Constant.PREF_APP_LOAD_ALIAS);
            appIconRes = Res.getDrawableID("ic_launcher_"+appFirstLoadAlis);
        }
        return appIconRes;
    }

    /**
     * 判断权限集合
     * permissions 权限数组
     * return true-表示没有改权限  false-表示权限已开启
     */
    public static boolean lacksPermissions(Context context,String[] permissions) {
        for (String permission : permissions) {
            if (lacksPermission(context,permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否缺少权限
     */
    private static boolean lacksPermission(Context mContexts, String permission) {
        return ContextCompat.checkSelfPermission(mContexts, permission) ==
                PackageManager.PERMISSION_DENIED;
    }

    /**
     * 获取是否开启语音转字
     * @return
     */
    public static boolean getIsVoiceWordOpen(){
        return PreferencesByUserAndTanentUtils.getBoolean(MyApplication.getInstance(), Constant.PREF_APP_OPEN_VOICE_WORD_SWITCH,DisplayMediaVoiceMsg.IS_VOICE_WORD_OPEN) == DisplayMediaVoiceMsg.IS_VOICE_WORD_OPEN;
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
            LogUtils.YfcDebug("判断悬浮窗权限异常："+e.getMessage());
        }
        return false;
    }

    /**
     * 安装apk
     * @param context
     * @param apkFilePath
     */
    public static void installApk(Context context,String apkFilePath,String apkFileName){
        File apkFile = new File(apkFilePath, apkFileName);
        if (!apkFile.exists()) {
            ToastUtils.show(context, R.string.update_fail);
            return;
        }
        // 通过Intent安装APK文件
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 更新后启动
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkFile.toString()),
                "application/vnd.android.package-archive");
        if (context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * 获取kb或者mb格式的数字
     * @param data
     * @return
     */
    public static String getKBOrMBFormatString(long data){
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
     * @param context
     * @param textView
     */
    public static void copyContentToPasteBoard(Context context, TextView textView) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText(null, textView.getText().toString()));
        ToastUtils.show(context, R.string.copyed_to_paste_board);
    }
}
