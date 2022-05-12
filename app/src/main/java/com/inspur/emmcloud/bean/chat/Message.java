package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.richtext.markdown.MarkDown;

import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

@Table(name = "Message", onCreated = "CREATE INDEX messageindex ON Message(channel)")
public class Message implements Serializable {
    public static final String MESSAGE_TYPE_FILE_REGULAR_FILE = "file/regular-file";
    public static final String MESSAGE_TYPE_MEDIA_IMAGE = "media/image";
    public static final String MESSAGE_TYPE_MEDIA_VOICE = "media/voice";
    public static final String MESSAGE_TYPE_TEXT_PLAIN = "text/plain";
    public static final String MESSAGE_TYPE_TEXT_WHISPER = "text/whisper";
    public static final String MESSAGE_TYPE_TEXT_BURN = "text/burn";
    public static final String MESSAGE_TYPE_TEXT_MARKDOWN = "text/markdown";
    public static final String MESSAGE_TYPE_EXTENDED_CONTACT_CARD = "extended/contact-card";
    public static final String MESSAGE_TYPE_EXTENDED_ACTIONS = "extended/actions";
//    public static final String MESSAGE_TYPE_EXTENDED_SELECTED = "extended/selects";
    public static final String MESSAGE_TYPE_EXTENDED_SELECTED = "experimental/selects";
    public static final String MESSAGE_TYPE_COMMENT_TEXT_PLAIN = "comment/text-plain";
    public static final String MESSAGE_TYPE_EXTENDED_LINKS = "extended/links";
    public static final String MESSAGE_TYPE_ATTACHMENT_CARD = "attachment/card";
    public static final int MESSAGE_SEND_ING = 0;
    public static final int MESSAGE_SEND_SUCCESS = 1;
    public static final int MESSAGE_SEND_FAIL = 2;
    public static final int MESSAGE_SEND_EDIT = 3;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_UNREAD = 0;
    public static final int MESSAGE_LIFE_PACK = 0;//消息未拆包
    public static final int MESSAGE_LIFE_UNPACK = 1;//消息已拆包
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
    @Column(name = "read")
    private int read = 0;  //0 未读，1 已读
    @Column(name = "sendStatus")
    private int sendStatus = 1;//0 发送中  1发送成功  2发送失败 3 编辑中   字段扩展
    @Column(name = "localPath")
    private String localPath = "";
    @Column(name = "showContent")
    private String showContent = "";
    //是否处于重复等待重发状态中
    @Column(name = "isWaitingSendRetry")
    private boolean isWaitingSendRetry = false;
    @Column(name = "recallFrom")
    private String recallFrom = "";
    @Column(name = "lifeCycleState")
    private int lifeCycleState = 0;//0未拆包，1已经拆包
    @Column(name = "states")
    private String states = "";//已读未读状态json
    private String tmpId = "";

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
        tmpId = JSONUtils.getString(content, "tmpId", "");
        channel = msg.getCid();
        creationDate = msg.getTime();
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
        states = JSONUtils.getString(obj, "states", "");
        tmpId = JSONUtils.getString(content, "tmpId", "");
        String UTCTime = JSONUtils.getString(obj, "creationDate", "");
        creationDate = TimeUtils.UTCString2Long(UTCTime);
        boolean readState = JSONUtils.getBoolean(obj, "read", false);
        if (!readState && getFromUser().equals(MyApplication.getInstance().getUid())) {
            readState = true;
        }
        read = readState ? 1 : 0;
        setMessageShowContent();
    }

    public static boolean isMessage(Msg msg) {
        return msg.getBody().contains("\\\"message\\\":\\\"1.0\\\"");
    }

    public void setMessageShowContent() {
        switch (type) {
            case MESSAGE_TYPE_TEXT_PLAIN:
                MsgContentTextPlain msgContentTextPlain = getMsgContentTextPlain();
                showContent = ChatMsgContentUtils.mentionsAndUrl2Span(msgContentTextPlain.getText(), msgContentTextPlain.getMentionsMap()).toString();
                break;
            case MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                MsgContentComment msgContentComment = getMsgContentComment();
                showContent = ChatMsgContentUtils.mentionsAndUrl2Span(msgContentComment.getText(), msgContentComment.getMentionsMap()).toString();
                break;
            case MESSAGE_TYPE_TEXT_MARKDOWN:
                MsgContentTextMarkdown msgContentTextMarkdown = getMsgContentTextMarkdown();
                showContent = MarkDown.fromMarkdown(msgContentTextMarkdown.getText());
                break;
        }
    }

    public MsgContentExtendedActions getMsgContentExtendedActions() {
        return new MsgContentExtendedActions(content);
    }

    public MsgContentExtendedDecide getMsgContentExtendedDecide(){
        return new MsgContentExtendedDecide(content);
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

    public MsgContentExtendedLinks getMsgContentExtendedLinks() {
        return new MsgContentExtendedLinks(content);
    }

    public MsgContentTextMarkdown getMsgContentTextMarkdown() {
        return new MsgContentTextMarkdown(content);
    }

    public MsgContentMediaVoice getMsgContentMediaVoice() {
        return new MsgContentMediaVoice(content);
    }

    public String getShowContent() {
        return showContent;
    }

    public void setShowContent(String showContent) {
        this.showContent = showContent;
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

    public String getStates() {
        return states;
    }

    public void setStates(String states) {
        this.states = states;
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

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public boolean isWaitingSendRetry() {
        return isWaitingSendRetry;
    }

    public void setWaitingSendRetry(boolean waitingSendRetry) {
        isWaitingSendRetry = waitingSendRetry;
    }

    public String getRecallFrom() {
        return recallFrom;
    }

    public void setRecallFrom(String recallFrom) {
        this.recallFrom = recallFrom;
    }

    public void setRecallFromSelf() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", BaseApplication.getInstance().getUid());
            String name = PreferencesUtils.getString(BaseApplication.getInstance(), "userRealName", "");
            obj.put("name", name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.recallFrom = obj.toString();
    }

    public String getRecallFromUid() {
        if (!StringUtils.isBlank(recallFrom)) {
            return JSONUtils.getString(recallFrom, "id", "");
        }
        return "";
    }

    public String getRecallFromUserName() {
        if (!StringUtils.isBlank(recallFrom)) {
            return JSONUtils.getString(recallFrom, "name", "");
        }
        return "";
    }

    public int getLifeCycleState() {
        return lifeCycleState;
    }

    public void setLifeCycleState(int lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }

    public String getTmpId() {
        return tmpId;
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

