/**
 * MyAppConfig.java
 * classes : com.inspur.emmcloud.basemodule.config.MyAppConfig
 *
 * @author Jason Chen
 * V 1.0.0
 * Create at 2016年8月23日 上午10:00:28
 */
package com.inspur.emmcloud.basemodule.config;

import android.content.Context;
import android.os.Environment;

import com.inspur.emmcloud.basemodule.api.BaseModuleApiUri;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;

import org.jsoup.Connection;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * com.inspur.emmcloud.basemodule.config.MyAppConfig
 *
 * @author Jason Chen; create at 2016年8月23日 上午10:00:28
 */
public class MyAppConfig {
    public static final String LOCAL_IMG_CREATE_PATH = BaseApplication.getInstance()
            .getExternalCacheDir() + "/IMP-Cloud/cache/img_create/";
    public static final String LOCAL_CACHE_PATH = BaseApplication.getInstance()
            .getExternalCacheDir() + "/IMP-Cloud/cache/";
    public static final String LOCAL_CACHE_VOICE_PATH = BaseApplication.getInstance()
            .getExternalCacheDir() + "/IMP-Cloud/cache/voice";
    public static final String LOCAL_CACHE_PHOTO_PATH = BaseApplication.getInstance()
            .getExternalCacheDir() + "/IMP-Cloud/cache/photo";
    public static final String LOCAL_OFFLINE_APP_PATH = BaseApplication.getInstance()
            .getExternalCacheDir() + "/IMP-Cloud/offlineApp";
    public static final String LOCAL_CACHE_MARKDOWN_PATH = BaseApplication.getInstance()
            .getExternalCacheDir() + "/IMP-Cloud/cache/Markdown/";
    public static final String LOCAL_CACHE_OSS_RECORD_PATH = BaseApplication.getInstance()
            .getExternalCacheDir() + "/IMP-Cloud/oss/";
    public static final String LOCAL_SHARE_FILE_PATH = BaseApplication.getInstance()
            .getExternalCacheDir() + "/IMP-Cloud/cache/share_file/";
    public static final String LOCAL_CACHE_SCREENSHOTS_PATH = BaseApplication.getInstance()
            .getExternalCacheDir() + "/IMP-Cloud/cache/screenshots/";
    /**
     * 用户文件操作空间，所有用户文件操作不应超出这个范围，清除全部缓存时应当考虑清除此处
     */
    public static final String LOCAL_IMP_USER_OPERATE_DIC = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/imp/user_space/";
    /**
     * 用户崩溃日志路径
     */
    public static final String LOCAL_IMP_CRASH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/crash/imp_cloud_crash.txt";
    public static final int UPLOAD_ORIGIN_IMG_MAX_SIZE = 2600;
    public static final int UPLOAD_ORIGIN_IMG_DEFAULT_SIZE = 1280;
    public static final int UPLOAD_THUMBNAIL_IMG_MAX_SIZE = 600;
    public static final int VOLUME_MAX_FILE_NAME_LENGTH = 40;
    public static final int WEBSOCKET_REQUEST_TIMEOUT_COMMON = 16;
    public static final int WEBSOCKET_REQUEST_TIMEOUT_SEND_MESSAGE = 600;
    private static final String LOCAL_DOWNLOAD_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/download/";
    public static int NETWORK_MOBILE_MAX_SIZE_ALERT = 1024 * 1024 * 50;
    private static Map<String, String> apiRequestRecordMap = new HashMap<>();


    public static Map<String, String> getLocalLanguageMap() {
        Map<String, String> languageMap = new HashMap<String, String>();
        languageMap.put("zh-CN", "zh-CN");
        languageMap.put("en-US", "en-US");
        languageMap.put("zh-TW", "zh-TW");
        return languageMap;

    }

    public static String getFileDownloadDirPath() {
        return Environment.getExternalStorageDirectory() + "/IMP-Cloud/download/";
    }

    public static String getFileDownloadByUserAndTanentDirPath() {
        return getFileDownloadDirPath() + BaseApplication.getInstance().getUid() + "/" + BaseApplication.getInstance().getTanent() + "/";
    }

    public static String getMailAttachmentFilePath(String mailId, String mailAttachmentName) {
        return getFileDownloadByUserAndTanentDirPath() + mailId + "/" + mailAttachmentName;
    }

    public static String getPluginDefaultDbCache() {
        return BaseApplication.getInstance().getDir("plugin_dir", MODE_PRIVATE).getPath() + "/cache/default_db";
    }

    /**
     * 获取React App的存储路径，分租户用户
     *
     * @param context
     * @param userId
     * @return
     */
    public static String getReactAppFilePath(Context context, String userId, String module) {
        return context.getDir("ReactResource_046", MODE_PRIVATE).getPath() + "/" + BaseApplication.getInstance().getTanent() + "/" + userId + "/" + module;
    }

    /**
     * 获取聊天语音存储目录
     *
     * @return
     */
    public static String getCacheVoiceFilePath(String cid, String messageId) {
        cid = cid.replaceAll(":", "_");
        return Environment.getExternalStorageDirectory() + "/IMP-Cloud/" + BaseApplication.getInstance().getUid() + "/" + BaseApplication.getInstance().getTanent() + "/voice/" + cid + "/" + messageId + ".amr";
    }

    /**
     * 获取聊天语音存储目录
     *
     * @return
     */
    public static String getCacheVoicePCMFilePath(String cid, String messageId) {
        cid = cid.replaceAll(":", "_");
        return Environment.getExternalStorageDirectory() + "/IMP-Cloud/" + BaseApplication.getInstance().getUid() + "/" + BaseApplication.getInstance().getTanent() + "/voice/" + cid + "/" + messageId + ".pcm";
    }

    /**
     * 获取React上一版本缓存途径（用于Roback的版本）
     *
     * @param context
     * @param userId
     * @return
     */
    public static String getReactTempFilePath(Context context, String userId) {
        return context.getDir("ReactResource_046", MODE_PRIVATE).getPath() + "/" + BaseApplication.getInstance().getTanent() + "/" + userId + "/Pre";
    }

    /**
     * 获取闪屏页目前显示图片的路径
     *
     * @param context
     * @param userId
     * @return
     */
    public static String getSplashPageImageShowPath(Context context, String userId, String module) {
        return context.getDir("SplashPage", MODE_PRIVATE).getPath() + "/" + BaseApplication.getInstance().getTanent() + "/" + userId + "/" + module;
    }

    public static String getChannelDrafsPreKey(String cid) {
        return "drafts" + cid;
    }

    public static void initAPIRequestRecordMap() {
        apiRequestRecordMap.clear();
        String loginUrl = WebServiceRouterManager.getInstance().getIDMUrl() + "oauth2.0/token";
        String profileUrl = WebServiceRouterManager.getInstance().getIDMUrl() + "oauth2.0/profile";
        String checkConfigUrl = WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v6.0/config/Check";
        String registerDeviceUrl = BaseModuleApiUri.getRegisterPushTokenUrl();
        String getChannelUrl = WebServiceRouterManager.getInstance().getClusterChat() + "/api/v1/channel";
        String getUserAppUrl = WebServiceRouterManager.getInstance().getClusterEmm() + "api/mam/v3.1/imp_app/userApps";
        apiRequestRecordMap.put(loginUrl, "Login");
        apiRequestRecordMap.put(profileUrl, "Profile");
        apiRequestRecordMap.put(checkConfigUrl, "CheckConfig");
        apiRequestRecordMap.put(registerDeviceUrl, "RegisterDevice");
        apiRequestRecordMap.put(getChannelUrl, "Getchannel");
        apiRequestRecordMap.put(getUserAppUrl, "GetUserApp");
    }

    public static Map<String, String> getApiRequestRecordMap() {
        return apiRequestRecordMap;
    }
}
