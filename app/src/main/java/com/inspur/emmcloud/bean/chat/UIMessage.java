package com.inspur.emmcloud.bean.chat;

import java.io.Serializable;

public class UIMessage implements Serializable {
    private Message message;

    public UIMessage() {

    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof UIMessage))
            return false;

        final UIMessage otherUIMsg = (UIMessage) other;
        if (!getMessage().getId().equals(otherUIMsg.getMessage().getId()))
            return false;
        return true;
    }


}

