package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/4/24.
 */

public class WSPushContent {
    private String body;
    private String tracer;
    private String method;
    private String path;
    private String action;
    private int status;

    public WSPushContent(String content) {
        body = JSONUtils.getString(content, "body", "");
        JSONObject headerObj = JSONUtils.getJSONObject(content, "headers", new JSONObject());
        tracer = JSONUtils.getString(headerObj, "tracer", "");
        String action = JSONUtils.getString(content, "action", "");
        status = JSONUtils.getInt(action, "status", 200);
        JSONObject actionObj = JSONUtils.getJSONObject(content, "action", new JSONObject());
        method = JSONUtils.getString(actionObj, "method", "");
        path = JSONUtils.getString(actionObj, "path", "");
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTracer() {
        return tracer;
    }

    public void setTracer(String tracer) {
        this.tracer = tracer;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
