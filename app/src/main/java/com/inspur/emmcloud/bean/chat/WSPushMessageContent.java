package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/4/24.
 */

public class WSPushMessageContent {
    private Message message;
    public WSPushMessageContent(String content){
        JSONObject messageObj = JSONUtils.getJSONObject(content,"body",new JSONObject());
        message = new Message(messageObj);
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
