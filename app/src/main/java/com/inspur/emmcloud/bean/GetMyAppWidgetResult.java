package com.inspur.emmcloud.bean;

/**
 * Created by yufuchang on 2017/11/10.
 */

public class GetMyAppWidgetResult {
    private String response = "";
    public GetMyAppWidgetResult(String response){
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
