package com.inspur.emmcloud.bean.system.badge;

import com.inspur.emmcloud.util.common.JSONUtils;

/**
 * Created by yufuchang on 2018/11/26.
 */

public class GetWebSocketBadgeResult {
    private BadgeBodyModel badgeBodyModel;
    public GetWebSocketBadgeResult(String response){
        badgeBodyModel = new BadgeBodyModel(JSONUtils.getString(response,"content",""));
    }

    public BadgeBodyModel getBadgeBodyModel() {
        return badgeBodyModel;
    }

    public void setBadgeBodyModel(BadgeBodyModel badgeBodyModel) {
        this.badgeBodyModel = badgeBodyModel;
    }
}
