package com.inspur.emmcloud.bean.chat;

import org.json.JSONException;
import org.json.JSONObject;

public class GetMsgResult {

    private Msg msg;

    public GetMsgResult(String response) {
        try {
            JSONObject msgObj = new JSONObject(response);
            this.msg = new Msg(msgObj);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Msg getMsg() {
        return msg;
    }
}
