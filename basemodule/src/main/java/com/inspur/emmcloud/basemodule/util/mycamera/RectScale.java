package com.inspur.emmcloud.basemodule.util.mycamera;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2017/12/15.
 */

public class RectScale {
    private String name = "";
    private String rectScale = "";

    public RectScale(JSONObject obj) {
        name = JSONUtils.getString(obj, "name", "");
        rectScale = JSONUtils.getString(obj, "rectScale", "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRectScale() {
        return rectScale;
    }

    public void setRectScale(String rectScale) {
        this.rectScale = rectScale;
    }
}
