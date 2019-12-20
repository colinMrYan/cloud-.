package com.inspur.emmcloud.componentservice.communication;

/**
 * Created by libaochao on 2019/12/18.
 */

public interface OnCreateGroupConversationListener {
    void createGroupConversationSuccess(Conversation conversation);

    void createGroupConversationFail();
}
