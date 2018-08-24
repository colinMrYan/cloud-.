package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

@Table(name = "Message",onCreated = "CREATE INDEX messageindex ON Message(channel)")
public class Message implements Serializable {
    public static final String MESSAGE_TYPE_FILE_REGULAR_FILE = "file/regular-file";
    public static final String MESSAGE_TYPE_MEDIA_IMAGE = "media/image";
    public static final String MESSAGE_TYPE_MEDIA_VOICE = "media/voice";
    public static final String MESSAGE_TYPE_TEXT_PLAIN = "text/plain";
    public static final String MESSAGE_TYPE_TEXT_MARKDOWN = "text/markdown";
    public static final String MESSAGE_TYPE_ATTACHMENT_CARD = "attachment/card";
    public static final String MESSAGE_TYPE_EXTENDED_ACTIONS = "extended/actions";
    public static final String MESSAGE_TYPE_COMMENT_TEXT_PLAIN= "comment/text-plain";
    public static final String MESSAGE_TYPE_EXTENDED_LINKS = "extended/links";
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
    @Column(name = "creationDate")
    private Long creationDate;

    private int sendStatus = 1;//0 发送中  1发送成功  2发送失败

    public Message() {

    }

    public Message(Msg msg) {
        id = msg.getMid();
        JSONObject extraObj = JSONUtils.getJSONObject(msg.getBody(), "extras", new JSONObject());
        JSONObject propsObj = JSONUtils.getJSONObject(extraObj, "props", new JSONObject());
        JSONObject dataObj = JSONUtils.getJSONObject(JSONUtils.getString(propsObj, "data", ""));
        message = JSONUtils.getString(dataObj, "message", "");
        from = JSONUtils.getString(dataObj, "from", "");
        type = JSONUtils.getString(dataObj, "type", "");
        state = JSONUtils.getString(dataObj, "state", "");
        content = JSONUtils.getString(dataObj, "content", "");
        channel = msg.getCid();
        creationDate = msg.getTime();
    }

    public static boolean isMessage(Msg msg) {
        return msg.getBody().contains("\\\"message\\\":\\\"1.0\\\"");
    }

    public Message(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "0");
        message = JSONUtils.getString(obj, "message", "");
        from = JSONUtils.getString(obj, "from", "");
        type = JSONUtils.getString(obj, "type", "");
        to = JSONUtils.getString(obj, "to", "");
        channel = JSONUtils.getString(obj, "channel", "");
        state = JSONUtils.getString(obj, "state", "");
        content = JSONUtils.getString(obj, "content", "");
        String UTCTime = JSONUtils.getString(obj, "creationDate", "");
        creationDate = TimeUtils.UTCString2Long(UTCTime);
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

    public MsgContentRegularFile getMsgContentAttachmentFile() {
        return new MsgContentRegularFile(content);
    }


    public MsgContentMediaImage getMsgContentMediaImage() {
        return new MsgContentMediaImage(content);
    }

    public MsgContentExtendedLinks getMsgContentExtendedLinks(){
        return  new MsgContentExtendedLinks(content);
    }

    public MsgContentTextMarkdown getMsgContentTextMarkdown() {
        return new MsgContentTextMarkdown(content);
    }

    public MsgContentMediaVoice getMsgContentMediaVoice(){
        return new MsgContentMediaVoice(content);
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

    public String getFromUser() {
        return JSONUtils.getString(from, "user", "");
    }


    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    /*
             * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
             */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Message))
            return false;

        final Message otherMsg = (Message) other;
        return getId().equals(otherMsg.getId());
    }

    public String Message2MsgBody() {
        JSONObject bodyObj = new JSONObject();
        try {
            JSONObject propsObj = new JSONObject();
            JSONObject extrasObj = new JSONObject();
            JSONObject MessageObj = new JSONObject();
            MessageObj.put("id", id);
            MessageObj.put("message", "1.0");
            MessageObj.put("type", type);
            MessageObj.put("from", from);
            MessageObj.put("content", content);
            propsObj.put("data", MessageObj.toString());
            extrasObj.put("props", propsObj);
            bodyObj.put("extras", extrasObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bodyObj.toString();
    }
}

