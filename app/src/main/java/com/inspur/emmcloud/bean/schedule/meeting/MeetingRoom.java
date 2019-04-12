package com.inspur.emmcloud.bean.schedule.meeting;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class MeetingRoom implements Serializable{

    private String id;
    private String name;
    private int galleryful;
    private String admin;
    private String light;
    private String shortName = "";
    private String maxAhead = "";
    private String maxDuration = "";
    private ArrayList<String> equipmentList = new ArrayList<>();
    private ArrayList<Integer> busyDegreeList = new ArrayList<>();

    public MeetingRoom(){
    }
    public MeetingRoom(JSONObject obj) {
        id = JSONUtils.getString(obj,"id","");
        name = JSONUtils.getString(obj,"name","");
        admin = JSONUtils.getString(obj,"admin","");
        galleryful = JSONUtils.getInt(obj,"galleryful",-1);
        maxAhead = JSONUtils.getString(obj,"maxAhead","");
        maxDuration = JSONUtils.getString(obj,"maxDuration","");
        light = JSONUtils.getString(obj,"light","");
        equipmentList = JSONUtils.getStringList(obj,"equipments",new ArrayList<String>());
        JSONArray array = JSONUtils.getJSONArray(obj,"busyDegree",new JSONArray());
        for (int i=0;i<array.length();i++){
            try {
                busyDegreeList.add(array.getInt(i));
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        shortName = JSONUtils.getString(obj,"shortname","");
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

    public int getGalleryful() {
        return galleryful;
    }

    public void setGalleryful(int galleryful) {
        this.galleryful = galleryful;
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

    public String getMaxAhead() {
        return maxAhead;
    }

    public void setMaxAhead(String maxAhead) {
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
}
