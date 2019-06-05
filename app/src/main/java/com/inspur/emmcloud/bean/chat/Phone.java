package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/2/27.
 */

public class Phone {
    private String category;
    private String number;

    public Phone(JSONObject obj) {
        category = JSONUtils.getString(obj, "category", "");
        number = JSONUtils.getString(obj, "number", "");
    }

    public Phone(String category, String number) {
        this.category = category;
        this.number = number;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("category", category);
            obj.put("number", number);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
