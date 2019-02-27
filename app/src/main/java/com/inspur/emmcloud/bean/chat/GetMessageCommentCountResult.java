package com.inspur.emmcloud.bean.chat;

import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/15.
 */

public class GetMessageCommentCountResult {
    private int number = 0;

    public GetMessageCommentCountResult(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.has("number")) {
                number = obj.getInt("number");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
