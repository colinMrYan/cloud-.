package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by: yufuchang
 * Date: 2019/9/24
 */
public class GetVoiceAndVideoResult {

    private String id;
    private String channel;
    private String fromUid;
    private String fromName;
    private String contextAction;
    private String contextParamsRoom;
    private String contextParamsSchema;
    private String contextParamsType;

    public GetVoiceAndVideoResult(String body) {
        this.id = JSONUtils.getString(body, "id", "");
        this.channel = JSONUtils.getString(body, "channel", "");
        JSONObject fromObj = JSONUtils.getJSONObject(body, "from", new JSONObject());
        this.fromUid = JSONUtils.getString(fromObj, "id", "");
        this.fromName = JSONUtils.getString(fromObj, "name", "");
        JSONObject contextObj = JSONUtils.getJSONObject(body, "context", new JSONObject());
        this.contextAction = JSONUtils.getString(contextObj, "action", "");
        JSONObject jsonParam = JSONUtils.getJSONObject(contextObj, "params", new JSONObject());
        this.contextParamsRoom = JSONUtils.getString(jsonParam, "room", "");
        this.contextParamsSchema = JSONUtils.getString(jsonParam, "schema", "");
        this.contextParamsType = JSONUtils.getString(jsonParam, "type", "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getContextAction() {
        return contextAction;
    }

    public void setContextAction(String contextAction) {
        this.contextAction = contextAction;
    }

    public String getContextParamsRoom() {
        return contextParamsRoom;
    }

    public void setContextParamsRoom(String contextParamsRoom) {
        this.contextParamsRoom = contextParamsRoom;
    }

    public String getContextParamsSchema() {
        return contextParamsSchema;
    }

    public void setContextParamsSchema(String contextParamsSchema) {
        this.contextParamsSchema = contextParamsSchema;
    }

    public String getContextParamsType() {
        return contextParamsType;
    }

    public void setContextParamsType(String contextParamsType) {
        this.contextParamsType = contextParamsType;
    }
}
