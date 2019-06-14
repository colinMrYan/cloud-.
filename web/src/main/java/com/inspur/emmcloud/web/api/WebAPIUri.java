package com.inspur.emmcloud.web.api;

import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;

/**
 * Created by chenmch on 2019/6/14.
 */

public class WebAPIUri {
    /**
     * 网页登录
     *
     * @return
     */
    public static String getWebLoginUrl() {
        return WebServiceRouterManager.getInstance().getIDMUrl() + "oauth2.0/authorize";
    }

}
