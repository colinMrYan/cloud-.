package com.inspur.emmcloud.bean.work;


import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

/**
 * {"creationDate":1465957172920,"lastUpdate":null,"state":"ACTIVED",
 * "id":"CAL:a00591fe581940e49cc9cf7a487142e9",
 * "name":"生活日历","color":"ORANGE","owner":66666}
 */
@Table(name = "MyCalendar")
public class MyCalendar implements Serializable {
    @Column(name = "id", isId = true)
    private String id;
    @Column(name = "name")
    private String name;
    @Column(name = "color")
    private String color;
    @Column(name = "owner")
    private String owner;
    @Column(name = "state")
    private String state;
    @Column(name = "community")
    private boolean community;

    public MyCalendar() {

    }

    public MyCalendar(JSONObject obj) {
        try {
            id = JSONUtils.getString(obj, "id", "");
            name = JSONUtils.getString(obj, "name", "");
            color = JSONUtils.getString(obj, "color", "");
            owner = JSONUtils.getString(obj, "owner", "");
            state = JSONUtils.getString(obj, "state", "");
            community = JSONUtils.getBoolean(obj, "community", false);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public MyCalendar(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            id = JSONUtils.getString(obj, "id", "");
            name = JSONUtils.getString(obj, "name", "");
            color = JSONUtils.getString(obj, "color", "");
            owner = JSONUtils.getString(obj, "owner", "");
            state = JSONUtils.getString(obj, "state", "");
            community = JSONUtils.getBoolean(obj, "community", false);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean getCommunity() {
        return community;
    }
}
