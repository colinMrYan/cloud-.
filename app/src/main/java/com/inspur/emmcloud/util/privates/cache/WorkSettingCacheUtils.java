package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.bean.schedule.WorkSetting;

import java.util.ArrayList;
import java.util.List;

/**
 * 频道列表缓存处理类
 *
 * @author Administrator
 */
public class WorkSettingCacheUtils {


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
            DbCacheUtils.getDb(context).saveOrUpdate(workSettingList);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 获取所有的工作配置列表
     *
     * @param context
     * @return
     */
    public static List<WorkSetting> getAllWorkSettingList(Context context) {
        List<WorkSetting> workSettingList = null;
        try {
            workSettingList = DbCacheUtils.getDb(context).selector(WorkSetting.class).orderBy("sort").findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (workSettingList == null) {
            workSettingList = new ArrayList<>();
        }
        return workSettingList;
    }

    /**
     * 获取所有打开的的工作配置列表
     *
     * @param context
     * @return
     */
    public static List<WorkSetting> getOpenWorkSettingList(Context context) {
        List<WorkSetting> workSettingList = null;
        try {
            workSettingList = DbCacheUtils.getDb(context).selector(WorkSetting.class).where("isOpen", "=", true).orderBy("sort").findAll();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if (workSettingList == null) {
            workSettingList = new ArrayList<>();
        }
        return workSettingList;
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
