package com.inspur.emmcloud.basemodule.api;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;

/**
 * Created by chenmch on 2019/6/5.
 */

public class BaseModuleApiUri {
    /**
     * PV收集
     *
     * @return
     */
    public static String getUploadPVCollectUrl() {
        return "https://uvc1.inspuronline.com/clientpv";
    }

    /**
     * 获取语言的接口
     *
     * @return
     */
    public static String getLangUrl() {
        return WebServiceRouterManager.getInstance().getClusterEcm() + "/" + BaseApplication.getInstance().getTanent() + "/settings/lang";
    }

    /**
     * 获取通用检查url
     *
     * @return
     */
    public static String getAllConfigVersionUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v6.0/config/Check";
    }

    /**
     * 获取上传推送信息的url
     *
     * @return
     */
    public static String getUploadPushInfoUrl() {
        return WebServiceRouterManager.getInstance().getClusterClientRegistry() + "/client";
    }

    /**
     * 向emm注册推送token的url
     * 固定地址
     *
     * @return
     */
    public static String getRegisterPushTokenUrl() {
        return "https://emm.inspuronline.com/api/sys/v6.0/config/registerDevice";
    }

    /**
     * 解除注册token的url
     * 固定地址
     *
     * @return
     */
    public static String getUnRegisterPushTokenUrl() {
        return "https://emm.inspuronline.com/api/sys/v6.0/config/unRegisterDevice";
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public static String getMyInfoUrl() {
        return WebServiceRouterManager.getInstance().getIDMUrl() + "oauth2.0/profile";
    }

}
