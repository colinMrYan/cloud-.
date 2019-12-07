package com.inspur.emmcloud.application.bean;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/2/23.
 * ｛“clientId”：“blahblah”｝
 */

public class GetClientIdRsult {
    private String clientId = "";

    public GetClientIdRsult(String clientId) {
        try {
            JSONObject jsonObject = new JSONObject(clientId);
            if (jsonObject.has("clientId")) {
                this.clientId = jsonObject.getString("clientId");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
