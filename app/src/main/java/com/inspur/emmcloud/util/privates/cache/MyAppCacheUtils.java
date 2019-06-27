package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.appcenter.AppGroupBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2017/8/15.
 */

public class MyAppCacheUtils {

    /**
     * 保存应用列表数据
     *
     * @param context
     * @param appGroupList
     */
    public static void saveMyAppList(Context context, List<AppGroupBean> appGroupList) {
        String appListJson = JSONUtils.toJSONString(appGroupList);
        if (!appListJson.equals("null") && !StringUtils.isBlank(appListJson)) {
            PreferencesByUserAndTanentUtils.putString(context, Constant.APP_MYAPP_LIST_IN_CACHE, appListJson);
        }
    }

    /**
     * 获取常用应用数据，字符串形式
     *
     * @param context
     */
    public static String getMyAppListJson(Context context) {
        return PreferencesByUserAndTanentUtils.getString(context, Constant.APP_MYAPP_LIST_IN_CACHE, "");
    }

    /**
     * 获取应用列表
     *
     * @param context
     * @return
     */
    public static List<AppGroupBean> getMyAppList(Context context) {
        String appListJson = PreferencesByUserAndTanentUtils.getString(context, Constant.APP_MYAPP_LIST_IN_CACHE, "");
        List<AppGroupBean> appGroupList = null;
        if (!StringUtils.isBlank(appListJson)) {
            appGroupList = JSONUtils.parseArray(appListJson, AppGroupBean.class);
        }
        if (appGroupList == null) {
            appGroupList = new ArrayList<>();
        }
        return appGroupList;
    }

    /**
     * 保存从网络获取的应用列表数据
     *
     * @param context
     * @param appGroupList
     */
    public static void saveMyAppListFromNet(Context context, List<AppGroupBean> appGroupList) {
        String appListJson = JSONUtils.toJSONString(appGroupList);
        if (!appListJson.equals("null") && !StringUtils.isBlank(appListJson)) {
            PreferencesByUserAndTanentUtils.putString(context, Constant.APP_MYAPP_LIST_FROM_NET, appListJson);
            PreferencesByUserAndTanentUtils.putString(context, Constant.APP_MYAPP_LIST_IN_CACHE, appListJson);
        }
    }

    /**
     * 获取网络返回的应用列表
     *
     * @param context
     * @return
     */
    public static List<AppGroupBean> getMyAppListFromNet(Context context) {
        String appListJson = PreferencesByUserAndTanentUtils.getString(context, Constant.APP_MYAPP_LIST_FROM_NET, "");
        List<AppGroupBean> appGroupList = null;
        if (!StringUtils.isBlank(appListJson)) {
            appGroupList = JSONUtils.parseArray(appListJson, AppGroupBean.class);
        }
        if (appGroupList == null) {
            appGroupList = new ArrayList<>();
        }
        return appGroupList;
    }

    /**
     * 清除常用应用缓存
     *
     * @param context
     * @return
     */
    public static boolean clearMyAppList(Context context) {
        return PreferencesByUserAndTanentUtils.putString(context, Constant.APP_MYAPP_LIST_IN_CACHE, "");
    }

}
