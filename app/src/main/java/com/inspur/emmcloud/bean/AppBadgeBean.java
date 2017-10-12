package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/10/11.
 */

public class AppBadgeBean {
    private String appId = "";
    private int badgeNum = 0;

    public AppBadgeBean(JSONObject appBadgeObj){
        appId = JSONUtils.getString(appBadgeObj,"appId","");
        badgeNum = JSONUtils.getInt(appBadgeObj,"badge",0);
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getBadgeNum() {
        return badgeNum;
    }

    public void setBadgeNum(int badgeNum) {
        this.badgeNum = badgeNum;
    }
}
