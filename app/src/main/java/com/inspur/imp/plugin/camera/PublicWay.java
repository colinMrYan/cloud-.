package com.inspur.imp.plugin.camera;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.inspur.imp.plugin.photo.PhotoService;

import android.app.Activity;

/**
 * 存放所有的list在最后退出时一起关闭
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class PublicWay {
	public static Set<Activity> activityList = new HashSet<Activity>();
	
	public static File file ;
	
	public static CameraService photoService;
	public static PhotoService uploadPhotoService;
	
	public static ArrayList<ImageItem> selectedDataList = new ArrayList<ImageItem>();
	
	
}
