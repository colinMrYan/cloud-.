package com.inspur.emmcloud.widget.draggrid;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.inspur.emmcloud.bean.AppItem;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;

public class AppDao{
	private static DbUtils db;

	public AppDao(Context context) {
		db = initDb(context);
	}

	public DbUtils initDb(Context context) {
		db = DbCacheUtils.getDb(context);
		db.configAllowTransaction(true);
		db.configDebug(false);
		return db;
	}

	/**
	 * 添加一个app
	 * @param item
	 * @return
	 */
	public boolean addApp(AppItem item) {
		boolean flag = false;
		try {
			db.saveOrUpdate(item);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	/**
	 * 更新App更新时间
	 * @param item
	 * @return
	 */
	public boolean updateAppTime(AppItem item) {
		boolean flag = false;
		try {
			db.saveOrUpdate(item);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	/**
	 * 获取常用app
	 * 
	 * @param appNumber
	 * @return
	 */
	public List<AppItem> getCommonlyUseApp(int appNumber) {
		List<AppItem> commonlyUseAppList = null;
		try {
			commonlyUseAppList = db.findAll(Selector.from(AppItem.class)
					.orderBy("lastUpdateTime",true).limit(appNumber));
		} catch (DbException e) {
			e.printStackTrace();
		}
		if (commonlyUseAppList == null) {
			commonlyUseAppList = new ArrayList<AppItem>();
		}
		return commonlyUseAppList;
	}

	/**
	 * 保存app
	 * @param appList
	 * @return
	 */
	public boolean saveAppList(List<AppItem> appList) {
		boolean flag = false;
		if (appList == null || appList.size() == 0) {
			return flag;
		}
		try {
			db.saveOrUpdateAll(appList);
			flag = true;
		} catch (DbException e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	/**
	 * 删除App
	 * @param groupNumber
	 * @return
	 */
	public boolean deleteAppCache(String groupNumber) {
		boolean flag = false;
		try {
			db.delete(AppItem.class,
					WhereBuilder.b("groupId", "=", groupNumber));
			flag = true;
		} catch (DbException e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	/**
	 * 
	 * @param selectId
	 * @return
	 */
	public List<AppItem> getAppListBySelectId(String selectId) {
		List<AppItem> appList = null;
		try {
			appList = db.findAll(Selector.from(AppItem.class).where(
					WhereBuilder.b("selected", "=", selectId)));
		} catch (DbException e) {
			e.printStackTrace();
		}
		if (appList == null) {
			appList = new ArrayList<AppItem>();
		}
		return appList;
	}

	public List<AppItem> getAppListByGroupId(String groupId) {
		List<AppItem> appList = null;
		try {
			appList = db.findAll(Selector.from(AppItem.class).where(
					WhereBuilder.b("groupId", "=", groupId)));
		} catch (DbException e) {
			e.printStackTrace();
		}
		if (appList == null) {
			appList = new ArrayList<AppItem>();
		}
		return appList;
	}

	/**
	 * 获取所有appItem
	 * 
	 * @return
	 */
	public List<AppItem> getAllAppList() {
		List<AppItem> appList = null;
		try {
			appList = db.findAll(Selector.from(AppItem.class));
		} catch (DbException e) {
			e.printStackTrace();
		}
		if (appList == null) {
			appList = new ArrayList<AppItem>();
		}
		return appList;
	}


	public void saveAppOrderList(List<AppItem> orderList, String orderGroup) {
		deleteAppCache(orderGroup);
		int count = orderList.size();
		for (int i = 0; i < count; i++) {
			AppItem appItem = (AppItem) orderList.get(i);
			appItem.setOrderId(i + "");
			addApp(appItem);
		}
	}

}
