package com.inspur.emmcloud.util;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.inspur.emmcloud.bean.AppGroupBean;

import java.util.List;

/**
 * Created by yufuchang on 2017/8/15.
 */

public class MyAppCacheUtils {

    /**
     * 保存常用应用数据
     * @param context
     * @param appsString
     */
    public static void saveMyApps(Context context, String appsString){
        PreferencesByUserAndTanentUtils.putString(context,"myapps",appsString);
    }

    /**
     * 获取常用应用列表
     * @param context
     * @return
     */
    public static List<AppGroupBean> getMyApps(Context context){
        String appsString = PreferencesByUserAndTanentUtils.getString(context,"myapps","");
        return JSON.parseArray(appsString,AppGroupBean.class);
    }

    /**
     * 保存是否含有常用应用标志
     * @param hasCommonlyApp
     */
    public static void saveHasCommonlyApp(Context context,boolean hasCommonlyApp){
        PreferencesByUserAndTanentUtils.putBoolean(context,"isHasCommonlyApp",hasCommonlyApp);
    }

    /**
     * 获取是否含有常用应用标志
     * @param context
     * @return
     */
    public  static boolean getHasCommonlyApp(Context context){
        return PreferencesByUserAndTanentUtils.getBoolean(context,"isHasCommonlyApp",false);
    }
}
