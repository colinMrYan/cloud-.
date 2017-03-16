/**
 * 
 * MyAppConfig.java
 * classes : com.inspur.emmcloud.config.MyAppConfig
 * @author Jason Chen
 * V 1.0.0
 * Create at 2016年8月23日 上午10:00:28
 */
package com.inspur.emmcloud.config;

import android.content.Context;
import android.os.Environment;

import com.inspur.emmcloud.bean.Language;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * com.inspur.emmcloud.config.MyAppConfig
 * 
 * @author Jason Chen; create at 2016年8月23日 上午10:00:28
 */
public class MyAppConfig {
	public static final String LOCAL_IMG_CREATE_PATH = Environment
			.getExternalStorageDirectory() + "/IMP-Cloud/cache/img_create/";
	public static final String LOCAL_CACHE_PATH = Environment
			.getExternalStorageDirectory() + "/IMP-Cloud/cache/";
	public static final String LOCAL_DOWNLOAD_PATH = Environment
			.getExternalStorageDirectory() + "/IMP-Cloud/download/";
	public static final String ERROR_FILE_PATH = Environment
			.getExternalStorageDirectory() + "/IMP-Cloud/";
	public static final String[] clientLanguages = { "zh-CN", "en-US", "zh-TW" };

	public static Map<String, String> getLocalLanguageMap() {
		Map<String, String> languageMap = new HashMap<String, String>();
		languageMap.put("zh-CN", "zh-CN");
		languageMap.put("en-US", "en-US");
		languageMap.put("zh-TW", "zh-TW");
		return languageMap;

	}


	/**
	 * 获取React当前展示的文件路径
	 * @param context
	 * @param userId
     * @return
     */
	public static String getReactCurrentFilePath(Context context,String userId){
		return context.getDir("react",MODE_PRIVATE).getPath()+"/current"+userId;
	}


	/**
	 * 获取React上一版本缓存途径（用于Roback的版本）
	 * @param context
	 * @param userId
     * @return
     */
	public static String getReactTempFilePath(Context context,String userId){
		return context.getDir("react",MODE_PRIVATE).getPath()+"/temp"+userId;
	}
	
	public static Language getDefaultLanguage = new Language("中文简体","zh-CN","zh-Hans","zh-CN","zh-CN","zh-Hans");
}
