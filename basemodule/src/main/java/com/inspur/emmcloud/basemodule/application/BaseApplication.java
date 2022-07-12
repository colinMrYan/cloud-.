package com.inspur.emmcloud.basemodule.application;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.github.zafarkhaja.semver.Version;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.bean.Enterprise;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.util.AppConfigCacheUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ClientConfigUpdateUtils;
import com.inspur.emmcloud.basemodule.util.CrashHandler;
import com.inspur.emmcloud.basemodule.util.CustomImageDownloader;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.basemodule.util.ECMShortcutBadgeNumberManagerUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.tencent.ugc.TXUGCBase;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.inspur.emmcloud.basemodule.util.protocol.ProtocolUtil.PREF_PROTOCOL_DLG_AGREED;

/**
 * Application class
 */
public abstract class BaseApplication extends MultiDexApplication {
    private static final String TAG = "BaseApplication";
    private static boolean isContactReady = false;
    private static BaseApplication instance;
    public Map<String, String> userPhotoUrlMap = new LinkedHashMap<>();
    private List<Activity> activityList = new LinkedList<Activity>();
    private boolean isIndexActivityRunning = false;
    private boolean isActive = false;
    private String uid;
    private String accessToken;
    private String refreshToken;
    private Enterprise currentEnterprise;
    private MyActivityLifecycleCallbacks myActivityLifecycleCallbacks;
    private boolean isOpenNotification = false;
    private String tanent;
    private String currentChannelCid = "";
    private boolean isSafeLock = false;//是否正处于安全锁定中（正处于二次认证解锁页面）
    private boolean mInited = false;


    /**
     * 单例获取application实例
     *
     * @return BaseApplication
     */
    public static BaseApplication getInstance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        instance = this;
        initWithoutUserInfo();
        if (PreferencesUtils.getBoolean(this, PREF_PROTOCOL_DLG_AGREED, false)) {
            init();
        }
    }

    private void initWithoutUserInfo() {
        Router.registerComponent("com.inspur.emmcloud.applike.AppApplike");
        Router.registerComponent("com.inspur.emmcloud.login.applike.LoginAppLike");
        Router.registerComponent("com.inspur.emmcloud.web.applike.WebAppLike");
        Router.registerComponent("com.inspur.emmcloud.news.applike.NewsAppLike");
        Router.registerComponent("com.inspur.emmcloud.webex.applike.WebexAppLike");
        Router.registerComponent("com.inspur.emmcloud.mail.applike.MailAppLike");
        Router.registerComponent("com.inspur.emmcloud.schedule.applike.ScheduleAppLike");
        Router.registerComponent("com.inspur.emmcloud.application.applike.ApplicationAppLike");
        Router.registerComponent("com.inspur.emmcloud.volume.applike.VolumeAppLike");
        Router.registerComponent("com.inspur.emmcloud.setting.applike.SettingAppLike");
        if (AppUtils.isApkDebugable(getInstance())) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(getInstance());
        Res.init(this); // 注册imp的资源文件类
        ToastUtils.init(this);
    }


    public void init() {
        if (mInited) {
            return;
        }
        mInited = true;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getInstance());
        x.Ext.init(getInstance());
        x.Ext.setDebug(true);
        LogUtils.isDebug = AppUtils.isApkDebugable(getInstance());

        ImageDisplayUtils.getInstance().initImageLoader(getInstance(), new CustomImageDownloader(getInstance()), MyAppConfig.LOCAL_CACHE_PATH);
        initTanent();
        userPhotoUrlMap = new LinkedHashMap<String, String>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                // TODO Auto-generated method stub
                return size() > 40;

            }
        };
        isActive = false;
        isContactReady = PreferencesUtils.getBoolean(getInstance(),
                Constant.PREF_IS_CONTACT_READY, false);
        uid = PreferencesUtils.getString(getInstance(), "userID");
        accessToken = PreferencesUtils.getString(getInstance(), "accessToken", "");
        refreshToken = PreferencesUtils.getString(getInstance(), "refreshToken", "");
        if ("11487".equals(uid)) {
            // 郑总token刷新失败分析日志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                String processName = getProcessName();
                PVCollectModelCacheUtils.saveCollectModel("BaseApplication: initToken", "---at---" + accessToken + "---rt---" + refreshToken + "---processName---" + processName);
            }
        }
        //科大讯飞语音SDK初始化
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5a6001bf");
        //置为0，调起解锁界面 (强杀进程后)
        PreferencesUtils.putLong(BaseApplication.getInstance(), Constant.PREF_APP_BACKGROUND_TIME, 0L);

        onConfigurationChanged(null);
        removeAllSessionCookie();
        myActivityLifecycleCallbacks = new MyActivityLifecycleCallbacks();
        registerActivityLifecycleCallbacks(myActivityLifecycleCallbacks);
        // 短视频licence设置
        String ugcKey = "28db2b484abaeef442fd8ce79929466d";
        String ugcLicenceUrl = "https://license.vod2.myqcloud.com/license/v2/1310194503_1/v_cube.license";
        TXUGCBase.getInstance().setLicence(this, ugcLicenceUrl, ugcKey);
    }

    /**************************************登出逻辑相关********************************************************/
    /**
     * 注销
     */
    public void signout() {
        clearNotification();
        removeAllCookie();
        removeAllSessionCookie();
        clearUserPhotoMap();
        Router router = Router.getInstance();
        if (router.getService(CommunicationService.class) != null) {
            CommunicationService service = router.getService(CommunicationService.class);
            service.stopPush();
            service.webSocketSignout();
            service.MessageSendManagerOnDestroy();
            service.stopVoiceCommunication();
        }
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.logout(getInstance());
        }
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

    /**
     * 获取http RequestParams
     *
     * @param url
     * @return
     */
    public RequestParams getHttpRequestParams(String url) {
        return getHttpRequestParams(url, "", "");
    }

    public RequestParams getHttpRequestParams(String url, String extraHeaderKey, String extraHeaderValue) {
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
        if (!StringUtils.isBlank(extraHeaderKey) && !StringUtils.isBlank(extraHeaderValue)) {
            params.addHeader(extraHeaderKey, extraHeaderValue);
        }
        params.addHeader("Accept-Language", LanguageManager.getInstance().getCurrentAppLanguage());
        return params;
    }

    public boolean getIsContactReady() {
        return isContactReady;
    }
    /*************************** http相关 **************************************/

    /******************************通讯录相关***************************************/

    public void setIsContactReady(boolean isContactReady) {
        BaseApplication.isContactReady = isContactReady;
        PreferencesUtils.putBoolean(getInstance(), Constant.PREF_IS_CONTACT_READY, isContactReady);
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
        Router router = Router.getInstance();
        if (router.getService(CommunicationService.class) != null) {
            CommunicationService service = router.getService(CommunicationService.class);
            service.sendAppStatus();
        }
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
            if (currentEnterprise != null) {
                WebServiceRouterManager.getInstance().setWebServiceRouter(currentEnterprise);
                tanent = currentEnterprise.getCode();
            } else {
                PreferencesUtils.putString(getInstance(), "myInfo", "");
                PreferencesUtils.putString(getInstance(), "accessToken", "");
                PreferencesUtils.putString(getInstance(), "refreshToken", "");
                PreferencesUtils.putInt(getInstance(), "keepAlive", 0);
                PreferencesUtils.putString(getInstance(), "tokenType", "");
                PreferencesUtils.putInt(getInstance(), "expiresIn", 0);
                BaseApplication.getInstance().setAccessToken("");
                BaseApplication.getInstance().setRefreshToken("");
                ToastUtils.show(R.string.login_user_not_bound_enterprise);
            }

        }
    }

    public String getTanent() {
        return tanent;
    }

    public void setTanent(String tanent) {
        this.tanent = tanent;
    }

    public Enterprise getCurrentEnterprise() {
        return currentEnterprise;
    }

    public void setCurrentEnterprise(Enterprise currentEnterprise) {
        this.currentEnterprise = currentEnterprise;
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
        String previousLocal = PreferencesUtils.getString(BaseApplication.getInstance(), Constant.PREF_LANGUAGE_CURRENT_LOCAL, "");
        if (config != null) {
            super.onConfigurationChanged(config);
        }
        String currentLocal = Resources.getSystem().getConfiguration().locale.toString();
        if (!previousLocal.equals(currentLocal)) {
            //清空我的应用统一更新版本信息防止切换语言不刷新列表
            ClientConfigUpdateUtils.getInstance().clearDbDataConfigWithMyApp();
        }
        LanguageManager.getInstance().setLanguageLocal();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String previousLocal = PreferencesUtils.getString(newBase, Constant.PREF_LANGUAGE_CURRENT_LOCAL, "");
        String currentLocal = Resources.getSystem().getConfiguration().locale.toString();
        if (!previousLocal.equals(currentLocal)) {
            super.attachBaseContext(newBase);
        } else {
            super.attachBaseContext(LanguageManager.getInstance().attachBaseContext(newBase));
        }

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
     * 是否从角标服务拉取角标数量
     */
    public boolean getBadgeFromBadgeServer() {
        String fromBadgeServerConfig = AppConfigCacheUtils.getAppConfigValue(BaseApplication.getInstance(), Constant.CONCIG_FORCE_PULL_BADGE, "914683;912536;915614;913530;");
        return fromBadgeServerConfig.contains(getCurrentEnterprise().getId());
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
     * 判断一个Activity是否存在
     *
     * @param targetActivity
     */
    public boolean isActivityExist(Class targetActivity) {
        try {
            for (Activity activity : activityList) {
                if (targetActivity.getCanonicalName().endsWith(activity.getLocalClassName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtils.exceptionDebug(TAG, e.toString());
        }
        return false;
    }

    public void closeActivity(String activityName) {
        try {
            for (Activity activity : activityList) {
                if (activity.getClass().getSimpleName().equals(activityName)) {
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
        //如语音通话中发送通知
        Router router = Router.getInstance();
        if (router.getService(CommunicationService.class) != null) {
            CommunicationService communicationService = router.getService(CommunicationService.class);
            communicationService.sendVoiceCommunicationNotify();
        }
        if (AppUtils.getIsXiaoMi()) {
            MiPushClient.clearNotification(this);
        }
    }

    /****************************标记当前正在某个频道中***************************************************/
    public String getCurrentChannelCid() {
        return currentChannelCid;
    }

    public void setCurrentChannelCid(String currentChannelCid) {
        this.currentChannelCid = currentChannelCid;
    }

    /*******************设置登录后跳转的路由*************************************/

    public abstract String getIntentClassRouterAfterLogin();
}
