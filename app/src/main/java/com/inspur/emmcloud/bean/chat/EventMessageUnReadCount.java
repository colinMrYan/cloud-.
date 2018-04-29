package com.inspur.emmcloud.bean.chat;

/**
 * Created by chenmch on 2018/4/25.
 */

public class EventMessageUnReadCount {
    private int messageUnReadCount;

    public EventMessageUnReadCount(int messageUnReadCount) {
        this.messageUnReadCount = messageUnReadCount;
    }

    public int getMessageUnReadCount() {
        return messageUnReadCount;
    }

    public void setMessageUnReadCount(int messageUnReadCount) {
        this.messageUnReadCount = messageUnReadCount;
    }
}
