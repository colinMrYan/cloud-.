package com.inspur.emmcloud;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.multidex.MultiDexApplication;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.beefe.picker.PickerViewPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.github.zafarkhaja.semver.Version;
import com.horcrux.svg.SvgPackage;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.interf.MyActivityLifecycleCallbacks;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.richtext.RichText;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.CrashHandler;
import com.inspur.emmcloud.util.privates.ECMShortcutBadgeNumberManagerUtils;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.inspur.emmcloud.util.privates.LanguageManager;
import com.inspur.emmcloud.util.privates.OauthUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.privates.PushManagerUtils;
import com.inspur.emmcloud.util.privates.WebServiceRouterManager;
import com.inspur.emmcloud.util.privates.cache.DbCacheUtils;
import com.inspur.imp.api.Res;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.oblador.vectoricons.VectorIconsPackage;

import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



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
    private boolean isSafeLock = false;//是否正处于安全锁定中（正处于二次认证解锁页面）

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
        LanguageManager.getInstance().setLanguageLocal();
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
        ImageDisplayUtils.getInstance().initImageLoader();
        initTanent();
        RichText.initCacheDir(new File(MyAppConfig.LOCAL_CACHE_MARKDOWN_PATH));
        RichText.debugMode = true;
        userPhotoUrlMap = new LinkedHashMap<String, String>() {
            @Override
            protected boolean removeEldestEntry(Entry<String, String> eldest) {
                // TODO Auto-generated method stub
                return size() > 40;

            }
        };
        PushManagerUtils.getInstance().clearPushFlag();
        isActive = false;
        isContactReady = PreferencesUtils.getBoolean(getInstance(),
                Constant.PREF_IS_CONTACT_READY, false);
        uid = PreferencesUtils.getString(getInstance(), "userID");
        accessToken = PreferencesUtils.getString(getInstance(), "accessToken", "");
        refreshToken = PreferencesUtils.getString(getInstance(), "refreshToken", "");
        //科大讯飞语音SDK初始化
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5a6001bf");
    }

    /**************************************登出逻辑相关********************************************************/
    /**
     * 注销
     */
    public void signout() {
        // TODO Auto-generated method stub
        //清除日历提醒极光推送本地通知
        PushManagerUtils.getInstance().stopPush();
        clearNotification();
        removeAllCookie();
        removeAllSessionCookie();
        clearUserPhotoMap();
        OauthUtils.getInstance().cancelToken();
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
        params.addHeader("Accept-Language", LanguageManager.getInstance().getCurrentAppLanguage());
        return params;
    }

    public boolean getIsContactReady() {
        return isContactReady;
    }

    /******************************通讯录相关***************************************/

    public void setIsContactReady(boolean isContactReady) {
        MyApplication.isContactReady = isContactReady;
        PreferencesUtils.putBoolean(getInstance(), Constant.PREF_IS_CONTACT_READY,isContactReady);
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
        boolean isMDMStatusPass = PreferencesUtils.getBoolean(getInstance(), Constant.PREF_MDM_STATUS_PASS, true);
        return (!StringUtils.isBlank(accessToken) && !StringUtils.isBlank(myInfo) && isMDMStatusPass);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        // TODO Auto-generated method stub
        if (config != null) {
            super.onConfigurationChanged(config);
        }
        LanguageManager.getInstance().setLanguageLocal();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.getInstance().attachBaseContext(newBase));
    }


    public MyActivityLifecycleCallbacks getActivityLifecycleCallbacks() {
        return myActivityLifecycleCallbacks;
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

    public boolean isSafeLock() {
        return isSafeLock;
    }

    public void setSafeLock(boolean safeLock) {
        isSafeLock = safeLock;
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
}
