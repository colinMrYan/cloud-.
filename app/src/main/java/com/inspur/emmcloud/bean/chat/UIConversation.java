package com.inspur.emmcloud.bean.chat;

import java.util.List;

/**
 * Created by chenmch on 2018/9/22.
 */

public class UIConversation {
    private String id;
    private Conversation conversation;
    private List<Message> messageList;
    private String title;
    private long lastUpdate;
    private long unReadCount = 0;
    private String content;

    public UIConversation() {
    }

    public UIConversation(Conversation conversation) {
        this.conversation = conversation;
        this.id = conversation.getId();

    }
}
