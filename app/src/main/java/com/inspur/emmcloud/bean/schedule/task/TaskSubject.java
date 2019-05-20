package com.inspur.emmcloud.bean.schedule.task;

import android.text.TextUtils;

import org.json.JSONObject;

import java.io.Serializable;

public class TaskSubject implements Serializable {

    private String creationDate;
    private String lastUpdate;
    private String state;
    private String id;
    private String title;
    private String owner;
    private String master;

    public TaskSubject() {

    }

    public TaskSubject(String response) {
        JSONObject jsonObject = null;
        try {
            if (!TextUtils.isEmpty(response) && !response.equals("null")) {
                jsonObject = new JSONObject(response);
                if (jsonObject.has("creationDate")) {
                    this.creationDate = jsonObject.getString("creationDate");
                }

                if (jsonObject.has("lastUpdate")) {
                    this.lastUpdate = jsonObject.getString("lastUpdate");
                }

                if (jsonObject.has("state")) {
                    this.state = jsonObject.getString("state");
                }

                if (jsonObject.has("id")) {
                    this.id = jsonObject.getString("id");
                }

                if (jsonObject.has("title")) {
                    this.title = jsonObject.getString("title");
                }


                if (jsonObject.has("owner")) {
                    this.owner = jsonObject.getString("owner");
                }

                if (jsonObject.has("master")) {
                    this.master = jsonObject.getString("master");
                }
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }


}
