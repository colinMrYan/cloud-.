package com.inspur.emmcloud.componentservice.communication;
import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

public class ServiceChannelInfo {
    private String id;
    private String enterprise;
    private String name;
    private String description;
    private String creator;
    private String creatorName;
    private boolean forceSubscribe;
    private String cast;
    private String group;
    private String fiber;
    private String user;
    private boolean subscribe;

    public ServiceChannelInfo(String id) {
        this.id = id;
    }

    public ServiceChannelInfo(JSONObject obj) {
        this.id = JSONUtils.getString(obj, "id", "");
        this.enterprise = JSONUtils.getString(obj, "enterprise", "");
        this.name = JSONUtils.getString(obj, "name", "");
        this.description = JSONUtils.getString(obj, "description", "");
        this.creator = JSONUtils.getString(obj, "creator", "");
        this.creatorName = JSONUtils.getString(obj, "creatorName", "");
        this.forceSubscribe = JSONUtils.getBoolean(obj, "forceSubscribe", false);
        this.subscribe = JSONUtils.getBoolean(obj, "currentUserSubscribed", false);
        this.cast = JSONUtils.getString(obj, "cast", "");
        this.group = JSONUtils.getString(obj, "group", "");
        this.fiber = JSONUtils.getString(obj, "fiber", "");
        this.user = JSONUtils.getString(obj, "user", "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(String enterprise) {
        this.enterprise = enterprise;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }


    public String getFiber() {
        return fiber;
    }

    public void setFiber(String fiber) {
        this.fiber = fiber;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isSubscribe() {
        return subscribe;
    }

    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }

    public boolean isForceSubscribe() {
        return forceSubscribe;
    }

    public void setForceSubscribe(boolean forceSubscribe) {
        this.forceSubscribe = forceSubscribe;
    }


}
