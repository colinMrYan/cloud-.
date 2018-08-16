package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;

/**
 * Created by yufuchang on 2018/8/16.
 */

public class VoiceCommunicationJoinChannelInfoBean {
    private String userId;
    private String headImageUrl;
    private int agoraUid;
    private String token;
    private int connectState;
    private String userName;
    public VoiceCommunicationJoinChannelInfoBean(String info){
        this.userId = JSONUtils.getString(info,"","");
        this.headImageUrl = JSONUtils.getString(info,"","");
        this.token = JSONUtils.getString(info,"","");
        this.agoraUid = JSONUtils.getInt(info,"",0);
        this.connectState = JSONUtils.getInt(info,"",0);
        this.userName = JSONUtils.getString(info,"username","");
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
}
