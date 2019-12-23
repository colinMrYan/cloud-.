package com.inspur.emmcloud.application.bean;


/**
 * 登录信息返回解析类
 */
public class GetAddAppResult {

    private static final String TAG = "GetAddAppResult";
    private String appID = "";

    public GetAddAppResult(String response, String appID) {
        this.appID = appID;

    }


    public String getAppID() {
        return appID;
    }

}
