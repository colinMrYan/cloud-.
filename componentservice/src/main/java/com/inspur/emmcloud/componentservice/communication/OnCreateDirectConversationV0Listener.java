package com.inspur.emmcloud.componentservice.communication;

public interface OnCreateDirectConversationV0Listener {
    void createDirectChatSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult);

    void createDirectChatFail();
}
