package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.bean.chat.Robot;

import java.util.List;

/**
 * 机器人增删改查
 */
public class RobotCacheUtils {

    /**
     * 保存或更新机器人信息
     *
     * @param context
     * @param robotList
     */
    public static void saveRobotList(Context context, List<Robot> robotList) {
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
     * 保存机器人信息
     *
     * @param context
     * @param robot
     */
    public static void saveRobot(Context context, Robot robot) {
        if (robot == null) {
            return;
        }
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(robot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空机器人信息
     *
     * @param context
     */
    public static void clearRobotList(Context context) {
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
