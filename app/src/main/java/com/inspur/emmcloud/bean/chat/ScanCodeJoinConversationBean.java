package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * invitationUrl=https://api.inspuronline.com/chat/rest/v1/channel/group/188953186239774721/seat?invitation=19edb77048ed5bc551ee17235d1f4bf14860bc76a76d2959de9c1354216c547f500b88d741652823c67cc86e4e5c477631339484f79dcc6d44ea038a2dc3fb2d
 * Created by: yufuchang
 * Date: 2019/9/13
 */
public class ScanCodeJoinConversationBean {
    String conversationQrCode = "";

    public ScanCodeJoinConversationBean(String response) {
        conversationQrCode = JSONUtils.getString(response, "invitationUrl", "");
    }

    public String getConversationQrCode() {
        return conversationQrCode;
    }

    public void setConversationQrCode(String conversationQrCode) {
        this.conversationQrCode = conversationQrCode;
    }
}
