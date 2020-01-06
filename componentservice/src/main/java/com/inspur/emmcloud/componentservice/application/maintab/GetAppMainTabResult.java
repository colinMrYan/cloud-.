package com.inspur.emmcloud.componentservice.application.maintab;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/7/12.
 */

public class GetAppMainTabResult {

    private String response;
    private String command;
    private MainTabPayLoad mainTabPayLoad;

    public GetAppMainTabResult(){}

    public GetAppMainTabResult(String response) {
        this.command = JSONUtils.getString(response, "command", "");
        this.response = response;
        this.mainTabPayLoad = new MainTabPayLoad(JSONUtils.getJSONObject(response, "payload", new JSONObject()));
    }

    public String getAppTabInfo() {
        return response;
    }


    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public MainTabPayLoad getMainTabPayLoad() {
        return mainTabPayLoad;
    }

    public void setMainTabPayLoad(MainTabPayLoad mainTabPayLoad) {
        this.mainTabPayLoad = mainTabPayLoad;
    }
}
