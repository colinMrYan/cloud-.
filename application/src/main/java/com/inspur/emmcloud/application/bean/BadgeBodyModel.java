package com.inspur.emmcloud.application.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/11/26.
 */

public class BadgeBodyModel {

    private String COM_INSPUR_ECM_CHAT = "com.inspur.ecm.chat";
    private String COM_INSPUR_EMM_APP_STORE = "com.inspur.emm.app-store";
    private String COM_INSPUR_ECM_SNS = "com.inspur.ecm.sns";
    private BadgeBodyModuleModel chatBadgeBodyModuleModel;
    private BadgeBodyModuleModel appStoreBadgeBodyModuleModel;
    private BadgeBodyModuleModel snsBadgeBodyModuleModel;
    private String body = "";

    public BadgeBodyModel(String body) {
        this.body = body;
        snsBadgeBodyModuleModel = new BadgeBodyModuleModel(JSONUtils.getString(body, COM_INSPUR_ECM_SNS, ""));
        appStoreBadgeBodyModuleModel = new BadgeBodyModuleModel(JSONUtils.getString(body, COM_INSPUR_EMM_APP_STORE, ""));
        chatBadgeBodyModuleModel = new BadgeBodyModuleModel(JSONUtils.getString(body, COM_INSPUR_ECM_CHAT, ""));
    }

    public BadgeBodyModuleModel getChatBadgeBodyModuleModel() {
        return chatBadgeBodyModuleModel;
    }

    public void setChatBadgeBodyModuleModel(BadgeBodyModuleModel chatBadgeBodyModuleModel) {
        this.chatBadgeBodyModuleModel = chatBadgeBodyModuleModel;
    }

    public BadgeBodyModuleModel getAppStoreBadgeBodyModuleModel() {
        return appStoreBadgeBodyModuleModel;
    }

    public void setAppStoreBadgeBodyModuleModel(BadgeBodyModuleModel appStoreBadgeBodyModuleModel) {
        this.appStoreBadgeBodyModuleModel = appStoreBadgeBodyModuleModel;
    }

    public BadgeBodyModuleModel getSnsBadgeBodyModuleModel() {
        return snsBadgeBodyModuleModel;
    }

    public void setSnsBadgeBodyModuleModel(BadgeBodyModuleModel snsBadgeBodyModuleModel) {
        this.snsBadgeBodyModuleModel = snsBadgeBodyModuleModel;
    }

    public boolean isChatExist() {
        return JSONUtils.getJSONObject(body).has(COM_INSPUR_ECM_CHAT);
    }

    public boolean isAppStoreExist() {
        return JSONUtils.getJSONObject(body).has(COM_INSPUR_EMM_APP_STORE);
    }

    public boolean isSNSExist() {
        return JSONUtils.getJSONObject(body).has(COM_INSPUR_ECM_SNS);
    }
}
