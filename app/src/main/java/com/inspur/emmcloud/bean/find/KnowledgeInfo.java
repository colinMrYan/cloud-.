package com.inspur.emmcloud.bean.find;

import org.json.JSONObject;


public class KnowledgeInfo {
    private String name = "";
    private String uri = "";
    private String id = "";
    private String icon = "";

    public KnowledgeInfo(JSONObject info) {
        try {
            if (info.has("id")) {
                this.id = info.getString("id");
            }
            if (info.has("title")) {
                this.name = info.getString("title");
            }
            if (info.has("url")) {
                this.uri = info.getString("url");
            }
            if (info.has("icon")) {
                this.icon = info.getString("icon");
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
