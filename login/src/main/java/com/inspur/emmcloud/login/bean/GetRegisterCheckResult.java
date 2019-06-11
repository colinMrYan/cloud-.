package com.inspur.emmcloud.login.bean;

import org.json.JSONObject;

public class GetRegisterCheckResult {

    private static final String Tag = "GetRegisterCheckResult";
    private String registerID;

    public GetRegisterCheckResult(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            this.registerID = jsonObject.getString("register_id");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getRegisterID() {
        return registerID;
    }

    public void setRegisterID(String registerID) {
        this.registerID = registerID;
    }


}
