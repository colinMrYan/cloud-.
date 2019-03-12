package com.inspur.emmcloud.bean.chat;

import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/15.
 */

public class GetMsgCommentCountResult {
    private int count = 0;

    public GetMsgCommentCountResult(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.has("count")) {
                count = obj.getInt("count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
