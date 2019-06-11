package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/3/13.
 */

public class Action {
    private String title;
    private String type;
    private String url;

    public Action(JSONObject obj) {
        title = JSONUtils.getString(obj, "title", "");
        type = JSONUtils.getString(obj, "type", "");
        JSONObject paramsObj = JSONUtils.getJSONObject(obj, "params", new JSONObject());
        url = JSONUtils.getString(paramsObj, "url", "");
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
}
