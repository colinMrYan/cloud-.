package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by yufuchang on 2018/8/16.
 */

public class VoiceCommunicationJoinChannelInfoBean implements Serializable {
    private String userId;
    private String headImageUrl;
    private int agoraUid;
    private String token;
    private int connectState;
    private String userName;
    private int volume = -1;
    private int userState = -1;

    public VoiceCommunicationJoinChannelInfoBean() {
    }

    public VoiceCommunicationJoinChannelInfoBean(JSONObject jsonObject) {
        this(jsonObject.toString());
    }

    public VoiceCommunicationJoinChannelInfoBean(String info) {
        this.userId = JSONUtils.getString(info, "id", "");
        this.headImageUrl = JSONUtils.getString(info, "headImgUrl", "");
        this.token = JSONUtils.getString(info, "token", "");
        this.agoraUid = JSONUtils.getInt(info, "agoraUid", 0);
        this.connectState = JSONUtils.getInt(info, "connectState", -1);
        this.userName = JSONUtils.getString(info, "name", "");
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHeadImageUrl() {
        return headImageUrl;
    }

    public void setHeadImageUrl(String headImageUrl) {
        this.headImageUrl = headImageUrl;
    }

    public int getAgoraUid() {
        return agoraUid;
    }

    public void setAgoraUid(int agoraUid) {
        this.agoraUid = agoraUid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getConnectState() {
        return connectState;
    }

    public void setConnectState(int connectState) {
        this.connectState = connectState;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getUserState() {
        return userState;
    }

    public void setUserState(int userState) {
        this.userState = userState;
    }
}
