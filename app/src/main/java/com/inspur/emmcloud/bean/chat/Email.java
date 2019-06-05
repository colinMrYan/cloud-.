package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/2/27.
 */

public class Email {
    private String category;
    private String address;

    public Email(String category, String address) {
        this.category = category;
        this.address = address;
    }

    public Email(JSONObject obj) {
        category = JSONUtils.getString(obj, "category", "");
        address = JSONUtils.getString(obj, "address", "");
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("category", category);
            obj.put("address", address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
