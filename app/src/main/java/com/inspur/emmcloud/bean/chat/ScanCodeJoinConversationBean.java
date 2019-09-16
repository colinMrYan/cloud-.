package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by: yufuchang
 * Date: 2019/9/13
 */
public class ScanCodeJoinConversationBean {
    String conversationQrCode = "";

    public ScanCodeJoinConversationBean(String response) {
        conversationQrCode = JSONUtils.getString(response, "url", "");
    }

    public String getConversationQrCode() {
        return conversationQrCode;
    }

    public void setConversationQrCode(String conversationQrCode) {
        this.conversationQrCode = conversationQrCode;
    }
}
