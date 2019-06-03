package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yufuchang on 2018/7/31.
 */

public class MainTabPayLoad {
    private String version;
    private ArrayList<MainTabResult> mainTabResultList = new ArrayList<>();
    //以下字段暂时没有用上
    private String selected;
    private long lastUpdate;
    private long creationDate;
    private String state;
    private String name;

    public MainTabPayLoad(){}

    public MainTabPayLoad(JSONObject jsonObject) {
        this.version = JSONUtils.getString(jsonObject, "version", "");
        JSONArray jsonArray = JSONUtils.getJSONArray(jsonObject, "tabs", new JSONArray());
        for (int i = 0; i < jsonArray.length(); i++) {
            mainTabResultList.add(new MainTabResult(JSONUtils.getJSONObject(jsonArray, i, new JSONObject())));
        }
        this.lastUpdate = JSONUtils.getLong(jsonObject, "lastUpdate", 0);
        this.creationDate = JSONUtils.getLong(jsonObject, "creationDate", 0);
        this.selected = JSONUtils.getString(jsonObject, "selected", "");
        this.state = JSONUtils.getString(jsonObject, "state", "");
        this.name = JSONUtils.getString(jsonObject, "name", "");
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ArrayList<MainTabResult> getMainTabResultList() {
        return mainTabResultList;
    }

    public void setMainTabResultList(ArrayList<MainTabResult> mainTabResultList) {
        this.mainTabResultList = mainTabResultList;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
