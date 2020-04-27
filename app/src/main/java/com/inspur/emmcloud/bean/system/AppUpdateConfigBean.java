package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by: yufuchang
 * Date: 2020/4/2
 * {
 *  "newVersionURL": "https://xxxxx",
 *  "tip": "请升级新版本",
 *  "helpURL": "https://xxxxx"
 * }
 */
public class AppUpdateConfigBean {
    private String newVersionURL = "";
    private String tip = "";
    private String helpURL = "";
    public AppUpdateConfigBean(String response){
        this.newVersionURL = JSONUtils.getString(response,"newVersionURL","");
        this.tip = JSONUtils.getString(response,"tip","");
        this.helpURL = JSONUtils.getString(response,"helpURL","");
    }

    public String getNewVersionURL() {
        return newVersionURL;
    }

    public void setNewVersionURL(String newVersionURL) {
        this.newVersionURL = newVersionURL;
    }

    public String getNewVersionTip() {
        return tip;
    }

    public void setNewVersionTip(String tip) {
        this.tip = tip;
    }

    public String getHelpURL() {
        return helpURL;
    }

    public void setHelpURL(String helpURL) {
        this.helpURL = helpURL;
    }
}
