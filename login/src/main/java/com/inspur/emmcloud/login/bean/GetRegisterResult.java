package com.inspur.emmcloud.login.bean;

import org.json.JSONObject;

public class GetRegisterResult {

    private static final String TAG = "GetRegisterResult";
    private String code;

    public GetRegisterResult(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            this.code = jsonObject.getString("code");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
