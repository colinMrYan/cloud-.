package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Date：2022/6/27
 * Author：wang zhen
 * Description 群主转让bean
 */
public class TransferGroupBean {
    private String channelId;
    private String newOwnerId;
    private String oldOwnerId;

    public TransferGroupBean(String json) {
        this.newOwnerId = JSONUtils.getString(json, "newOwnerId", "");
    }

    public String getNewOwnerId() {
        return newOwnerId;
    }

    public void setNewOwnerId(String newOwnerId) {
        this.newOwnerId = newOwnerId;
    }
}
