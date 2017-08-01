package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.bean.WorkSetting;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;

import java.util.ArrayList;
import java.util.List;

/**
 * 频道列表缓存处理类
 * 
 * @author Administrator
 *
 */
public class WorkSettingCacheUtils {
	private static DbUtils db;


	/**
	 * 存储工作配置列表
	 * 
	 * @param context
	 * @param channelList
	 */
	public static void saveWorkSettingList(Context context,
			 List<WorkSetting> workSettingList) {

		// TODO Auto-generated method stub
		try {
			if (workSettingList == null || workSettingList.size() == 0) {
				return;
			}
			DbCacheUtils.getDb(context).saveOrUpdateAll(workSettingList);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 获取所有的工作配置列表
	 * @param context
	 * @return
	 */
	public static List<WorkSetting> getAllWorkSettingList(Context context){
		List<WorkSetting> workSettingList = null;
		try {
			workSettingList = DbCacheUtils.getDb(context).findAll(Selector.from(WorkSetting.class).orderBy("sort"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (workSettingList == null){
			workSettingList = new ArrayList<>();
		}
			return  workSettingList;
	}

	/**
	 * 获取所有打开的的工作配置列表
	 * @param context
	 * @return
	 */
	public static List<WorkSetting> getOpenWorkSettingList(Context context){
		List<WorkSetting> workSettingList = null;
		try {
			workSettingList = DbCacheUtils.getDb(context).findAll(Selector.from(WorkSetting.class).where("isOpen","=",true).orderBy("sort"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (workSettingList == null){
			workSettingList = new ArrayList<>();
		}
		return  workSettingList;
	}


	
	/**
	 * 清除工作配置表信息
	 * @param context
	 */
	public static void clearWorkSetting(Context context) {
		try {
			DbCacheUtils.getDb(context).deleteAll(WorkSetting.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 存储工作配置列表
	 * 
	 * @param context
	 * @param channel
	 */
	public static void saveWorkSetting(Context context, WorkSetting workSetting) {
		try {
			if (workSetting == null) {
				return;
			}
			DbCacheUtils.getDb(context).saveOrUpdate(workSetting);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}



}
