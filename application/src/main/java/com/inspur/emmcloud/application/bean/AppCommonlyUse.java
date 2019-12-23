package com.inspur.emmcloud.application.bean;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "com_inspur_emmcloud_bean_AppCommonlyUse")
public class AppCommonlyUse {

    @Column(name = "appID", isId = true)
    private String appID = "";
    @Column(name = "lastUpdateTime")
    private long lastUpdateTime = 0L;
    @Column(name = "clickCount")
    private int clickCount = 0;
    @Column(name = "weight")
    private double weight = 0;

    public AppCommonlyUse() {
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof AppCommonlyUse)) {
            return false;
        }
        AppCommonlyUse appCommonlyUse = (AppCommonlyUse) other;
        return getAppID().equals(appCommonlyUse.getAppID());
    }

}
