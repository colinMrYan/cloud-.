package com.inspur.emmcloud.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.inspur.emmcloud.bean.Robot;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

/**
 * 机器人增删改查
 *
 */
public class RobotCacheUtils {

	/**
	 * 保存或更新机器人信息
	 * 
	 * @param context
	 * @param robotList
	 */
	public static void saveOrUpdateRobotList(Context context,
			List<Robot> robotList) {
		if (robotList == null || robotList.size() == 0) {
			return;
		}
		try {
			DbCacheUtils.getDb(context).saveOrUpdateAll(robotList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取所有Robot
	 * 
	 * @return
	 */
	public static List<Robot> getRobotList(Context context) {
		List<Robot> robotList = null;
		try {
			robotList = DbCacheUtils.getDb(context).findAll(
					Selector.from(Robot.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (robotList == null) {
			robotList = new ArrayList<Robot>();
		}
		return robotList;
	}

	/**
	 * 根据id获得机器人，如果为空到父方法处理
	 * 
	 * @param context
	 * @param id
	 * @return
	 */
	public static Robot getRobotById(Context context, String id) {
		Robot robot = null;
		try {
			robot = DbCacheUtils.getDb(context).findFirst(
					Selector.from(Robot.class).where("id", "=", id));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return robot;
	}

	/**
	 * 搜索robot，提供与通讯录同类型方法，
	 * 传参数与通讯录相同，由于robot信息较少所以
	 * @param context
	 * @param searchString
	 * @param currentId
	 * @param offset
	 * @param limit
	 * @return
	 */
	public static List<Robot> getSearchRobotList(Context context,
			String searchString, String currentId, int offset, int limit) {
		List<Robot> robotList = null;
		String search = "";
		for (int i = 0; i < searchString.length(); i++) {
			if (i < searchString.length() - 1) {
				search = search + "%" + searchString.charAt(i);
			} else {
				search = search + "%" + searchString + "%";
			}
		}
		try {
			if (StringUtils.isBlank(currentId)) {
				robotList = DbCacheUtils.getDb(context).findAll(
						Selector.from(Robot.class)
								.where("name", "like", search).offset(offset)
								.limit(limit));
			} else {
				robotList = DbCacheUtils.getDb(context).findAll(
						Selector.from(Robot.class)
								.where("name", "like", search));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (robotList == null) {
			robotList = new ArrayList<Robot>();
		}
		return robotList;
	}
	
	/**
	 * 删除一个机器人
	 * @param context
	 * @param robotId
	 */
	public static void deleteRobotById(Context context,String robotId){
		try {
			DbCacheUtils.getDb(context).deleteById(Robot.class, robotId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 删除一组机器人，参数是需要删除的Robot的Idlist
	 * @param context
	 * @param idList
	 */
	public static void deleteRobotByIdList(Context context,ArrayList<String> idList){
		for (int i = 0; i < idList.size(); i++) {
			try {
				DbCacheUtils.getDb(context).deleteById(Robot.class, idList.get(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
