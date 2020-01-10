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

    /**
     * 获取app真实地址
     *
     * @param appId
     * @return
     */
    public static String getAppRealUrl(String appId) {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mam/v3.0/gs_sso/app_uri?id=" + appId;
    }

    /**
     * 行政审批验证密码
     */
    public static String getVeriryApprovalPasswordUrl() {
        return "http://ishenpi.inspur.com:8090/Inspur/login.jhtml?";
    }


    /**
     * 获取clientid的
     *
     * @return
     */
    public static String getClientId() {
        return WebServiceRouterManager.getInstance().getClusterDistribution() + "/client/registry";
    }

    /**
     * ReactNative应用更新写回
     *
     * @return
     */
    public static String getReactNativeWriteBackUrl(String appModule) {
        return WebServiceRouterManager.getInstance().getClusterDistribution() + "/app/" + appModule + "/update";
    }

    /**
     * 获取所有App以及查询app
     *
     * @return
     */
    public static String getNewAllApps() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mam/v3.0/imp_app/appCenterList";
    }

    /**
     * 获取用户apps
     *
     * @return
     */
    public static String getUserApps() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mam/v3.1/imp_app/userApps";
    }
}
