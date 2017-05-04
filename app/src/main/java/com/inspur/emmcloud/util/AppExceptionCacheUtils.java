package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.bean.AppException;
import com.lidroid.xutils.DbUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 频道列表缓存处理类
 * 
 * @author Administrator
 *
 */
public class AppExceptionCacheUtils {
	private static DbUtils db;


	/**
	 * 存储异常信息
	 * @param context
	 * @param appException
	 */
	public static void saveAppException(final Context context,
										final AppException appException) {

		// TODO Auto-generated method stub
		try {
			DbCacheUtils.getDb(context).saveOrUpdate(appException);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 获取异常list
	 * @param context
	 * @return
	 */
	public static List<AppException> getAppExceptionList(final Context context){
		List<AppException> appExceptionList = new ArrayList<>();
		try {
			appExceptionList = DbCacheUtils.getDb(context).findAll(AppException.class);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (appExceptionList == null){
			appExceptionList = new ArrayList<>();
		}
		return  appExceptionList;
	}
	
	/**
	 * 清除AppException表信息
	 * @param context
	 */
	public static void clearAppException(Context context) {
		try {
			DbCacheUtils.getDb(context).deleteAll(AppException.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
