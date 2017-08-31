package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.bean.Robot;

import java.util.ArrayList;
import java.util.List;

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
			DbCacheUtils.getDb(context).saveOrUpdate(robotList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 清空机器人信息
	 * @param context
	 */
	public static void clearRobotList(Context context){
		try {
			DbCacheUtils.getDb(context).delete(Robot.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			robot = DbCacheUtils.getDb(context).selector(Robot.class).where("id", "=", id).findFirst();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return robot;
	}


}
