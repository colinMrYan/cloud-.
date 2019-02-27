package com.inspur.emmcloud.bean.chat;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "MessageReadCreationDate")
public class MessageReadCreationDate {
    @Column(name = "cid", isId = true)
    private String cid = "";
    @Column(name = "messageReadCreationDate")
    private long messageReadCreationDate = 0L; //已读最新消息的创建时间

    public MessageReadCreationDate() {

    }

    public MessageReadCreationDate(String cid, long messageReadCreationDate) {
        this.cid = cid;
        this.messageReadCreationDate = messageReadCreationDate;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public long getMessageReadCreationDate() {
        return messageReadCreationDate;
    }

    public void setMessageReadCreationDate(long messageReadCreationDate) {
        this.messageReadCreationDate = messageReadCreationDate;
    }
}
