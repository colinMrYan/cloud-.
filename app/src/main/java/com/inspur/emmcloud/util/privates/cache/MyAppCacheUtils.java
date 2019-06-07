package com.inspur.emmcloud.util.privates.cache;

import android.content.Context;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.bean.appcenter.AppGroupBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2017/8/15.
 */

public class MyAppCacheUtils {

    /**
     * 保存常用应用数据
     *
     * @param context
     * @param appGroupList
     */
    public static void saveMyAppList(Context context, List<AppGroupBean> appGroupList) {
        String appListJson = JSONUtils.toJSONString(appGroupList);
        if (!appListJson.equals("null") && !StringUtils.isBlank(appListJson)) {
            PreferencesByUserAndTanentUtils.putString(context, "my_app_list", appListJson);
        }
    }

    /**
     * 获取常用应用数据，字符串形式
     *
     * @param context
     */
    public static String getMyAppListJson(Context context) {
        return PreferencesByUserAndTanentUtils.getString(context, "my_app_list", "");
    }

    /**
     * 获取常用应用列表
     *
     * @param context
     * @return
     */
    public static List<AppGroupBean> getMyAppList(Context context) {
        String appListJson = PreferencesByUserAndTanentUtils.getString(context, "my_app_list", "");
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
     * 获取是否含有常用应用标志
     *
     * @param context
     * @return
     */
    public static boolean getHasCommonlyApp(Context context) {
        return PreferencesByUserAndTanentUtils.getBoolean(context, "is_has_commonly_app", false);
    }

    /**
     * 清除常用应用缓存
     *
     * @param context
     * @return
     */
    public static boolean clearMyAppList(Context context) {
        return PreferencesByUserAndTanentUtils.putString(context, "my_app_list", "");
    }

}
