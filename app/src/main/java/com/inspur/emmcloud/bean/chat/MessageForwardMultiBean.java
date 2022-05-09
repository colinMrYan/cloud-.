package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.componentservice.communication.SearchModel;

import java.io.Serializable;

/**
 * Date：2022/5/7
 * Author：wang zhen
 * Description
 */
public class MessageForwardMultiBean implements Serializable {
    private String conversationId = ""; // 会话ID
    private String name = "";
    private String type = ""; // 单人：user 组织：struct 群组：channelGroup
    private String icon = "";
    private String contactId = ""; // 联系人ID

    public MessageForwardMultiBean(String conversationId, String name, String type, String icon, String contactId) {
        this.conversationId = conversationId;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.contactId = contactId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public boolean equals(Object other) {
        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof MessageForwardMultiBean))
            return false;
        final MessageForwardMultiBean bean = (MessageForwardMultiBean) other;
        // 先判断是否是联系人，联系人比较contactId，单聊/群聊则比较conversationId
        if (getType().equals("USER")) {
            return bean.getContactId().equals(getContactId());
        } else {
            return bean.getConversationId().equals(getConversationId());
        }
    }
}
