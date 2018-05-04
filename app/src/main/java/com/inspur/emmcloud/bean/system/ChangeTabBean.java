package com.inspur.emmcloud.bean.system;

/**
 * Created by yufuchang on 2018/4/3.
 */

public class ChangeTabBean {
    private String tabId = "";
    public ChangeTabBean(String tabId){
        this.tabId = tabId;
    }

    public String getTabId() {
        return tabId;
    }

    public void setTabId(String tabId) {
        this.tabId = tabId;
    }
}
