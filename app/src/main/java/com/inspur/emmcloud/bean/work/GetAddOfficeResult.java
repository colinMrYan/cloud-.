package com.inspur.emmcloud.bean.work;

import org.json.JSONObject;

public class GetAddOfficeResult {

    private String id;

    public GetAddOfficeResult(String response) {
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
