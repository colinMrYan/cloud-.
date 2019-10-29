package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/10/25.
 */

public class WSCommand {
    private String id;
    private String channel;
    private String fromUid;
    private String fromName;
    private String action;
    private String params;
    private String from;
    private String tracer;
    private String request;

    public WSCommand(String request) {
        this.request = request;
        JSONObject requestObj = JSONUtils.getJSONObject(request);
        JSONObject headerObj = JSONUtils.getJSONObject(requestObj, "headers", new JSONObject());
        tracer = JSONUtils.getString(headerObj, "tracer", "");
        JSONObject bodyObj = JSONUtils.getJSONObject(requestObj, "body", new JSONObject());
        this.id = JSONUtils.getString(bodyObj, "id", "");
        this.channel = JSONUtils.getString(bodyObj, "channel", "");
        from = JSONUtils.getString(bodyObj, "from", "");
        JSONObject fromObj = JSONUtils.getJSONObject(bodyObj, "from", new JSONObject());
        this.fromUid = JSONUtils.getString(fromObj, "id", "");
        this.fromName = JSONUtils.getString(fromObj, "name", "");
        JSONObject contextObj = JSONUtils.getJSONObject(bodyObj, "context", new JSONObject());
        this.action = JSONUtils.getString(contextObj, "action", "");
        this.params = JSONUtils.getString(contextObj, "params", "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTracer() {
        return tracer;
    }

    public void setTracer(String tracer) {
        this.tracer = tracer;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
