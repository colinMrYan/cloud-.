package com.inspur.emmcloud.schedule.bean.task;

import org.json.JSONObject;

public class GetTaskAddResult {

    private String id = "";

    public GetTaskAddResult(String response) {
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(response);
            if (jsonObject.has("id")) {
                this.id = jsonObject.getString("id");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
