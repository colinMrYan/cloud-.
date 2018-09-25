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

    public UIConversation(String id) {
        this.id = id;
    }

    public UIConversation(Conversation conversation) {
        this.conversation = conversation;
        this.id = conversation.getId();
        if (conversation.getType().equals())
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(long unReadCount) {
        this.unReadCount = unReadCount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Conversation))
            return false;

        final UIConversation uiConversation = (UIConversation) other;
        return getId().equals(uiConversation.getId());
    }
}
