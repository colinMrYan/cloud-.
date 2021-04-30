package com.inspur.emmcloud.setting.api;

import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;

/**
 * Created by libaochao on 2019/12/25.
 */

public class SettingAPIUri {

    /**
     * 获得推荐云+页面url
     *
     * @return
     */
    public static String getRecommandAppUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "admin/share_qr";
    }

    /**
     * 获取我的信息展示配置
     *
     * @return
     */
    public static String getUserProfileUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v3.0/userprofile/displayconfig";
    }

    /**
     * 获取个人信息及其显示配置
     */
    public static String getUserProfileAndDisPlayUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v3.0/userprofile/detail";
    }


    /**
     * 修改用户头像
     *
     * @param
     */
    public static String getUpdateUserHeadUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v3.0/user/update_head";
    }

    /**
     * 修改用户信息
     *
     * @return
     */
    public static String getModifyUserInfoUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api?module=user&method=update_baseinfo";
    }


    /**
     * 设置人脸头像
     *
     * @return
     */
    public static String getFaceSettingUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v6.0/face/save";
    }

    /**
     * 脸部图像验证
     *
     * @return
     */
    public static String getFaceVerifyUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v6.0/face/verify";
    }

    /***********设备管理******************
     /**
     * 获取解绑设备url
     *
     * @return
     */
    public static String getUnBindDeviceUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mdm/v3.0/device/unbind ";
    }

    /**
     * 获取绑定设备
     *
     * @return
     */
    public static String getBindingDevicesUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mdm/v3.0/device/getUserDevices";
    }


    /**
     * 获取绑定设备
     *
     * @return
     */
    public static String getDeviceLogUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/mdm/v3.0/device/getDeviceLogs";
    }

    /**
     * 获取用户注销url
     * @return
     */
    public static String getAccountLogOffUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v3.0/user/close_account";
    }


    /**
     * 获取MDM启用状态
     *
     * @return
     */
    public static String getMDMStateUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v3.0/userprofile/mdm_state";
    }


    /**
     * 获取卡包信息
     *
     * @return
     */
    public static String getCardPackageUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/buildinapp/v6.0/CardPackage";
    }

    /**
     * 获取是否打开体验升级
     *
     * @return
     */
    public static String getUserExperienceUpgradeFlagUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v3.0/upgrade/checkExperiencePlan";
    }

    public static String getUpdateUserExperienceUpgradeFlagUrl(int flag) {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v3.0/upgrade/joinExperiencePlan?flag=" + flag;
    }

    /**
     * 获取我的页面个人信息卡片的menu配置
     *
     * @return
     */
    public static String getUserCardMenusUrl() {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v6.0/config/UserCardMenus";
    }

    /**
     * 存储app配置url
     *
     * @param key
     * @return
     */
    public static String seetingSaveAppConfigUrl(String key) {
        return WebServiceRouterManager.getInstance().getClusterEmm() + "api/sys/v6.0/config/" + key;
    }


}
