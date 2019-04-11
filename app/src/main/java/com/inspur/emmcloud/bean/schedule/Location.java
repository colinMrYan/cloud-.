package com.inspur.emmcloud.bean.schedule;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/4/6.
 */

public class Location {
    private String id;
    private String displayName;
    public Location(JSONObject obj){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
