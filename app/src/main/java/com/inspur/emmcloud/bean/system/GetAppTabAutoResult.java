package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.bean.system.AppTabAutoBean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/4/26.
 */

public class GetAppTabAutoResult {
    private String tabId = "";
    private String version = "";
    private String appTabInfo = "";
    private String command = "";

    public GetAppTabAutoResult(String responose){
        try {
            JSONObject jsonObject = new JSONObject(responose);
            if(jsonObject.has("command")){
                this.command = jsonObject.getString("command");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!command.equals("STANDBY")&&!command.equals("")){
            appTabInfo = responose;
            AppTabAutoBean appTabAutoBean = new AppTabAutoBean(responose);
            version = appTabAutoBean.getId().getVersion();
            command = appTabAutoBean.getCommand();
        }
    }

    public String getTabId() {
        return tabId;
    }

    public void setTabId(String tabId) {
        this.tabId = tabId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAppTabInfo() {
        return appTabInfo;
    }

    public void setAppTabInfo(String appTabInfo) {
        this.appTabInfo = appTabInfo;
    }

//    public AppTabAutoBean getAppTabAutoBean() {
//        return appTabAutoBean;
//    }
//
//    public void setAppTabAutoBean(AppTabAutoBean appTabAutoBean) {
//        this.appTabAutoBean = appTabAutoBean;
//    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
