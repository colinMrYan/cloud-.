package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by yufuchang on 2018/7/26.
 */

public class MainTabMenu implements Serializable {
    private String ico;
    private String action;
    private String text;

    public MainTabMenu(JSONObject jsonObject) {
        this.ico = JSONUtils.getString(jsonObject, "ico", "");
        this.action = JSONUtils.getString(jsonObject, "action", "");
        if (jsonObject.has("callback")) {
            this.action = JSONUtils.getString(jsonObject, "callback", "");
        }
        this.text = JSONUtils.getString(jsonObject, "text", "");
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
