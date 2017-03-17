package com.inspur.imp.plugin.photo;

import android.content.Context;

import com.inspur.emmcloud.util.PreferencesUtils;

public class PhotoNameUtils {
	public static String getFileName(Context context) {
		return getFileName(context,0);
	}
	
	public static String getFileName(Context context,int index) {
		index = index+1;
		String userName = PreferencesUtils.getString(context,
				"userName", "");
		String fileName = userName+"_"+System.currentTimeMillis()+ "_"+index+".png";
		return fileName;
	}
	
	public static String getListFileName(Context context,long time,int index) {
		index = index+1;
		String userName = PreferencesUtils.getString(context,
				"userName", "");
		String fileName = userName+"_"+time+ "_"+index+".png";
		return fileName;
	}
}
