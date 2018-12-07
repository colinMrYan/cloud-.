package com.inspur.emmcloud.bean.system.badge;

import com.inspur.emmcloud.util.common.JSONUtils;

/**
 * Created by yufuchang on 2018/11/26.
 */

public class BadgeBodyModel {

    private BadgeBodyModuleModel chatBadgeBodyModuleModel;
    private BadgeBodyModuleModel appStoreBadgeBodyModuleModel;
    private BadgeBodyModuleModel snsBadgeBodyModuleModel;
    public BadgeBodyModel(String body){
        chatBadgeBodyModuleModel = new BadgeBodyModuleModel(JSONUtils.getString(body,"com.inspur.ecm.chat",""));
        appStoreBadgeBodyModuleModel = new BadgeBodyModuleModel(JSONUtils.getString(body,"com.inspur.emm.app-store",""));
        snsBadgeBodyModuleModel = new BadgeBodyModuleModel(JSONUtils.getString(body,"com.inspur.ecm.sns",""));
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
}
