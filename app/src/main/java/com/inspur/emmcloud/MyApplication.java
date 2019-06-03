package com.inspur.emmcloud;

import static com.inspur.emmcloud.config.MyAppConfig.LOCAL_CACHE_MARKDOWN_PATH;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.xutils.x;
import org.xutils.http.RequestParams;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.beefe.picker.PickerViewPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.github.zafarkhaja.semver.Version;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastBlackStyle;
import com.horcrux.svg.SvgPackage;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.bean.mine.Language;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.interf.MyActivityLifecycleCallbacks;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.richtext.RichText;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.CrashHandler;
import com.inspur.emmcloud.util.privates.ECMShortcutBadgeNumberManagerUtils;
import com.inspur.emmcloud.util.privates.LanguageUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.privates.PushManagerUtils;
import com.inspur.emmcloud.util.privates.ScheduleAlertUtils;
import com.inspur.emmcloud.util.privates.WebServiceRouterManager;
import com.inspur.emmcloud.util.privates.cache.DbCacheUtils;
import com.inspur.emmcloud.widget.CustomImageDownloader;
import com.inspur.imp.api.Res;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;
import com.oblador.vectoricons.VectorIconsPackage;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;


/**
 * Application class
 */
public class MyApplication extends MultiDexApplication implements ReactApplication {
    private static final String TAG = "MyApplication";
    private static boolean isContactReady = false;
    private static MyApplication instance;
    /**
     * ReactNative相关代码
     */
    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return com.facebook.react.BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.asList(
                    new MainReactPackage(),
                    new AuthorizationManagerPackage(),
                    new PickerViewPackage(),
                    new SvgPackage(),
                    new VectorIconsPackage()
            );
        }
    };
    private List<Activity> activityList = new LinkedList<Activity>();
    private boolean isIndexActivityRunning = false;
    private boolean isActive = false;
    private String uid;
    private String accessToken;
    private String refreshToken;
    private Enterprise currentEnterprise;
    private Map<String, String> userPhotoUrlMap = new LinkedHashMap<>();
    private MyActivityLifecycleCallbacks myActivityLifecycleCallbacks;
    private boolean isOpenNotification = false;
    private String tanent;

    private String currentChannelCid = "";
    private boolean isEnterSystemUI = false;  //是否进入第三方系统界面，判断app前后台

    /**
     * 单例获取application实例
     *
     * @return MyApplication
     */
    public static MyApplication getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        init();
        LogUtils.isDebug = AppUtils.isApkDebugable(getInstance());
        setAppLanguageAndFontScale();
        removeAllSessionCookie();
        myActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
        registerActivityLifecycleCallbacks(myActivityLifecycleCallbacks);
        WebSocketPush.getInstance().startWebSocket();
    }


    private void init() {
        // TODO Auto-generated method stub
        instance = this;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getInstance());
        x.Ext.init(MyApplication.this);
        x.Ext.setDebug(true);
        SoLoader.init(this, false);//ReactNative相关初始化
        Res.init(this); // 注册imp的资源文件类
        initImageLoader();
        initTanent();
        RichText.initCacheDir(new File(LOCAL_CACHE_MARKDOWN_PATH));
        RichText.debugMode = true;
        userPhotoUrlMap = new LinkedHashMap<String, String>() {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<String, String> eldest) {
                // TODO Auto-generated method stub
                return size() > 40;

            }
        };
        PushManagerUtils.getInstance().clearPushFlag();
        isActive = false;
        isContactReady = PreferencesUtils.getBoolean(getInstance(),
                "isContactReady", false);
        uid = PreferencesUtils.getString(getInstance(), "userID");
        accessToken = PreferencesUtils.getString(getInstance(), "accessToken", "");
        refreshToken = PreferencesUtils.getString(getInstance(), "refreshToken", "");
        //科大讯飞语音SDK初始化
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5a6001bf");
        ToastUtils.init(this,new ToastBlackStyle());
        ToastUtils.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM,0,0);
    }

    /**************************************登出逻辑相关********************************************************/
    /**
     * 注销
     */
    public void signout() {
        // TODO Auto-generated method stub
        //清除日历提醒极光推送本地通知
        ScheduleAlertUtils.cancelAllCalEventNotification(this);
        PushManagerUtils.getInstance().stopPush();
        clearNotification();
        removeAllCookie();
        removeAllSessionCookie();
        clearUserPhotoMap();
        cancelToken();
        PreferencesUtils.putString(this, "accessToken", "");
        PreferencesUtils.putString(this, "refreshToken", "");
        setAccessToken("");
        setRefreshToken("");
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        WebSocketPush.getInstance().webSocketSignout();
        ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(getInstance(), 0);
    }
/****************************通知相关（极光和华为推送）******************************************/

    /**
     * 退出登录时注销token
     * 无后续需要根据返回内容
     */
    private void cancelToken() {
        AppAPIService appAPIService = new AppAPIService(this);
        appAPIService.cancelToken();
    }


/************************ Cookie相关 *****************************/

    /**
     * 清除所有的SessionCookie
     */
    public void removeAllSessionCookie() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeSessionCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr =
                    CookieSyncManager.createInstance(getInstance());
            CookieManager.getInstance().removeSessionCookie();
        }
    }

    public void removeAllCookie() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr =
                    CookieSyncManager.createInstance(getInstance());
            CookieManager.getInstance().removeAllCookie();
        }
    }

    /************************ UID相关 *****************************/
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }


    /*************************** http相关 **************************************/



    /**
     * 获取http RequestParams
     *
     * @param url
     * @return
     */
    public RequestParams getHttpRequestParams(String url) {
        RequestParams params = new RequestParams(url);
        String versionValue = AppUtils.getVersion(getInstance());
        try {
            Version version = Version.valueOf(versionValue);
            versionValue = version.getNormalVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }

        params.addHeader(
                "User-Agent",
                "Android/" + AppUtils.getReleaseVersion() + "("
                        + AppUtils.GetChangShang() + " " + AppUtils.GetModel()
                        + ") " + "CloudPlus_Phone/"
                        + versionValue);
        params.addHeader("X-Device-ID",
                AppUtils.getMyUUID(getInstance()));
        params.addHeader("Accept", "application/json");
        params.addHeader("Connect", "close");
        if (getToken() != null) {
            params.addHeader("Authorization", getToken());
        }
        if (currentEnterprise != null) {
            params.addHeader("X-ECC-Current-Enterprise", currentEnterprise.getId());
        }
        String languageJson = PreferencesUtils.getString(
                getInstance(), MyApplication.getInstance().getTanent() + "appLanguageObj");
        if (languageJson != null) {
            Language language = new Language(languageJson);
            params.addHeader("Accept-Language", language.getIana());
        }
        return params;
    }

    public boolean getIsContactReady() {
        return isContactReady;
    }

    /******************************通讯录相关***************************************/

    public void setIsContactReady(boolean isContactReady) {
        MyApplication.isContactReady = isContactReady;
        PreferencesUtils.putBoolean(getInstance(), "isContactReady",
                isContactReady);
    }

    public boolean getIsActive() {
        return isActive;
    }

    /***
     * 设置应用是否在前台flag
     *
     * @param isActive
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
        WebSocketPush.getInstance().sendAppStatus();
        clearNotification();
    }

    /*************************** Oauth认证 **************************************/

    public String getToken() {
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }
        return "Bearer" + " " + accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    /***************************** db相关 *******************************************/

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * 重启所有的数据库
     */
    public void restartAllDb() {
        // TODO Auto-generated method stub
        DbCacheUtils.closeDb(getInstance());
        DbCacheUtils.initDb(getInstance());
    }


    /**
     * 删除此用户在此实例的所有db
     */
    public void deleteAllDb() {
        DbCacheUtils.deleteDb(getInstance());
    }

    /******************************租户信息*******************************************/

    public void initTanent() {
        // TODO Auto-generated method stub
        // UriUtils.res = "res_dev";
        currentEnterprise = null;
        String myInfo = PreferencesUtils.getString(getInstance(), "myInfo");
        if (!StringUtils.isBlank(myInfo)) {
            GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
            String currentEnterpriseId = PreferencesByUsersUtils.getString(getInstance(), "current_enterprise_id");
            List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
            if (!StringUtils.isBlank(currentEnterpriseId)) {
                for (int i = 0; i < enterpriseList.size(); i++) {
                    Enterprise enterprise = enterpriseList.get(i);
                    if (enterprise.getId().equals(currentEnterpriseId)) {
                        currentEnterprise = enterprise;
                        break;
                    }
                }
            }
            if (currentEnterprise == null) {
                currentEnterprise = getMyInfoResult.getDefaultEnterprise();
            }
            if (currentEnterprise == null && enterpriseList.size() > 0) {
                currentEnterprise = enterpriseList.get(0);
            }
            WebServiceRouterManager.getInstance().setWebServiceRouter(currentEnterprise);
            tanent = currentEnterprise.getCode();
        }
    }




    public String getTanent() {
        return tanent;
    }

    public Enterprise getCurrentEnterprise() {
        return currentEnterprise;
    }

    /*****************************通讯录头像缓存********************************************/
    public String getUserPhotoUrl(String uid) {
        String photoUrl = null;
        if (!StringUtils.isBlank(uid)) {
            photoUrl = userPhotoUrlMap.get(uid);
        }
        return photoUrl;
    }

    public boolean isKeysContainUid(String uid) {
        if (!StringUtils.isBlank(uid)) {
            return userPhotoUrlMap.containsKey(uid);
        }
        return false;
    }

    public void setUsesrPhotoUrl(String uid, String url) {
        if (!StringUtils.isBlank(uid) && !StringUtils.isBlank(url)) {
            userPhotoUrlMap.put(uid, url);
        }
    }

    public void clearUserPhotoMap() {
        userPhotoUrlMap.clear();
    }

    /*************************************************************************/

    public void clearUserPhotoUrl(String uid) {
        if (!StringUtils.isBlank(uid) && userPhotoUrlMap.containsKey(uid)) {
            userPhotoUrlMap.remove(uid);
        }
    }

/**************************************************************************/

    /***
     * 判断当前版本是否是开发版
     *
     * @return
     */
    public boolean isVersionDev() {
        ApplicationInfo appInfo;
        try {
            appInfo = this.getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            String msg = appInfo.metaData.getString("VERSION_TYPE");
            if (msg.equals("dev")) {
                return true;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;

    }

    /**
     * 判断是否已登录
     *
     * @return
     */
    public boolean isHaveLogin() {
        String accessToken = PreferencesUtils.getString(this,
                "accessToken", "");
        String myInfo = PreferencesUtils.getString(getInstance(),
                "myInfo", "");
        boolean isMDMStatusPass = PreferencesUtils.getBoolean(getInstance(), "isMDMStatusPass", true);
        return (!StringUtils.isBlank(accessToken) && !StringUtils.isBlank(myInfo) && isMDMStatusPass);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        // TODO Auto-generated method stub
        if (config != null) {
            super.onConfigurationChanged(config);
        }
        setAppLanguageAndFontScale();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageUtils.attachBaseContext(newBase));
    }

    /**
     * 设置App的语言
     */
    public void setAppLanguageAndFontScale() {

        String languageJson = PreferencesUtils
                .getString(getInstance(), MyApplication.getInstance().getTanent()
                        + "appLanguageObj");
        Configuration config = getResources().getConfiguration();
        if (languageJson != null) {
            String language = PreferencesUtils.getString(
                    getInstance(), MyApplication.getInstance().getTanent() + "language");
            // 当系统语言选择为跟随系统的时候，要检查当前系统的语言是不是在commonList中，重新赋值
            if (language.equals("followSys")) {
                String commonLanguageListJson = PreferencesUtils.getString(
                        getInstance(), MyApplication.getInstance().getTanent()
                                + "commonLanguageList");
                if (commonLanguageListJson != null) {
                    List<Language> commonLanguageList = JSONUtils
                            .parseArray(commonLanguageListJson,
                                    Language.class);
                    boolean isContainDefault = false;
                    for (int i = 0; i < commonLanguageList.size(); i++) {
                        Language commonLanguage = commonLanguageList.get(i);
                        if (commonLanguage.getIso().contains(
                                Resources.getSystem().getConfiguration().locale.getCountry())) {
                            PreferencesUtils.putString(
                                    getInstance(),
                                    MyApplication.getInstance().getTanent() + "appLanguageObj",
                                    commonLanguage.toString());
                            languageJson = commonLanguage.toString();
                            isContainDefault = true;
                            break;
                        }
                    }
                    if (!isContainDefault) {
                        PreferencesUtils.putString(getInstance(),
                                MyApplication.getInstance().getTanent() + "appLanguageObj",
                                commonLanguageList.get(0).toString());
                        languageJson = commonLanguageList.get(0).toString();
                    }
                }

            }
            PreferencesUtils.putString(getInstance(), Constant.PREF_LAST_LANGUAGE, languageJson);
            // 将iso字符串分割成系统的设置语言
            String[] array = new Language(languageJson).getIso().split("-");
            String country = "";
            String variant = "";
            try {
                country = array[0];
                variant = array[1];
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            Locale locale = new Locale(country, variant);
            Locale.setDefault(locale);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLocale(locale);
            } else {
                config.locale = locale;
            }
        }
        config.fontScale = 1.0f;
        getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());

    }

    public MyActivityLifecycleCallbacks getActivityLifecycleCallbacks() {
        return myActivityLifecycleCallbacks;
    }

    /**
     * startWebSocket ImageLoaderCommon
     **/
    private void initImageLoader() {
        // TODO Auto-generated method stub
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                getInstance())
                .memoryCacheExtraOptions(1280, 1280)
                .defaultDisplayImageOptions(options)
                .imageDownloader(
                        new CustomImageDownloader(getApplicationContext()))
                .threadPoolSize(6)
                .threadPriority(Thread.NORM_PRIORITY - 1)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(
                        new UsingFreqLimitedMemoryCache(3 * 1024 * 1024))
                .diskCacheSize(100 * 1024 * 1024)
                // You can pass your own memory cache implementation
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File cacheDir = new File(MyAppConfig.LOCAL_CACHE_PATH);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            builder = builder.diskCache(new UnlimitedDiskCache(cacheDir));
        }

        ImageLoaderConfiguration config = builder.build();
        L.writeLogs(false); // 关闭imageloader的疯狂的log
        ImageLoader.getInstance().init(config);

    }

    /**
     * 添加桌面快捷方式
     **/
    public void addShortCut(Context context) {
        Intent shortcutIntent = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");
        // 不允许重复创建
        shortcutIntent.putExtra("duplicate", false);
        // 快捷方式下的名字
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                context.getString(R.string.app_name));
        // 快捷方式的图标
        Parcelable icon = null;
        if (isVersionDev()) {
            icon = Intent.ShortcutIconResource.fromContext(context,
                    R.drawable.ic_launcher_dev);
        } else {
            icon = Intent.ShortcutIconResource.fromContext(context,
                    R.drawable.ic_launcher);
        }

        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        Intent intent = new Intent(context, MainActivity.class);
        // 卸载应用的时候删除桌面图标
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        // 绑定事件
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        LocalBroadcastManager.getInstance(context).sendBroadcast(shortcutIntent);
    }

    // 判断IndexActivity是否存在的标志
    public boolean isIndexActivityRunning() {
        return isIndexActivityRunning;
    }

    public void setIndexActvityRunning(boolean running) {
        isIndexActivityRunning = running;
    }

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    public List<Activity> getActivityList() {
        return activityList;
    }

    /**
     * 获取是否正在打开通知
     *
     * @return
     */
    public boolean getOPenNotification() {
        return isOpenNotification;
    }

    /**
     * 设置是否正在打开通知
     *
     * @param isOpenNotification
     */
    public void setOpenNotification(boolean isOpenNotification) {
        this.isOpenNotification = isOpenNotification;
    }

    /**
     * exit Activity
     **/
    public void exit() {
        try {
            for (Activity activity : activityList) {
                if (activity != null)
                    activity.finish();
            }
            activityList.clear();
        } catch (Exception e) {
            LogUtils.exceptionDebug(TAG, e.toString());
        }
        setIsActive(false);
    }

    /**
     * 清除目标之外的Activity
     *
     * @param targetActivity
     */
    public void closeOtherActivity(Activity targetActivity) {
        try {
            for (Activity activity : activityList) {
                if (activity != targetActivity) {
                    activity.finish();
                }
            }
        } catch (Exception e) {
            LogUtils.exceptionDebug(TAG, e.toString());
        }
    }

    /**
     * 清除除了指定名称之外的Activity
     */
    public void closeOtherActivity(String activityName) {
        try {
            for (Activity activity : activityList) {
                if (!activity.getClass().getSimpleName().equals(activityName)) {
                    activity.finish();
                }
            }
        } catch (Exception e) {
            LogUtils.exceptionDebug(TAG, e.toString());
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    /**
     * 清除app所有的通知
     */
    public void clearNotification() {
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    /**
     * ReactNative相关代码
     *
     * @return
     */
    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    /****************************标记当前正在某个频道中***************************************************/
    public String getCurrentChannelCid() {
        return currentChannelCid;
    }

    public void setCurrentChannelCid(String currentChannelCid) {
        this.currentChannelCid = currentChannelCid;
    }

    /*****************************是否进入第三方系统界面，判断app前后台***********************************************/
    public boolean isEnterSystemUI() {
        return isEnterSystemUI;
    }

    public void setEnterSystemUI(boolean enterSystemUI) {
        isEnterSystemUI = enterSystemUI;
    }
}
