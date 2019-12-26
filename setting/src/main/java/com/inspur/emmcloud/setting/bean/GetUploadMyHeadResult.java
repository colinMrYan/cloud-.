package com.inspur.emmcloud.setting.bean;

import org.json.JSONObject;

public class GetUploadMyHeadResult {

    private static final String TAG = "GetUploadMyHeadResult";
    private String url;

    public GetUploadMyHeadResult(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            this.url = jsonObject.getString("head");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String code) {
        this.url = code;
    }
}
