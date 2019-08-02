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

import com.inspur.emmcloud.basemodule.application.BaseApplication;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * com.inspur.emmcloud.basemodule.config.MyAppConfig
 *
 * @author Jason Chen; create at 2016年8月23日 上午10:00:28
 */
public class MyAppConfig {
    public static final String LOCAL_IMG_CREATE_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/cache/img_create/";
    public static final String LOCAL_CACHE_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/cache/";
    public static final String LOCAL_CACHE_VOICE_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/cache/voice";
    public static final String LOCAL_CACHE_PHOTO_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/cache/photo";
    public static final String LOCAL_OFFLINE_APP_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/offlineApp";
    public static final String LOCAL_DOWNLOAD_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/download/";
    public static final String LOCAL_DOWNLOAD_PATH_MAIL_ATTCACHEMENT = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/download/mail/";
    public static final String LOCAL_CACHE_MARKDOWN_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/cache/Markdown/";
    public static final String LOCAL_CACHE_CHAT_PATH = Environment
            .getExternalStorageDirectory() + "/IMP-Cloud/cache/chat/";
    public static final int UPLOAD_ORIGIN_IMG_MAX_SIZE = 2600;
    public static final int UPLOAD_ORIGIN_IMG_DEFAULT_SIZE = 1280;
    public static final int UPLOAD_THUMBNAIL_IMG_MAX_SIZE = 600;
    public static final int VOLUME_MAX_FILE_NAME_LENGTH = 40;
    public static final int WEBSOCKET_QEQUEST_TIMEOUT = 16;
    public static Boolean test = true;


    public static Map<String, String> getLocalLanguageMap() {
        Map<String, String> languageMap = new HashMap<String, String>();
        languageMap.put("zh-CN", "zh-CN");
        languageMap.put("en-US", "en-US");
        languageMap.put("zh-TW", "zh-TW");
        return languageMap;

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
}
