package com.inspur.emmcloud.basemodule.bean.badge;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/11/26.
 */

public class GetWebSocketBadgeResult {
    private BadgeActionModel badgeActionModel;
    private BadgeHeadersModel badgeHeadersModel;
    private BadgeBodyModel badgeBodyModel;

    public GetWebSocketBadgeResult(String response) {
        badgeActionModel = new BadgeActionModel(JSONUtils.getString(response, "action", ""));
        badgeHeadersModel = new BadgeHeadersModel(JSONUtils.getString(response, "headers", ""));
        badgeBodyModel = new BadgeBodyModel(JSONUtils.getString(response, "content", ""));
    }

    public BadgeBodyModel getBadgeBodyModel() {
        return badgeBodyModel;
    }

    public void setBadgeBodyModel(BadgeBodyModel badgeBodyModel) {
        this.badgeBodyModel = badgeBodyModel;
    }

    public BadgeActionModel getBadgeActionModel() {
        return badgeActionModel;
    }

    public void setBadgeActionModel(BadgeActionModel badgeActionModel) {
        this.badgeActionModel = badgeActionModel;
    }

    public BadgeHeadersModel getBadgeHeadersModel() {
        return badgeHeadersModel;
    }

    public void setBadgeHeadersModel(BadgeHeadersModel badgeHeadersModel) {
        this.badgeHeadersModel = badgeHeadersModel;
    }
}
