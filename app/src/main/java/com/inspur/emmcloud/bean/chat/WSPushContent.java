package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;

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
    public WSPushContent(String content){
        body = JSONUtils.getString(content,"body","");
        JSONObject headerObj = JSONUtils.getJSONObject(content,"header",new JSONObject());
        tracer = JSONUtils.getString(headerObj,"tracer","");
        String action = JSONUtils.getString(content,"action","");
        JSONObject actionObj = JSONUtils.getJSONObject(content,"action",new JSONObject());
        method = JSONUtils.getString(actionObj,"method","");
        path = JSONUtils.getString(actionObj,"path","");
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
}
