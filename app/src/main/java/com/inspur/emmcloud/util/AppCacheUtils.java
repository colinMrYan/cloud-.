package com.inspur.emmcloud.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.inspur.emmcloud.bean.AppCommonlyUse;
import com.inspur.emmcloud.bean.AppOrder;

/**
 * classes : com.inspur.emmcloud.util.AppCacheUtils Create at 2016年12月17日
 * 下午2:59:11
 */
public class AppCacheUtils {

	/**
	 * 保存顺序
	 * @param context
	 * @param appOrderList
	 * @param categoryID
	 */
	public static void saveAppOrderList(Context context,
			List<AppOrder> appOrderList, String categoryID) {
		try {
			DbCacheUtils.getDb(context).saveOrUpdate(appOrderList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 获取所又AppOrder
	 * @param context
	 */
	public static List<AppOrder> getAllAppOrderList(Context context){
		List<AppOrder> appOrderList = new ArrayList<AppOrder>();
		try {
			appOrderList = DbCacheUtils.getDb(context).findAll(AppOrder.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(appOrderList == null){
			appOrderList = new ArrayList<AppOrder>();
		}
		return appOrderList;
	}
	

	/**
	 * 存储常用应用顺序
	 */
	public static void saveAppCommonlyUseList(Context context,List<AppCommonlyUse> appCommonlyUseList){
		try {
			DbCacheUtils.getDb(context).saveOrUpdate(appCommonlyUseList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除常用app  List
	 * @param context
	 */
	public static void deleteAppCommonlyByAppID(Context context,String appID){
		try {
			DbCacheUtils.getDb(context).deleteById(AppCommonlyUse.class, appID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取指定数量的常用app
	 * @param context
	 * @param commonlyUseAppNum
	 * @return
	 */
	public static List<AppCommonlyUse> getCommonlyUseAppList(Context context,int commonlyUseAppNum){
		List<AppCommonlyUse> commonlyUseAppList = null;
		try {
			commonlyUseAppList = DbCacheUtils.getDb(context).selector(AppCommonlyUse.class)
						.orderBy("lastUpdateTime",true).limit(commonlyUseAppNum).findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(commonlyUseAppList == null){
			commonlyUseAppList = new ArrayList<AppCommonlyUse>();
		}
		return commonlyUseAppList;
	}
	
	/**
	 * 获取所有AppCommonLyUse
	 * @param context
	 * @return
	 */
	public static List<AppCommonlyUse> getCommonlyUseAppList(Context context){
		List<AppCommonlyUse> commonlyUseAppList = null;
		try {
			commonlyUseAppList = DbCacheUtils.getDb(context).findAll(AppCommonlyUse.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(commonlyUseAppList == null){
			commonlyUseAppList = new ArrayList<AppCommonlyUse>();
		}
		return commonlyUseAppList;
	}
	
}
