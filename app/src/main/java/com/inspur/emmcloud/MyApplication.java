package com.inspur.emmcloud;

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

import com.beefe.picker.PickerViewPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.horcrux.svg.SvgPackage;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
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
import com.inspur.emmcloud.util.privates.CalEventNotificationUtils;
import com.inspur.emmcloud.util.privates.CrashHandler;
import com.inspur.emmcloud.util.privates.ECMShortcutBadgeNumberManagerUtils;
import com.inspur.emmcloud.util.privates.HuaWeiPushMangerUtils;
import com.inspur.emmcloud.util.privates.MutilClusterUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
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

import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

import static com.inspur.emmcloud.config.MyAppConfig.LOCAL_CACHE_MARKDOWN_PATH;


/**
 * Application class
 */
public class MyApplication extends MultiDexApplication implements ReactApplication {
    private static final String TAG = "MyApplication";
    private List<Activity> activityList = new LinkedList<Activity>();
    private boolean isIndexActivityRunning = false;
    private boolean isActive = false;
    private static boolean isContactReady = false;
    private String uid;
    private String accessToken;
    private String refreshToken;
    private Enterprise currentEnterprise;
    private Map<String, String> userPhotoUrlMap;
    private static MyApplication instance;
    private MyActivityLifecycleCallbacks myActivityLifecycleCallbacks;
    private boolean isOpenNotification = false;
    private String tanent;
    private String clusterEcm = "";//多云ecm服务
    private String clusterChat = "";
    private String clusterSchedule = "";
    private String clusterDistribution = "";
    private String clusterNews = "";
    private String clusterCloudDrive = "";
    private String clusterStorageLegacy = "";
    private String socketPath = "";
    private String clusterChatVersion = "";//仅标识chat的version
    private String clusterChatSocket = "";
    private String clusterEmm = Constant.DEFAULT_CLUSTER_EMM;//多云emm服务
    private String clusterClientRegistry = "";
    private String clusterScheduleVersion = "";//仅标识Schedule
    private String clusterBot = "";

    public void onCreate() {
        super.onCreate();
        init();
        setAppLanguageAndFontScale();
        removeAllSessionCookie();
        myActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
        registerActivityLifecycleCallbacks(myActivityLifecycleCallbacks);
    }

    public String getCloudId() {
        String clusterId = PreferencesUtils.getString(this, "cloud_idm", Constant.DEFAULT_CLUSTER_ID);
        return StringUtils.isBlank(clusterId) ? Constant.DEFAULT_CLUSTER_ID : clusterId;
    }


    private void init() {
        // TODO Auto-generated method stub
        instance = this;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getInstance());
        x.Ext.init(MyApplication.this);
        x.Ext.setDebug(false);
        SoLoader.init(this, false);//ReactNative相关初始化
        Res.init(this); // 注册imp的资源文件类
        initImageLoader();
        initTanent();
        RichText.initCacheDir(new File(LOCAL_CACHE_MARKDOWN_PATH));
        userPhotoUrlMap = new LinkedHashMap<String, String>() {
            @Override
            protected boolean removeEldestEntry(Entry<String, String> eldest) {
                // TODO Auto-generated method stub
                return size() > 40;

            }
        };
        PreferencesUtils.putString(this, "pushFlag", "");
        isActive = false;
        isContactReady = PreferencesUtils.getBoolean(getInstance(),
                "isContactReady", false);
        uid = PreferencesUtils.getString(getInstance(), "userID");
        accessToken = PreferencesUtils.getString(getInstance(), "accessToken", "");
        refreshToken = PreferencesUtils.getString(getInstance(), "refreshToken", "");
        //科大讯飞语音SDK初始化
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5a6001bf");
    }


    /**
     * 单例获取application实例
     *
     * @return MyApplication
     */
    public static MyApplication getInstance() {
        return instance;
    }


    /**************************************登出逻辑相关********************************************************/
    public void signout(){
        signout(true);
    }

    /**
     * 注销
     * @param isWebSocketSignout 是否在此处处理websocket的注销
     */
    public void signout(boolean isWebSocketSignout) {
        // TODO Auto-generated method stub
        if (isWebSocketSignout){
            WebSocketPush.getInstance().webSocketSignout();
        }
        //清除日历提醒极光推送本地通知
        CalEventNotificationUtils.cancelAllCalEventNotification(this);
        stopPush();
        clearNotification();
        removeAllCookie();
        removeAllSessionCookie();
        clearUserPhotoMap();
        PreferencesUtils.putString(this, "accessToken", "");
        PreferencesUtils.putString(this, "refreshToken", "");
        setAccessToken("");
        setRefreshToken("");
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        ECMShortcutBadgeNumberManagerUtils.setDesktopBadgeNumber(getInstance(), 0);
    }
/****************************通知相关（极光和华为推送）******************************************/
    /**
     * 初始化推送，以后如需定制小米等厂家的推送服务可从这里定制
     */
    public void startPush() {
        if (AppUtils.getIsHuaWei() && canConnectHuawei()) {
            HuaWeiPushMangerUtils.getInstance(this).connect();
        } else {
            startJPush();
        }
    }

    /**
     * 开启极光推送
     */
    public void startJPush() {
        // 初始化 JPush
        JPushInterface.init(this);
        if (JPushInterface.isPushStopped(this)) {
            JPushInterface.resumePush(this);
        }
        // 设置开启日志,发布时请关闭日志
        JPushInterface.setDebugMode(true);
    }

    /**
     * 判断是否可以连接华为推了送
     *
     * @return
     */
    private boolean canConnectHuawei() {
        String pushFlag = PreferencesUtils.getString(this, "pushFlag", "");
        return (StringUtils.isBlank(pushFlag) || pushFlag.equals("huawei"));
    }

    /**
     * 关闭推送
     */
    public void stopPush() {
        if (AppUtils.getIsHuaWei() && canConnectHuawei()) {
            HuaWeiPushMangerUtils.getInstance(this).stopPush();
        } else {
            JPushInterface.stopPush(this);
        }
        //清除日历提醒极光推送本地通知
        CalEventNotificationUtils.cancelAllCalEventNotification(getInstance());
    }


/************************ Cookie相关 *****************************/
    /**
     * 清除所有的SessionCookie
     */
    private void removeAllSessionCookie() {
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
        params.addHeader(
                "User-Agent",
                "Android/" + AppUtils.getReleaseVersion() + "("
                        + AppUtils.GetChangShang() + " " + AppUtils.GetModel()
                        + ") " + "CloudPlus_Phone/"
                        + AppUtils.getVersion(getInstance()));
        params.addHeader("X-Device-ID",
                AppUtils.getMyUUID(getInstance()));
        params.addHeader("Accept", "application/json");
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

    /******************************通讯录相关***************************************/

    public void setIsContactReady(boolean isContactReady) {
        MyApplication.isContactReady = isContactReady;
        PreferencesUtils.putBoolean(getInstance(), "isContactReady",
                isContactReady);
    }

    public boolean getIsContactReady() {
        return isContactReady;
    }

    /***
     * 设置应用是否在前台flag
     *
     * @param isActive
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
        WebSocketPush.getInstance().sendAppStatus(isActive);
        clearNotification();
    }

    public boolean getIsActive() {
        return isActive;
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

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }


    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /***************************** db相关 *******************************************/
    /**
     * 重启所有的数据库
     */
    public void restartAllDb() {
        // TODO Auto-generated method stub
        DbCacheUtils.closeDb(getInstance());
        DbCacheUtils.closeDb(getInstance());
        DbCacheUtils.initDb(getInstance());
    }

    /**
     * 删除此用户在此实例的所有db
     */
    public void deleteAllDb() {
        DbCacheUtils.deleteDb(getInstance());
    }

//    /******************************Websocket********************************************/
//
//    /**
//     * 开启websocket推送
//     */
//    public void startWebSocket(boolean isForceNew) {
//        if (isHaveLogin()) {
//            WebSocketPush.getInstance().startWebSocket(isForceNew);
//        }
//    }


    /******************************租户信息*******************************************/

    public void initTanent() {
        // TODO Auto-generated method stub
        // UriUtils.res = "res_dev";
        currentEnterprise = null;
        String myInfo = PreferencesUtils.getString(getInstance(),"myInfo");
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
            if (currentEnterprise == null && enterpriseList.size()>0 ) {
                currentEnterprise = enterpriseList.get(0);
            }
            MutilClusterUtils.setClusterBaseUrl(currentEnterprise);
            tanent = currentEnterprise.getCode();
        }
    }

    /**
     * 获取ecm云
     *
     * @return
     */
    public String getClusterEcm() {
        return clusterEcm;
    }

    /**
     * 设置ecm云
     *
     * @param clusterEcm
     */
    public void setClusterEcm(String clusterEcm) {
        this.clusterEcm = clusterEcm;
    }

    /**
     * 获取emm云
     *
     * @return
     */
    public String getClusterEmm() {
        return clusterEmm;
    }

    /**
     * 设置emm云
     *
     * @return
     */
    public void setClusterEmm(String clusterEmm) {
        this.clusterEmm = clusterEmm;
    }

    /**
     * 沟通相关
     * @return
     */
    public String getClusterChat() {
        return clusterChat;
    }

    public void setClusterChat(String clusterChat) {
        this.clusterChat = clusterChat;
    }

    public String getClusterSchedule() {
        return clusterSchedule;
    }

    public void setClusterSchedule(String clusterSchedule) {
        this.clusterSchedule = clusterSchedule;
    }

    public String getClusterDistribution() {
        return clusterDistribution;
    }

    public void setClusterDistribution(String clusterDistribution) {
        this.clusterDistribution = clusterDistribution;
    }

    public String getClusterNews() {
        return clusterNews;
    }

    public void setClusterNews(String clusterNews) {
        this.clusterNews = clusterNews;
    }

    public String getClusterCloudDrive() {
        return clusterCloudDrive;
    }

    public void setClusterCloudDrive(String clusterCloudDrive) {
        this.clusterCloudDrive = clusterCloudDrive;
    }

    public String getClusterStorageLegacy() {
        return clusterStorageLegacy;
    }

    public void setClusterStorageLegacy(String clusterStorageLegacy) {
        this.clusterStorageLegacy = clusterStorageLegacy;
    }

    public String getTanent() {
        return tanent;
    }

    public Enterprise getCurrentEnterprise() {
        return currentEnterprise;
    }

    public String getSocketPath() {
        return socketPath;
    }

    public void setSocketPath(String socketPath) {
        this.socketPath = socketPath;
    }

    public String getClusterChatVersion() {
        return clusterChatVersion;
    }

    public void setClusterChatVersion(String clusterChatVersion) {
        this.clusterChatVersion = clusterChatVersion;
    }

    public String getClusterChatSocket() {
        return clusterChatSocket;
    }

    public void setClusterChatSocket(String clusterChatSocket) {
        this.clusterChatSocket = clusterChatSocket;
    }

    public String getClusterClientRegistry() {
        return clusterClientRegistry;
    }

    public void setClusterClientRegistry(String clusterClientRegistry) {
        this.clusterClientRegistry = clusterClientRegistry;
    }

    public String getClusterScheduleVersion() {
        return clusterScheduleVersion;
    }

    public void setClusterScheduleVersion(String clusterScheduleVersion) {
        this.clusterScheduleVersion = clusterScheduleVersion;
    }

    public String getClusterBot() {
        return clusterBot;
    }

    public void setClusterBot(String clusterBot) {
        this.clusterBot = clusterBot;
    }

    public boolean isV0VersionChat(){
        return getClusterChatVersion().toLowerCase().startsWith(Constant.SERVICE_VERSION_CHAT_V0);
    }

    /**
     * namespace
     * v1版及v1.x版返回/api/v1
     * v0版返回/
     * @return
     */
    public String getChatSocketNameSpace(){
        if(getClusterChatVersion().toLowerCase().startsWith("v0")){
            return "/";
        }else if(getClusterChatVersion().toLowerCase().startsWith("v1")){
            return "/api/v1";
        }
        return "";
    }

    /**
     * 判断是v1.x版本
     * @return
     */
    public  boolean isV1xVersionChat(){
        return getClusterChatVersion().toLowerCase().startsWith(Constant.SERVICE_VERSION_CHAT_V1);
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

    public void clearUserPhotoUrl(String uid) {
        if (!StringUtils.isBlank(uid) && userPhotoUrlMap.containsKey(uid)) {
            userPhotoUrlMap.remove(uid);
        }
    }

    /*************************************************************************/

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

/**************************************************************************/
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
                .diskCacheSize(50 * 1024 * 1024)
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
        L.disableLogging(); // 关闭imageloader的疯狂的log
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

    /**
     * ReactNative相关代码
     *
     * @return
     */
    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }


}
