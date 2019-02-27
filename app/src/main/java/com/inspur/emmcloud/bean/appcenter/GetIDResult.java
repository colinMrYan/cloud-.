package com.inspur.emmcloud.bean.appcenter;

import org.json.JSONObject;

public class GetIDResult {
    private String id = "";

    public GetIDResult(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.has("id")) {

                id = obj.getString("id");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }
}
