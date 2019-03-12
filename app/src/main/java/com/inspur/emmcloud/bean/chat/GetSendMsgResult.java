package com.inspur.emmcloud.bean.chat;

import org.json.JSONObject;

public class GetSendMsgResult {

    private Msg msg;
    private JSONObject msgJsonObj;

    public GetSendMsgResult(String response) {

        try {
            JSONObject msgObj = new JSONObject(response);
            this.msg = new Msg(msgObj);
            this.msgJsonObj = msgObj;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Msg getMsg() {
        return msg;
    }

    public void setMsg(Msg msg) {
        this.msg = msg;
    }

    public JSONObject getMsgJsonObj() {
        return msgJsonObj;
    }

    public void setMsgJsonObj(JSONObject msgJsonObj) {
        this.msgJsonObj = msgJsonObj;
    }
}
