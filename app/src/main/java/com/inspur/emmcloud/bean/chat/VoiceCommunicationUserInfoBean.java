package com.inspur.emmcloud.bean.chat;

/**
 * Created by yufuchang on 2018/8/16.
 */

public class VoiceCommunicationUserInfoBean {
    private String userId = "";
    private String deviceId = "";
    public VoiceCommunicationUserInfoBean(String userId,String deviceId){
        this.userId = userId;
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
