package com.inspur.emmcloud.basemodule.bean;

/**
 * Created by chenmch on 2018/7/27.
 */

public enum ClientConfigItem {
    CLIENT_CONFIG_MAINTAB("client_config_maintab"),
    CLIENT_CONFIG_LANGUAGE("client_config_language"),
    CLIENT_CONFIG_SPLASH("client_config_splash"),
    CLIENT_CONFIG_ROUTER("client_config_router"),
    CLIENT_CONFIG_MY_APP("client_config_my_app"),
    CLIENT_CONFIG_CONTACT_USER("client_config_contact_user"),
    CLIENT_CONFIG_CONTACT_ORG("client_config_contact_org"),
    CLIENT_CONFIG_NAVI_TAB("client_config_navi_tab");
    private final String value;

    ClientConfigItem(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
