package com.inspur.emmcloud.bean.chat;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "MessageMatheSet")
public class MessageMatheSet {
    @Column(name = "channelId", isId = true)
    private String channelId = "";
    @Column(name = "matheSetStr")
    private String matheSetStr = "";

    public MessageMatheSet() {

    }

    public MessageMatheSet(String channelId, String matheSetStr) {
        this.channelId = channelId;
        this.matheSetStr = matheSetStr;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getMatheSetStr() {
        return matheSetStr;
    }

    public void setMathSetStr(String matheSetStr) {
        this.matheSetStr = matheSetStr;
    }
}
