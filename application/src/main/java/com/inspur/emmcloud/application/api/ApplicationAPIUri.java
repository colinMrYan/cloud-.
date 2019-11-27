package com.inspur.emmcloud.application.api;

import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;

/**
 * Created by: yufuchang
 * Date: 2019/11/27
 */
public class ApplicationAPIUri {

    /**
     * 添加app
     *
     * @returnsunqx
     */
    public static String addApp() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/installApp";
    }

    /**
     * 移除app
     *
     * @return
     */
    public static String removeApp() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/uninstallApp";
    }

    /**
     * 获取所有App以及查询app
     *
     * @return
     */
    public static String getAllApps() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/getAllApps";
    }

    /**
     * 获取应用详情
     *
     * @return
     */
    public static String getAppInfo() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/getAppInfo";
    }


    /**
     * 存储app配置url
     *
     * @param key
     * @return
     */
    public static String saveAppConfigUrl(String key) {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v6.0/config/" + key;
    }

    /**
     * 获取我的应用小部件的url
     *
     * @return
     */
    public static String getMyAppWidgetsUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mam/v6.0/app/recommend/apps";
    }
}
