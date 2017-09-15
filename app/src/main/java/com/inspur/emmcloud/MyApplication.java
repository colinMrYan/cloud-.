package com.inspur.emmcloud;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Parcelable;
import android.support.multidex.MultiDexApplication;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.alibaba.fastjson.JSON;
import com.beefe.picker.PickerViewPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.Enterprise;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.Language;
import com.inspur.emmcloud.callback.OauthCallBack;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.CalEventNotificationUtils;
import com.inspur.emmcloud.util.CrashHandler;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.HuaWeiPushMangerUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.util.richtext.RichText;
import com.inspur.imp.api.Res;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;

import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
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
    private WebSocketPush webSocketPush;
    private static boolean isContactReady = false;
    private boolean isTokenRefreshing = false;
    private List<OauthCallBack> callBackList = new ArrayList<OauthCallBack>();
    private String uid;
    private String accessToken;
    private Enterprise currentEnterprise;
    private Map<String, String> userPhotoUrlMap;

    public void onCreate() {
        super.onCreate();
        init();
        setAppLanguageAndFontScale();
        removeAllSessionCookie();
    }


    private void init() {
        // TODO Auto-generated method stub
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        x.Ext.init(MyApplication.this);
        x.Ext.setDebug(LogUtils.isDebug);
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
        isContactReady = PreferencesUtils.getBoolean(getApplicationContext(),
                "isContactReady", false);
        uid = PreferencesUtils.getString(getApplicationContext(), "userID");
        accessToken = PreferencesUtils.getString(getApplicationContext(), "accessToken", "");

    }


/**************************************登出逻辑相关********************************************************/
    //登出逻辑
    public void signout() {
        // TODO Auto-generated method stub
        if (((MyApplication) getApplicationContext()).getWebSocketPush() != null) {
            ((MyApplication) getApplicationContext()).getWebSocketPush()
                    .webSocketSignout();
        }
        //清除日历提醒极光推送本地通知
        CalEventNotificationUtils.cancelAllCalEventNotification(this);
        ((MyApplication) getApplicationContext()).stopPush();
        ((MyApplication) getApplicationContext()).clearNotification();
        ((MyApplication) getApplicationContext()).removeAllCookie();
        ((MyApplication) getApplicationContext()).clearUserPhotoMap();
        PreferencesUtils.putString(this, "tokenType", "");
        PreferencesUtils.putString(this, "accessToken", "");
        ((MyApplication) getApplicationContext()).setAccessToken("");
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        exit();
    }
/****************************通知相关（极光和华为推送）******************************************/
    /**
     * 初始化推送，以后如需定制小米等厂家的推送服务可从这里定制
     */
    public void startPush() {
        if (AppUtils.getIsHuaWei()&&canConnectHuawei()) {
            HuaWeiPushMangerUtils.getInstance(this).connect();
        } else {
            // 初始化 JPush
            JPushInterface.init(this);
            if (JPushInterface.isPushStopped(this)) {
                JPushInterface.resumePush(this);
            }
            // 设置开启日志,发布时请关闭日志
            JPushInterface.setDebugMode(true);
        }
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
        if (AppUtils.getIsHuaWei()) {
            HuaWeiPushMangerUtils.getInstance(this).delToken();
        } else {
            JPushInterface.stopPush(this);
        }
        //清除日历提醒极光推送本地通知
        CalEventNotificationUtils.cancelAllCalEventNotification(getApplicationContext());
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
                    CookieSyncManager.createInstance(getApplicationContext());
            CookieManager.getInstance().removeSessionCookie();
        }
    }

    public void removeAllCookie() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr =
                    CookieSyncManager.createInstance(getApplicationContext());
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
                        + AppUtils.getVersion(getApplicationContext()));
        params.addHeader("X-Device-ID",
                AppUtils.getMyUUID(getApplicationContext()));
        params.addHeader("Accept", "application/json");
        if (getToken() != null) {
            params.addHeader("Authorization", getToken());
        }
        if (currentEnterprise != null) {
            params.addHeader("X-ECC-Current-Enterprise", currentEnterprise.getId());
        }
        String languageJson = PreferencesUtils.getString(
                getApplicationContext(), UriUtils.tanent + "appLanguageObj");
        if (languageJson != null) {
            Language language = new Language(languageJson);
            params.addHeader("Accept-Language", language.getIana());
        }
        return params;
    }

    /******************************通讯录相关***************************************/

    public void setIsContactReady(boolean isContactReady) {
        this.isContactReady = isContactReady;
        PreferencesUtils.putBoolean(getApplicationContext(), "isContactReady",
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
        if(webSocketPush != null){
            if(isActive){
                webSocketPush.sendActivedMsg();

            }else{
                webSocketPush.sendFrozenMsg();
            }
        }
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

    public void setIsTokenRefreshing(boolean isTokenRefreshing) {
        this.isTokenRefreshing = isTokenRefreshing;
    }

    public boolean getIsTokenRefreshing() {
        return isTokenRefreshing;
    }

    public void addCallBack(OauthCallBack oauthCallBack) {
        callBackList.add(oauthCallBack);
    }

    public List<OauthCallBack> getCallBackList() {
        return callBackList;
    }

    public void clearCallBackList() {
        callBackList = new ArrayList<OauthCallBack>();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /***************************** db相关 *******************************************/
    /**
     * 关闭所有的数据库
     */
    public void closeAllDb() {
        // TODO Auto-generated method stub
        DbCacheUtils.closeDb(getApplicationContext());
    }

    /**
     * 删除此用户在此实例的所有db
     */
    public void deleteAllDb() {
        DbCacheUtils.deleteDb(getApplicationContext());
    }

    /******************************Websocket********************************************/

    /**
     * 开启websocket推送
     */
    public void startWebSocket() {
        webSocketPush = WebSocketPush.getInstance(getApplicationContext());
        if (isHaveLogin() && !webSocketPush.isSocketConnect()) {
            webSocketPush.start();
        }
    }

    /**
     * 关闭websocket推送
     */
    public void closeWebSocket(){
        if (webSocketPush != null) {
            webSocketPush.webSocketSignout();
        }
    }

    public WebSocketPush getWebSocketPush() {
        return webSocketPush;
    }

    /******************************租户信息*******************************************/

    public void initTanent() {
        // TODO Auto-generated method stub
        // UriUtils.res = "res_dev";
        currentEnterprise = null;
        String myInfo = PreferencesUtils.getString(getApplicationContext(),
                "myInfo");
        if (!StringUtils.isBlank(myInfo)) {
            GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
            String currentEnterpriseId = PreferencesByUsersUtils.getString(getApplicationContext(), "current_enterprise_id");
            if (!StringUtils.isBlank(currentEnterpriseId)) {
                List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
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
            String enterpriseCode = currentEnterprise.getCode();
            UriUtils.tanent = enterpriseCode;
            APIUri.tanent = enterpriseCode;
        }
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
        String myInfo = PreferencesUtils.getString(getApplicationContext(),
                "myInfo", "");
        boolean isMDMStatusPass = PreferencesUtils.getBoolean(getApplicationContext(), "isMDMStatusPass", true);
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
                .getString(getApplicationContext(), UriUtils.tanent
                        + "appLanguageObj");
        Configuration config = getResources().getConfiguration();
        if (languageJson != null) {
            String language = PreferencesUtils.getString(
                    getApplicationContext(), UriUtils.tanent + "language");
            // 当系统语言选择为跟随系统的时候，要检查当前系统的语言是不是在commonList中，重新赋值
            if (language.equals("followSys")) {
                String commonLanguageListJson = PreferencesUtils.getString(
                        getApplicationContext(), UriUtils.tanent
                                + "commonLanguageList");
                if (commonLanguageListJson != null) {
                    List<Language> commonLanguageList = (List) JSON
                            .parseArray(commonLanguageListJson,
                                    Language.class);
                    boolean isContainDefault = false;
                    for (int i = 0; i < commonLanguageList.size(); i++) {
                        Language commonLanguage = commonLanguageList.get(i);
                        if (commonLanguage.getIso().contains(
                                Resources.getSystem().getConfiguration().locale.getCountry())) {
                            PreferencesUtils.putString(
                                    getApplicationContext(),
                                    UriUtils.tanent + "appLanguageObj",
                                    commonLanguage.toString());
                            languageJson = commonLanguage.toString();
                            isContainDefault = true;
                            break;
                        }
                    }
                    if (!isContainDefault) {
                        PreferencesUtils.putString(getApplicationContext(),
                                UriUtils.tanent + "appLanguageObj",
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
            config.locale = locale;
        }
        config.fontScale = 1.0f;
        getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());

    }


    /**
     * init ImageLoader
     **/
    private void initImageLoader() {
        // TODO Auto-generated method stub
        ImageLoaderConfiguration config = null;
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            config = new ImageLoaderConfiguration.Builder(
                    getApplicationContext())
                    .memoryCacheExtraOptions(1200, 1200)
                    .threadPoolSize(6)
                    .threadPriority(Thread.NORM_PRIORITY - 1)
                    .denyCacheImageMultipleSizesInMemory()
                    .memoryCache(
                            new UsingFreqLimitedMemoryCache(3 * 1024 * 1024))
                    .diskCacheSize(50 * 1024 * 1024)
                    // You can pass your own memory cache implementation
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    // You can pass your own disc cache implementation
                    .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                    .build();
        } else {
            File cacheDir = new File(MyAppConfig.LOCAL_CACHE_PATH);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            config = new ImageLoaderConfiguration.Builder(
                    getApplicationContext())
                    .memoryCacheExtraOptions(1200, 1200)
                    .threadPoolSize(6)
                    .threadPriority(Thread.NORM_PRIORITY - 1)
                    .denyCacheImageMultipleSizesInMemory()
                    .memoryCache(
                            new UsingFreqLimitedMemoryCache(3 * 1024 * 1024))
                    .diskCacheSize(50 * 1024 * 1024)
                    // You can pass your own memory cache implementation
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .diskCache(new UnlimitedDiskCache(cacheDir))
                    // You can pass your own disc cache implementation
                    .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                    .build();
        }
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
        context.sendBroadcast(shortcutIntent);
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

    /**
     * exit Activity
     **/
    public void exit() {
        try {
            for (Activity activity : activityList) {
                if (activity != null)
                    activity.finish();
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
            return Arrays.<ReactPackage>asList(
                    new MainReactPackage(),
                    new AuthorizationManagerPackage(),
                    new PickerViewPackage()
            );
        }
    };

    /**
     * ReactNative相关代码
     * @return
     */
    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

}
