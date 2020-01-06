package com.inspur.emmcloud.volume.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by chenmch on 2018/1/20.
 */

public class Group implements Serializable {
    private String id;
    private String name;
    private String enterprise;
    private long creationDate;
    private long lastUpdate;
    private String volume;
    private String owner;
    private int privilege;
    private String type;
    private ArrayList<String> memberUidList = new ArrayList<>();

    public Group() {
    }

    public Group(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "");
        name = JSONUtils.getString(obj, "name", "");
        owner = JSONUtils.getString(obj, "owner", "");
        privilege = JSONUtils.getInt(obj, "privilege", 0);
        type = JSONUtils.getString(obj, "type", "");
        enterprise = JSONUtils.getString(obj, "enterprise", "");
        creationDate = JSONUtils.getLong(obj, "creationDate", 0);
        lastUpdate = JSONUtils.getLong(obj, "lastUpdate", 0);
        volume = JSONUtils.getString(obj, "volume", "");
        JSONArray array = JSONUtils.getJSONArray(obj, "members", new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            String uid = JSONUtils.getString(array, i, "");
            memberUidList.add(uid);
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getPrivilege() {
        return privilege;
    }

    public void setPrivilege(int privilege) {
        this.privilege = privilege;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(String enterprise) {
        this.enterprise = enterprise;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public ArrayList<String> getMemberUidList() {
        return memberUidList;
    }

    public void setMemberUidList(ArrayList<String> memberUidList) {
        this.memberUidList = memberUidList;
    }
}
