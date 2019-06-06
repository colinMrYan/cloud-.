package com.inspur.emmcloud.basemodule.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by chenmch on 2018/3/13.
 */

public class GetUploadPushInfoResult {
    private String chatClientId;

    public GetUploadPushInfoResult(String response) {
        chatClientId = JSONUtils.getString(response, "id", "");
    }

    public String getChatClientId() {
        return chatClientId;
    }

    public void setChatClientId(String chatClientId) {
        this.chatClientId = chatClientId;
    }
}
