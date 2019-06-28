package com.inspur.emmcloud.web.plugin.window;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/8/2.
 */

public class DropItemTitle {
    private String ico = "";
    private String text = "";
    private String action = "";
    private boolean selected = false;

    public DropItemTitle(JSONObject object) {
        ico = JSONUtils.getString(object, "ico", "");
        text = JSONUtils.getString(object, "text", "");
        action = JSONUtils.getString(object, "action", "");
        selected = JSONUtils.getBoolean(object, "selected", false);
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
