package com.inspur.emmcloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.CrashHandler;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.imp.api.Res;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import cn.jpush.android.api.JPushInterface;



/**
 * 
 * Application class
 * 
 */
public class MyApplication extends MultiDexApplication implements  ReactApplication{
	private static final String TAG = "MyApplication";
	private List<Activity> activityList = new LinkedList<Activity>();
	private List<Activity> contactActivityList = new LinkedList<Activity>();

	private boolean isIndexActivityRunning = false;
	private boolean isActive = false;
	private boolean isChannelActivityRunning = false;
	private WebSocketPush webSocketPush;
	private static boolean isContactReady = false;

	private boolean isTokenRefreshing = false;
	private List<OauthCallBack> callBackList = new ArrayList<OauthCallBack>();
	private String uid;
	private String accessToken;
	private Enterprise currentEnterprise;



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

	@Override
	public ReactNativeHost getReactNativeHost() {
		return mReactNativeHost;
	}
	public void onCreate() {
		super.onCreate();
		init();
		isActive = false;
		isContactReady = PreferencesUtils.getBoolean(getApplicationContext(),
				"isContactReady", false);
		uid = PreferencesUtils.getString(getApplicationContext(), "userID");
		accessToken = PreferencesUtils.getString(getApplicationContext(), "accessToken","");
		onConfigurationChanged(null);
		removeAllSessionCookie();
		//修改字体方案预留
//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("fonts/xiaozhuan.ttf")
//

	}


	private void init() {
		// TODO Auto-generated method stub
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		x.Ext.init(MyApplication.this);
		x.Ext.setDebug(LogUtils.isDebug);
		SoLoader.init(this,false);
		Res.init(this); // 注册imp的资源文件类
		initJPush();
		initImageLoader();
		initTanent();
	}

	/**
	 * 初始化极光推送
	 */
	public void initJPush() {
		// TODO Auto-generated method stub
		// 设置开启日志,发布时请关闭日志
		JPushInterface.setDebugMode(true);
		// 初始化 JPush
		JPushInterface.init(this);
		// 获取和存储RegId
		String pushRegId = JPushInterface
				.getRegistrationID(getApplicationContext());
		if (!StringUtils.isBlank(pushRegId)) {
			PreferencesUtils.putString(getApplicationContext(), "JpushRegId",
					pushRegId);
		}
		JPushInterface.resumePush(this);
	}

/************************ Cookie相关 *****************************/
	/**
	 * 清除所有的SessionCookie
	 */
	private void removeAllSessionCookie(){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			CookieManager.getInstance().removeSessionCookies(null);
			CookieManager.getInstance().flush();
		}else {
			CookieSyncManager cookieSyncMngr =
					CookieSyncManager.createInstance(getApplicationContext());
			CookieManager.getInstance().removeSessionCookie();
		}
	}

	public void removeAllCookie(){
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			CookieManager.getInstance().removeAllCookies(null);
			CookieManager.getInstance().flush();
		}else {
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
		if (currentEnterprise != null){
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
	 * 开启推送
	 */
	public void startWebSocket() {
		String myInfo = PreferencesUtils.getString(getApplicationContext(),
				"myInfo", "");
		boolean isHaveLogined = !StringUtils.isBlank(myInfo)
				&& getToken() != null;
		webSocketPush = WebSocketPush.getInstance(getApplicationContext());
		if (isHaveLogined && !webSocketPush.isSocketConnect()) {
			webSocketPush.start();
		}
	}

	public WebSocketPush getWebSocketPush() {
		return webSocketPush;
	}

	/**
	 * 关闭推送
	 */
	public void stopWebSocket() {
		if (webSocketPush != null) {
			webSocketPush.closeSocket();
		}
	}

	/**
	 * WebScoket发送应用切到前台信息
	 */
	public void sendActivedWSMsg() {
		if (webSocketPush != null) {
			webSocketPush.sendActivedMsg();
		}
	}

	/**
	 * WebScoket发送应用切到后台信息
	 */
	public void sendFrozenWSMsg() {
		if (webSocketPush != null) {
			webSocketPush.sendFrozenMsg();
		}
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
			String currentEnterpriseId = PreferencesByUsersUtils.getString(getApplicationContext(),"current_enterprise_id");
			if (!StringUtils.isBlank(currentEnterpriseId)){
				List<Enterprise> enterpriseList = getMyInfoResult.getEnterpriseList();
				for (int i=0;i<enterpriseList.size();i++){
					Enterprise enterprise = enterpriseList.get(i);
					if (enterprise.getId().equals(currentEnterpriseId)){
						currentEnterprise = enterprise;
						break;
					}

				}
			}
			if (currentEnterprise == null){
				currentEnterprise =getMyInfoResult.getDefaultEnterprise();
			}
			String enterpriseCode = currentEnterprise.getCode();
			UriUtils.tanent = enterpriseCode;
			APIUri.tanent = enterpriseCode;
		}
	}

	public Enterprise  getCurrentEnterprise(){
		return  currentEnterprise;
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


	@Override
	public void onConfigurationChanged(Configuration config) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(null);
//		if (config == null) {
			config = getResources().getConfiguration();

			String languageJson = PreferencesUtils
					.getString(getApplicationContext(), UriUtils.tanent
							+ "appLanguageObj");
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
									Locale.getDefault().getCountry())) {
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
				config.locale = new Locale(country, variant);
			}
			if(getApplicationContext() != null){
				getApplicationContext().getResources().updateConfiguration(config,
						getResources().getDisplayMetrics());
			}
//		} else {
//			super.onConfigurationChanged(null);
//		}

	}

	/** init ImageLoader **/
	private void initImageLoader() {
		// TODO Auto-generated method stub
		ImageLoaderConfiguration config = null;
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			config = new ImageLoaderConfiguration.Builder(
					getApplicationContext())
					.memoryCacheExtraOptions(1200, 1200)
					.imageDownloader(
							new CustomImageDownloader(getApplicationContext()))
					.threadPoolSize(6)
					.threadPriority(Thread.NORM_PRIORITY - 1)
					.denyCacheImageMultipleSizesInMemory()
					.memoryCache(
							new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
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
					.imageDownloader(
							new CustomImageDownloader(getApplicationContext()))
					.threadPoolSize(6)
					.threadPriority(Thread.NORM_PRIORITY - 1)
					.denyCacheImageMultipleSizesInMemory()
					.memoryCache(
							new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
					.diskCacheSize(50 * 1024 * 1024)
					// You can pass your own memory cache implementation
					.tasksProcessingOrder(QueueProcessingType.LIFO)
					.diskCache(new UnlimitedDiskCache(cacheDir))
					// You can pass your own disc cache implementation
					.diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
					.build();
		}
		// L.disableLogging(); // 关闭imageloader的疯狂的log
		ImageLoader.getInstance().init(config);

	}

	public class CustomImageDownloader extends BaseImageDownloader {// universal
																	// image
																	// loader获取图片时,若需要cookie，
		// 需在application中进行配置添加此类。
		public CustomImageDownloader(Context context) {
			super(context);
		}

		@Override
		protected HttpURLConnection createConnection(String url, Object extra)
				throws IOException {
			// Super...
			HttpURLConnection connection = super.createConnection(url, extra);
			// connection.setRequestProperty("Authorization", getToken());
			connection.setRequestProperty("Connection", "keep-Alive");
			connection.setRequestProperty("User-Agent", "jsgdMobile");
			return connection;
		}
	}

	/** 添加桌面快捷方式 **/
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

	/**
	 * 添加通讯录Activity的实例
	 */
	public void addContactActivity(Activity activity) {
		contactActivityList.add(activity);
	}

	/**
	 * 关闭通讯录Activity的实例
	 */
	public void closeContactActivity() {
		try {
			for (Activity activity : contactActivityList) {
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e) {
			LogUtils.exceptionDebug(TAG, e.toString());
		}
	}

	// 判断IndexActivity是否存在的标志
	public boolean isIndexActivityRunning() {
		return isIndexActivityRunning;
	}

	public void setIndexActvityRunning(boolean running) {
		isIndexActivityRunning = running;
	}

	// 判断会话窗口ChannelActivity是否存在的标志
	public boolean isChannelActivityRunning() {
		return isChannelActivityRunning;
	}

	public void setChannelActivityRunning(boolean isChannelActivityRunning) {
		this.isChannelActivityRunning = isChannelActivityRunning;
	}

	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	/** exit Activity **/
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

	public void clearNotification() {
		JPushInterface.clearAllNotifications(this);
	}

}
