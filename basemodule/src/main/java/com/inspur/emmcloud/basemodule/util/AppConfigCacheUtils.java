package com.inspur.emmcloud.basemodule.util;

import android.content.Context;

import com.inspur.emmcloud.basemodule.bean.AppConfig;

import java.util.List;

/**
 * Created by chenmch on 2017/10/14.
 */

public class AppConfigCacheUtils {

    /**
     * 存储应用配置列表
     *
     * @param context
     * @param appConfigList
     */
    public static void saveAppConfigList(Context context, List<AppConfig> appConfigList) {
        try {
            if (appConfigList == null || appConfigList.size() == 0) {
                return;
            }
            DbCacheUtils.getDb(context).saveOrUpdate(appConfigList);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    /**
     * 存储单个配置
     *
     * @param context
     * @param appConfig
     */
    public static void saveAppConfig(Context context, AppConfig appConfig) {
        try {
            DbCacheUtils.getDb(context).saveOrUpdate(appConfig);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 先清空所有的配置列表再进行存储
     *
     * @param context
     * @param appConfigList
     */
    public static void clearAndSaveAppConfigList(Context context, List<AppConfig> appConfigList) {
        try {
            DbCacheUtils.getDb(context).delete(AppConfig.class);
            saveAppConfigList(context, appConfigList);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 获取App的某一项配置
     *
     * @param context
     * @param id
     * @param defaultValue
     * @return
     */
    public static String getAppConfigValue(Context context, String id, String defaultValue) {
        try {
            AppConfig appConfig = DbCacheUtils.getDb(context).findById(AppConfig.class, id);
            if (appConfig != null) {
                return appConfig.getValue();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }
}
