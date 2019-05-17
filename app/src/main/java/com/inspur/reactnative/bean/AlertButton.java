package com.inspur.reactnative.bean;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/3/9.
 */

public class AlertButton {
    private String text;
    private String code;

    public AlertButton(JSONObject obj) {
        text = JSONUtils.getString(obj, "text", "");
        code = JSONUtils.getString(obj, "code", "");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
