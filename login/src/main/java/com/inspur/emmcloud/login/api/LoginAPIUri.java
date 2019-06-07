package com.inspur.emmcloud.login.api;

import android.content.Context;

import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;

/**
 * Created by chenmch on 2019/6/7.
 */

public class LoginAPIUri {
    /**
     * 获取oauth认证的基础
     *
     * @return
     */
    public static String getOauthSigninUrl() {
        return WebServiceRouterManager.getInstance().getIDMUrl() + "oauth2.0/token";
    }


    public static String getCancelTokenUrl() {
        return WebServiceRouterManager.getInstance().getIDMUrl() + "oauth2.0/profile";
    }


    /**
     * 请求短信验证码
     *
     * @param mobile
     * @return
     */
    public static String getLoginSMSCaptchaUrl(String mobile) {
        return WebServiceRouterManager.getInstance().getIDMUrl() + "api/v1/passcode?phone=" + mobile;
    }

    /**
     * 验证短信验证码
     *
     * @return
     */
    public static String getSMSRegisterCheckUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "/api?module=register&method=verify_smscode";
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public static String getMyInfoUrl() {
        return WebServiceRouterManager.getInstance().getIDMUrl() + "oauth2.0/profile";
    }

    /**
     * 修改密码
     **/
    public static String getChangePsdUrl() {
        return WebServiceRouterManager.getInstance().getIDMUrl() + "console/api/v1/account/password";
    }


    /**
     * 返回我的信息
     *
     * @return
     */
    public static String getOauthMyInfoUrl() {
        return WebServiceRouterManager.getInstance().getIDMUrl() + "oauth2.0/token/profile";
    }

    /**
     * 刷新token
     *
     * @return
     */
    public static String getRefreshToken() {
        return WebServiceRouterManager.getInstance().getIDMUrl() + "oauth2.0/token";
    }

    /**
     * 获取设备注册URl
     *
     * @param context
     * @return
     */
    public static String getDeviceRegisterUrl(Context context) {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "app/mdm/v3.0/loadForRegister?udid=" + AppUtils.getMyUUID(context);
    }

    /**
     * 设备检查
     *
     * @return
     */
    public static String getDeviceCheckUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mdm/v3.0/mdm/check_state";
    }


}
