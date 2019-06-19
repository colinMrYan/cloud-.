package com.inspur.emmcloud.news.bean;

import org.json.JSONObject;

public class NewsTitle {

    private static final String TAG = "GetNewsTitleResult";
    private String ncid = "";
    private String title = "";
    private String parent = "";
    private String creationDate = "";
    private String lastUpdate = "";
    private String state = "";
    private String description = "";
    private String type = "";
    private boolean hasExtraPermission = false;

    public NewsTitle(JSONObject jsonObject) {
        try {

            if (jsonObject.has("id")) {
                this.ncid = jsonObject.getString("id");
            }
            if (jsonObject.has("title")) {
                this.title = jsonObject.getString("title");
            }
            if (jsonObject.has("parent")) {
                this.parent = jsonObject.getString("parent");
            }
            if (jsonObject.has("creationDate")) {
                this.creationDate = jsonObject.getString("creationDate");
            }
            if (jsonObject.has("lastUpdate")) {
                this.lastUpdate = jsonObject.getString("lastUpdate");
            }
            if (jsonObject.has("state")) {
                this.state = jsonObject.getString("state");
            }
            if (jsonObject.has("description")) {
                this.description = jsonObject.getString("description");
            }
            if (jsonObject.has("type")) {
                this.type = jsonObject.getString("type");
            }
            if (jsonObject.has("hasExtraPermission")) {
                this.hasExtraPermission = jsonObject.getBoolean("hasExtraPermission");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getNcid() {
        return ncid;
    }

    public void setNcid(String ncid) {
        this.ncid = ncid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isHasExtraPermission() {
        return hasExtraPermission;
    }

    public void setHasExtraPermission(boolean hasExtraPermission) {
        this.hasExtraPermission = hasExtraPermission;
    }
}
