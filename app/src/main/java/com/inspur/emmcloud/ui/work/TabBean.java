package com.inspur.emmcloud.ui.work;

import com.inspur.emmcloud.bean.system.MainTabResult;

/**
 * Created by yufuchang on 2017/4/28.
 */

public class TabBean {

    private String tabName = "";
    private String tabIcon = "";
    private Class<?> clz;
    private String tabId = "";
    private MainTabResult mainTabResult;

    public TabBean(String tabName, String tabIcon, Class<?> clz,MainTabResult mainTabResult) {
        this.clz = clz;
        this.tabName = tabName;
        this.tabIcon = tabIcon;
        this.mainTabResult = mainTabResult;
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

    public MainTabResult getMainTabResult() {
        return mainTabResult;
    }

    public void setMainTabResult(MainTabResult mainTabResult) {
        this.mainTabResult = mainTabResult;
    }
}
