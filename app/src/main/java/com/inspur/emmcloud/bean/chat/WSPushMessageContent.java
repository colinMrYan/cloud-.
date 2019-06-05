package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/4/24.
 */

public class WSPushMessageContent {
    private Message message;
    private String tracer;

    public WSPushMessageContent(String content) {
        JSONObject messageObj = JSONUtils.getJSONObject(content, "body", new JSONObject());
        message = new Message(messageObj);
        JSONObject headerObj = JSONUtils.getJSONObject(content, "header", new JSONObject());
        LogUtils.jasonDebug("headerObj=" + headerObj);
        tracer = JSONUtils.getString(headerObj, "tracer", "");
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getTracer() {
        return tracer;
    }

    public void setTracer(String tracer) {
        this.tracer = tracer;
    }
}
