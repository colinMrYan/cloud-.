package com.inspur.emmcloud.util;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.bean.AppGroupBean;
import com.inspur.emmcloud.config.MyAppConfig;

import java.util.List;

/**
 * Created by yufuchang on 2017/8/15.
 */

public class MyAppCacheUtils {

    /**
     * 保存常用应用数据
     * @param context
     * @param appGroupList
     */
    public static void saveMyAppList(Context context, List<AppGroupBean> appGroupList){
        String appList = JSON.toJSONString(appGroupList);
        WriteLongLog2FileUtils.writeTxtToFile(appList, MyAppConfig.LOCAL_CACHE_PATH+"appcache/","save.txt");
        if(!appList.equals("null") && !StringUtils.isBlank(appList)){
            PreferencesByUserAndTanentUtils.putString(context,"my_app_list",appList);
        }
    }

    /**
     * 获取常用应用数据，字符串形式
     * @param context
     */
    public static String getMyAppsData(Context context){
        return PreferencesByUserAndTanentUtils.getString(context,"my_app_list","");
    }

    /**
     * 获取常用应用列表
     * @param context
     * @return
     */
    public static List<AppGroupBean> getMyApps(Context context){
        String appsString = PreferencesByUserAndTanentUtils.getString(context,"my_app_list","");
        WriteLongLog2FileUtils.writeTxtToFile(appsString, MyAppConfig.LOCAL_CACHE_PATH+"appcache/","get.txt");
        return JSON.parseArray(appsString,AppGroupBean.class);
    }

    /**
     * 保存是否含有常用应用标志
     * @param hasCommonlyApp
     */
    public static void saveHasCommonlyApp(Context context,boolean hasCommonlyApp){
        PreferencesByUserAndTanentUtils.putBoolean(context,"is_has_commonly_app",hasCommonlyApp);
    }

    /**
     * 获取是否含有常用应用标志
     * @param context
     * @return
     */
    public  static boolean getHasCommonlyApp(Context context){
        return PreferencesByUserAndTanentUtils.getBoolean(context,"is_has_commonly_app",false);
    }

}
