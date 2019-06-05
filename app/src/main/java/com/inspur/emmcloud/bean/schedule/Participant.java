package com.inspur.emmcloud.bean.schedule;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/4/16.
 */

public class Participant {
    public static final String TYPE_COMMON = "common";
    public static final String TYPE_RECORDER = "recorder";
    public static final String TYPE_CONTACT = "contact";
    private String id;
    private String name;//name可能有也可能没有会议详情那里显示的时候用的id从通讯录中取的名字
    private String role;//common,普通参与者,recorder, 记录人,contact,联系人

    public Participant() {

    }

    public Participant(JSONObject obj) {
        this.id = JSONUtils.getString(obj, "id", "");
        this.name = JSONUtils.getString(obj, "name", "");
        this.role = JSONUtils.getString(obj, "role", "");
    }

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

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("name", name);
            obj.put("role", role);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
