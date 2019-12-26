package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.basemodule.app.maintab.MainTabResult;

/**
 * Created by yufuchang on 2017/4/28.
 */

public class TabBean {

    private String tabName = "";
    private Class<?> clz;
    private String tabId = "";
    private MainTabResult mainTabResult;

    public TabBean(String tabName, Class<?> clz, MainTabResult mainTabResult) {
        this.clz = clz;
        this.tabName = tabName;
        this.mainTabResult = mainTabResult;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
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
