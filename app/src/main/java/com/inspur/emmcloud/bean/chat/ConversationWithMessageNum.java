package com.inspur.emmcloud.bean.chat;

import java.io.Serializable;

/**
 * Created by libaochao on 2019/8/23.
 */

public class ConversationWithMessageNum implements Serializable {

    private Conversation conversation = new Conversation();
    private int messageNum = 0;

    public ConversationWithMessageNum(Conversation conversation, int messageNum) {
        this.conversation = conversation;
        this.messageNum = messageNum;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public int getMessageNum() {
        return messageNum;
    }


}
