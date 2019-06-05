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

}
