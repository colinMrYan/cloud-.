package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;


public class Option {
    private String title;
    private String type;
    private String url;
    private String message;
    private String actionTrigger;

    public Option(JSONObject obj) {
        title = JSONUtils.getString(obj, "title", "");
        type = JSONUtils.getString(obj, "type", "");
        JSONObject paramsObj = JSONUtils.getJSONObject(obj, "params", new JSONObject());
        url = JSONUtils.getString(paramsObj, "url", "");
        message = JSONUtils.getString(paramsObj,"message","");
        actionTrigger = JSONUtils.getString(paramsObj,"actionTrigger","");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getActionTrigger() {
        return actionTrigger;
    }

    public void setActionTrigger(String actionTrigger) {
        this.actionTrigger = actionTrigger;
    }
}
