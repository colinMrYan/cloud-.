package com.inspur.emmcloud.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.inspur.emmcloud.bean.AppCommonlyUse;
import com.inspur.emmcloud.bean.AppOrder;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

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
			DbCacheUtils.getDb(context).saveOrUpdateAll(appOrderList);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 按组删除
	 * @param context
	 * @param categoryID
	 */
	public static void deleteAppOrderByCategoryID(Context context, String categoryID) {
		try {
			DbCacheUtils.getDb(context).delete(AppOrder.class,
					WhereBuilder.b("categoryID", "=", categoryID));
		} catch (DbException e) {
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
			appOrderList = DbCacheUtils.getDb(context).findAll(Selector.from(AppOrder.class));
		} catch (DbException e) {
			e.printStackTrace();
		}
		if(appOrderList == null){
			appOrderList = new ArrayList<AppOrder>();
		}
		return appOrderList;
	}
	
	/**
	 * 保存常用应用列表
	 * @param context
	 * @param appCommonlyUseList
	 */
	public static void saveAppCommonlyUse(Context context,AppCommonlyUse appCommonlyUse){
		try {
			DbCacheUtils.getDb(context).saveOrUpdate(appCommonlyUse);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 存储常用应用顺序
	 */
	public static void saveAppCommonlyUseList(Context context,List<AppCommonlyUse> appCommonlyUseList){
		try {
			DbCacheUtils.getDb(context).saveOrUpdateAll(appCommonlyUseList);
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新点击次数
	 * @param context
	 * @param clickCount
	 */
	public static void saveAppCommonlyUse(Context context,int clickCount){
	}
	
	/**
	 * 删除常用app  List
	 * @param context
	 * @param appCommonlyUseList
	 */
	public static void deleteAppCommonlyUseList(Context context,List<AppCommonlyUse> appCommonlyUseList){
		try {
			DbCacheUtils.getDb(context).deleteAll(appCommonlyUseList);
		} catch (DbException e) {
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
		} catch (DbException e) {
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
			commonlyUseAppList = DbCacheUtils.getDb(context).findAll(Selector.from(AppCommonlyUse.class)
						.orderBy("lastUpdateTime",true).limit(commonlyUseAppNum));
		} catch (DbException e) {
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
			commonlyUseAppList = DbCacheUtils.getDb(context).findAll(Selector.from(AppCommonlyUse.class));
		} catch (DbException e) {
			e.printStackTrace();
		}
		if(commonlyUseAppList == null){
			commonlyUseAppList = new ArrayList<AppCommonlyUse>();
		}
		return commonlyUseAppList;
	}
	
	/**
	 * 通过AppId获取常用应用
	 * @param context
	 * @param appId
	 * @return
	 */
	public static AppCommonlyUse getCommonlyUseAppById(Context context,String appId){
		AppCommonlyUse appCommonlyUse = null;
		try {
			appCommonlyUse = DbCacheUtils.getDb(context).findById(AppCommonlyUse.class, appId);
		} catch (DbException e) {
			e.printStackTrace();
		}
//		if(appCommonlyUse == null){
//			appCommonlyUse = new AppCommonlyUse();
//		}
		return appCommonlyUse;
	}
}
