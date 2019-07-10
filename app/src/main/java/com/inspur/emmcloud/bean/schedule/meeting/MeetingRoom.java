package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class MeetingRoom implements Serializable {

    private String id;
    private String name;
    private int capacity;
    private String admin;
    private String light;
    private String shortName = "";
    private int maxAhead = 0;
    private String maxDuration = "";
    private ArrayList<String> equipmentList = new ArrayList<>();
    private ArrayList<Integer> busyDegreeList = new ArrayList<>();
    private Building building;
    private long creationTime = 0;
    private long lastTime = 0;
    private int state = 0;

    public MeetingRoom() {
    }

    public MeetingRoom(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", ""); //
        name = JSONUtils.getString(obj, "name", ""); //
        admin = JSONUtils.getString(obj, "admin", ""); //
        capacity = JSONUtils.getInt(obj, "capacity", -1);
        maxAhead = JSONUtils.getInt(obj, "maxAhead", 0);
        maxDuration = JSONUtils.getString(obj, "maxDuration", "");
        light = JSONUtils.getString(obj, "light", "");
        equipmentList = JSONUtils.getStringList(obj, "equipments", new ArrayList<String>());
        JSONArray array = JSONUtils.getJSONArray(obj, "busyDegree", new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            try {
                busyDegreeList.add(array.getInt(i));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        shortName = JSONUtils.getString(obj, "shortName", "");
        creationTime = JSONUtils.getLong(obj, "creationTime", 0);
        lastTime = JSONUtils.getLong(obj, "lastTime", 0);
        state = JSONUtils.getInt(obj, "state", 0);
        JSONObject buildingObj = JSONUtils.getJSONObject(obj, "building", new JSONObject());
        building = new Building(buildingObj);
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

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getMaxAhead() {
        return maxAhead;
    }

    public void setMaxAhead(int maxAhead) {
        this.maxAhead = maxAhead;
    }

    public String getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(String maxDuration) {
        this.maxDuration = maxDuration;
    }

    public ArrayList<String> getEquipmentList() {
        return equipmentList;
    }

    public void setEquipmentList(ArrayList<String> equipmentList) {
        this.equipmentList = equipmentList;
    }

    public ArrayList<Integer> getBusyDegreeList() {
        return busyDegreeList;
    }

    public void setBusyDegreeList(ArrayList<Integer> busyDegreeList) {
        this.busyDegreeList = busyDegreeList;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
