package com.inspur.emmcloud.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.Channel;
import com.inspur.emmcloud.bean.MatheSet;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 * Object类缓存存储类
 * @author Administrator
 *
 */
public class ObjectLocalCacheUtils {
	private static final String myAppFileName = "app_file.xml"; //我的应用缓存文件
	private static final String msgFileName = "msg_file.xml";//消息缓存文件
	private static final String channelFileName = "channel_file.xml";//频道列表缓存文件
	//public static PropertiesConfigUtils config;

	public static void saveObj(Context context, String key, Object object,String saveFileName) {
		PropertiesConfigUtils config = PropertiesConfigUtils.getInstance(saveFileName, context);
		Gson gson = new Gson();
		String value = gson.toJson(object);
		config.setProperty(key, value);
	}

	public static Object getObject(Context context, String key,String saveFileName) {
		PropertiesConfigUtils config = PropertiesConfigUtils.getInstance(saveFileName, context);
		if (!config.containsKey(key)) {
			return null;
		} else {
			String value = config.getProperty(key);
			Gson gson = new Gson();
			Object object = null;
			if (saveFileName.equals(myAppFileName)) {
				 object = gson.fromJson(value, new TypeToken<List<App>>() {
				}.getType());	
			}else if (saveFileName.equals(msgFileName)) {
				 object = gson.fromJson(value, new TypeToken<ArrayList<MatheSet>>() {
				}.getType());	
			}else if (saveFileName.equals(channelFileName)) {
				 object = gson.fromJson(value, new TypeToken<ArrayList<Channel>>() {
					}.getType());	
			}
			
			return object;
		}
	}
	
	public static void clearCache(Context context,String saveFileName){
		File cacheFile = new File("/data/data/" + context.getPackageName()
				+ "/files/" + saveFileName);
		if (cacheFile.exists()) {
			cacheFile.delete();
		}
	}

}
