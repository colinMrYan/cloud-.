package com.inspur.emmcloud.bean.chat;

import org.json.JSONException;
import org.json.JSONObject;

public class CommentBodyBean {
    private String type = "";
    private String key = "";
    private String name = "";
    private String size = "0";

    public CommentBodyBean(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("type")) {
                this.type = jsonObject.getString("type");
            }
            if (jsonObject.has("key")) {
                this.key = jsonObject.getString("key");
            }
            if (jsonObject.has("name")) {
                this.name = jsonObject.getString("name");
            }
            if (jsonObject.has("size")) {
                this.size = jsonObject.getString("size");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

}
