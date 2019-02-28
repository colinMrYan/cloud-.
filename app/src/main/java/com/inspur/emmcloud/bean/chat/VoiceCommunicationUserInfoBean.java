package com.inspur.emmcloud.bean.chat;

import java.io.Serializable;

/**
 * Created by yufuchang on 2018/8/16.
 */

public class VoiceCommunicationUserInfoBean implements Serializable {
    private String userId = "";
    private String userName = "";

    public VoiceCommunicationUserInfoBean(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
