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
    public static final String TYPE_INVITE = "invite";
    public static final String TYPE_ORGANIZER = "organizer";
    public static final String CALENDAR_RESPONSE_TYPE_UNKNOWN = "unknown"; //未知
    public static final String CALENDAR_RESPONSE_TYPE_TENTATIVE = "tentative";//暂定
    public static final String CALENDAR_RESPONSE_TYPE_ACCEPT = "accept";//接受
    public static final String CALENDAR_RESPONSE_TYPE_DECLINE = "decline";//拒绝
    private String id;
    private String name;//name可能有也可能没有会议详情那里显示的时候用的id从通讯录中取的名字
    private String role;//common,普通参与者,recorder, 记录人,contact,联系人
    private String email;//邮箱
    private String responseType;//日历返回类型

    public Participant() {

    }

    public Participant(JSONObject obj) {
        this.id = JSONUtils.getString(obj, "id", "");
        this.name = JSONUtils.getString(obj, "name", "");
        this.role = JSONUtils.getString(obj, "role", "");
        this.email = JSONUtils.getString(obj, "email", "");
        this.responseType = JSONUtils.getString(obj, "responseType", CALENDAR_RESPONSE_TYPE_UNKNOWN);
    }

    public static String getTypeCommon() {
        return TYPE_COMMON;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
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
