package com.inspur.emmcloud.bean.schedule;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by libaochao on 2019/4/15.
 */

public class Participant implements Serializable {
    private String id = "";
    private String name = "";
    private String role = "common";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Participant() {
    }

    public Participant(JSONObject jsonObject) {
        this.id = JSONUtils.getString(jsonObject, "id", "");
        this.name = JSONUtils.getString(jsonObject, "name", "");
        this.role = JSONUtils.getString(jsonObject, "role", "common");
    }

    public Participant(String id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }
}

