package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MarkDownLinkCacheUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UIMessage implements Serializable {
    private String id;
    private Message message;
    private String senderName;
    private Long creationDate;
    private String senderPhotoUrl;
    private int sendStatus = 1;//0 发送中  1发送成功  2发送失败
    private List<MarkDownLink> markDownLinkList = new ArrayList<>();
    private int voicePlayState = 0;//0 未下载，1下载中，2，下载完成，3，未播放,4，播放中，5，播放完成，6，播放停止

    public UIMessage(Message message) {
        this.message = message;
        this.id = message.getId();
        this.creationDate = message.getCreationDate();
        this.sendStatus = message.getSendStatus();
        senderName = ContactUserCacheUtils.getUserName(message.getFromUser());
        senderPhotoUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), message.getFromUser());
        if (message.getType().equals(Message.MESSAGE_TYPE_TEXT_MARKDOWN)) {
            markDownLinkList = MarkDownLinkCacheUtils.getMarkDownLinkListByMid(MyApplication.getInstance(), message.getId());
        }
    }

    public UIMessage() {
    }

    public UIMessage(String id) {
        this.id = id;
    }

    public static List<UIMessage> MessageList2UIMessageList(List<Message> messageList) {
        List<UIMessage> UIMessageList = new ArrayList<>();
        if (messageList != null && messageList.size() > 0) {
            for (Message message : messageList) {
                UIMessageList.add(new UIMessage(message));
            }
        }
        return UIMessageList;

    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public List<MarkDownLink> getMarkDownLinkList() {
        return markDownLinkList;
    }

    public void setMarkDownLinkList(List<MarkDownLink> markDownLinkList) {
        this.markDownLinkList = markDownLinkList;
    }

    public String getSenderPhotoUrl() {
        return senderPhotoUrl;
    }

    public void setSenderPhotoUrl(String senderPhotoUrl) {
        this.senderPhotoUrl = senderPhotoUrl;
    }

    public int getVoicePlayState() {
        return voicePlayState;
    }

    public void setVoicePlayState(int voicePlayState) {
        this.voicePlayState = voicePlayState;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof UIMessage))
            return false;

        final UIMessage otherUIMsg = (UIMessage) other;
        return getId().equals(otherUIMsg.getId());
    }


}

