package com.inspur.emmcloud.componentservice.communication;

/**
 * Created by libaochao on 2019/12/18.
 */

public interface OnCreateDirectConversationListener {
    void createDirectConversationSuccess(Conversation conversation);

    void createDirectConversationFail();
}
