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

    /**
     * 微信 电子发票获取  token 和 ticket
     */
    public static String getWechatTicketUrl() {
        return "https://emm.inspuronline.com/" + "wechat/invoice/TokenAndTicket";
    }

}
