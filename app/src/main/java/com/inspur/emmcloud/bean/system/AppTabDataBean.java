package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.util.common.StringUtils;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/4/2.
 */

public class AppTabDataBean {
    /**
     * id : 1
     * key : application
     * component : main-tab
     * icon : hello-app
     * selected : true
     * title : {"zh-Hans":"应用","zh-Hant":"應用","en-US":"App"}
     * "properties": {
     * "canContact": "false",
     * "canCreate": "true"
     * }
     */

    private int id;
    private String key = "";
    private String component = "";
    private String icon = "";
    private boolean selected = false;
    private AppTabTitleBean title;
    private AppTabProperty property;

    public AppTabDataBean(JSONObject jsonObject) {
        try {
            if (jsonObject == null) {
                return;
            }
            if (jsonObject.has("id")) {
                this.id = jsonObject.getInt("id");
            }
            if (jsonObject.has("key")) {
                this.key = jsonObject.getString("key");
            }
            if (jsonObject.has("component")) {
                this.component = jsonObject.getString("component");
            }
            if (jsonObject.has("icon")) {
                this.icon = jsonObject.getString("icon");
            }
            if (jsonObject.has("selected")) {
                this.selected = jsonObject.getBoolean("selected");
            }
            if (jsonObject.has("title")) {
                this.title = new AppTabTitleBean(jsonObject.getString("title"));
            }
            if (jsonObject.has("properties")) {
                String response = jsonObject.getString("properties");
                if (StringUtils.isBlank(response)) {
                    response = " ";
                }
                this.property = new AppTabProperty(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public AppTabProperty getProperty() {
        return property;
    }

    public void setProperty(AppTabProperty property) {
        this.property = property;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTabId() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public AppTabTitleBean getTitle() {
        return title;
    }

    public void setTitle(AppTabTitleBean title) {
        this.title = title;
    }




}