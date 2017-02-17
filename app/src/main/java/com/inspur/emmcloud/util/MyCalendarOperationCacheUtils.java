package com.inspur.emmcloud.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;




import android.content.Context;

import com.inspur.emmcloud.bean.MyCalendar;
import com.inspur.emmcloud.bean.MyCalendarOperation;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.DbUtils.DbUpgradeListener;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.db.table.Table;

/**
 * 频道列表缓存处理类
 * 
 * @author Administrator
 *
 */
public class MyCalendarOperationCacheUtils {


	/**
	 * 存储单个日历操作
	 * 
	 * @param context
	 * @param channel
	 */
	public static void saveMyCalendarOperation(Context context, String myCalendarId,boolean isHide) {
		try {
			MyCalendarOperation operation = new MyCalendarOperation(myCalendarId, isHide);
			DbCacheUtils.getDb(context).saveOrUpdate(operation);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
	
	/**
	 * 获取对此日历是否隐藏
	 * @param context
	 * @param myCalendarId
	 * @return
	 */
	public static boolean getIsHide(Context context, String myCalendarId){
		boolean isHide = false;
		try {
			MyCalendarOperation operation = DbCacheUtils.getDb(context).findById(MyCalendarOperation.class, myCalendarId);
			if (operation != null) {
				isHide = operation.getIsHide();
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return isHide;

	}

	



}
