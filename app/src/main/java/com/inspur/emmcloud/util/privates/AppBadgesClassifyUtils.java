package com.inspur.emmcloud.util.privates;

import com.inspur.emmcloud.bean.system.badge.BadgeBodyModel;
import com.inspur.emmcloud.bean.system.badge.BadgeBodyModuleModel;
import com.inspur.emmcloud.bean.system.badge.GetWebSocketBadgeResult;

import java.util.Map;

/**
 * Created by yufuchang on 2018/12/1.
 */

public class AppBadgesClassifyUtils {

    public static void classifyWebSocketBadgeResult(GetWebSocketBadgeResult getWebSocketBadgeResult){
        BadgeBodyModel badgeBodyModel = getWebSocketBadgeResult.getBadgeBodyModel();
        classifyHttpBadgeResult(badgeBodyModel);
    }

    public static void classifyHttpBadgeResult(BadgeBodyModel badgeBodyModel){
        BadgeBodyModuleModel chatBadgeBodyModuleModel = badgeBodyModel.getChatBadgeBodyModuleModel();
        BadgeBodyModuleModel appStoreBadgeBodyModuleModel = badgeBodyModel.getAppStoreBadgeBodyModuleModel();
        BadgeBodyModuleModel snsBadgeBodyModuleModel = badgeBodyModel.getSnsBadgeBodyModuleModel();
    }
}
