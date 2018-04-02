package com.inspur.emmcloud.bean.system;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/4/2.
 */

public  class AppTabPayloadBean {
    /**
     * version : v1.0.0
     * name : 云+tabbar
     * state : PENDING
     * creationDate : 1493186738081
     * selected : application
     * tabs : [{"id":1,"key":"application","component":"main-tab","icon":"hello-app","selected":true,"title":{"zh-Hans":"应用","zh-Hant":"應用","en-US":"App"}}]
     */

    private String version = "";
    private String name = "";
    private String state = "";
    private long creationDate = 0;
    private String selected = "";
    private List<AppTabDataBean> tabs = new ArrayList<>();

    public AppTabPayloadBean(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("version")) {
                this.version = jsonObject.getString("version");
            }
            if (jsonObject.has("name")) {
                this.name = jsonObject.getString("name");
            }
            if (jsonObject.has("state")) {
                this.state = jsonObject.getString("state");
            }
            if (jsonObject.has("selected")) {
                this.selected = jsonObject.getString("selected");
            }
            if (jsonObject.has("creationDate")) {
                this.creationDate = jsonObject.getLong("creationDate");
            }
            if (jsonObject.has("tabs")) {
                JSONArray jsonArray = jsonObject.getJSONArray("tabs");
                int arraySize = jsonArray.length();
                for (int i = 0; i < arraySize; i++) {
                    this.tabs.add(new AppTabDataBean(jsonArray.getJSONObject(i)));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public List<AppTabDataBean> getTabs() {
        return tabs;
    }

    public void setTabs(List<AppTabDataBean> tabs) {
        this.tabs = tabs;
    }


}