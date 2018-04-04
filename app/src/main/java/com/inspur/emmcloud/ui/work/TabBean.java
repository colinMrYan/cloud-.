package com.inspur.emmcloud.ui.work;

/**
 * Created by yufuchang on 2017/4/28.
 */

public class TabBean {

    private String tabName = "";
    private String tabIcon = "";
    private Class<?> clz;
    private String tabId = "";

    public TabBean(String tabName, String tabIcon, Class<?> clz) {
        this.clz = clz;
        this.tabName = tabName;
        this.tabIcon = tabIcon;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public String getTabIcon() {
        return tabIcon;
    }

    public void setTabIcon(String tabIcon) {
        this.tabIcon = tabIcon;
    }

    public Class<?> getClz() {
        return clz;
    }

    public void setClz(Class<?> clz) {
        this.clz = clz;
    }

    public String getTabId() {
        return tabId;
    }

    public void setTabId(String tabId) {
        this.tabId = tabId;
    }
}
