package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

@Table(name = "MessageRobot", onCreated = "CREATE INDEX msgrobotchannelindex ON MessageRobot(channel)")
public class MsgRobot implements Serializable {
    private static final String TAG = "Msg";
    @Column(name = "id", isId = true)
    private String id;
    @Column(name = "message")
    private String message;
    @Column(name = "type")
    private String type;
    @Column(name = "from")
    private String from;
    @Column(name = "to")
    private String to;
    @Column(name = "channel")
    private String channel;
    @Column(name = "state")
    private String state;
    @Column(name = "content")
    private String content;
    @Column(name="time")
    private long time;

    private int sendStatus = 1;//0 发送中  1发送成功  2发送失败
    private String tmpId = "";

    public MsgRobot() {

    }

    public MsgRobot(JSONObject obj,boolean isOldMsgType){
        id = JSONUtils.getString(obj, "mid", "0");
        JSONObject bodyObj = JSONUtils.getJSONObject(obj,"body",new JSONObject());
        String source = JSONUtils.getString(bodyObj,"source","");
        JSONObject sourceObj = JSONUtils.getJSONObject(source);
        message = JSONUtils.getString(sourceObj, "message", "");
        LogUtils.jasonDebug("sourceObj="+sourceObj.toString());
        from = JSONUtils.getString(sourceObj, "from", "");
        type = JSONUtils.getString(sourceObj, "type", "");
        state = JSONUtils.getString(sourceObj, "state", "");
        content = JSONUtils.getString(sourceObj, "content", "");

        channel = JSONUtils.getString(obj, "to", "");

        String oldMsgtime = JSONUtils.getString(obj,"timestamp","");
        time = TimeUtils.UTCString2Long(oldMsgtime);

    }

    public MsgRobot(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "0");
        message = JSONUtils.getString(obj, "message", "");
        from = JSONUtils.getString(obj, "from", "");
        type = JSONUtils.getString(obj, "type", "");
        to = JSONUtils.getString(obj, "to", "");
        channel = JSONUtils.getString(obj, "channel", "");
        state = JSONUtils.getString(obj, "state", "");
        content = JSONUtils.getString(obj, "content", "");
        time = JSONUtils.getLong(obj,"time",0L);
    }

    public MsgContentExtendedActions getMsgContentExtendedActions() {
        return new MsgContentExtendedActions(content);
    }

    public MsgContentAttachmentCard getMsgContentAttachmentCard() {
        return new MsgContentAttachmentCard(content);
    }

    public MsgContentComment getMsgContentComment() {
        return new MsgContentComment(content);
    }

    public MsgContentAttachmentFile getMsgContentAttachmentFile() {
        return new MsgContentAttachmentFile(content);
    }

    public Long getTime(){
        return  time;
    }

    public MsgContentMediaImage getMsgContentMediaImage() {
        return new MsgContentMediaImage(content);
    }

    public MsgContentTextMarkdown getMsgContentTextMarkdown() {
        return new MsgContentTextMarkdown(content);
    }

    public MsgContentTextPlain getMsgContentTextPlain() {
        return new MsgContentTextPlain(content);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getTmpId() {
        return tmpId;
    }

    public String getFromUser(){
        return JSONUtils.getString(from,"user","");
    }

    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }


    /*
         * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
         */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof MsgRobot))
            return false;

        final MsgRobot otherMsg = (MsgRobot) other;
        if (!getId().equals(otherMsg.getId()))
            return false;
        return true;
    }
}

