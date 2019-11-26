package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;

@Table(name = "Msg", onCreated = "CREATE INDEX msgindex ON Msg(cid)")
public class Msg implements Serializable {
    private static final String TAG = "Msg";
    @Column(name = "mid", isId = true)
    private String mid = "";
    @Column(name = "time")
    private Long time = 0L;
    @Column(name = "type")
    private String type = "";
    @Column(name = "body")
    private String body = "";
    @Column(name = "uid")
    private String uid = "";
    @Column(name = "title")
    private String title = "";
    @Column(name = "avatar")
    private String avatar = "";
    @Column(name = "commentContent")
    private String commentContent = "";
    @Column(name = "cid")
    private String cid = "";  //channel id
    @Column(name = "nTitle")
    private String nTitle = "";
    @Column(name = "privates")
    private String privates = "";

    private int sendStatus = 1;//0 发送中  1发送成功  2发送失败
    private String tmpId = "";

    public Msg() {

    }

    public Msg(JSONObject obj) {
        try {
            if (obj.has("to")) {
                this.cid = obj.getString("to");
            }
            if (obj.has("mid")) {
                this.mid = obj.getString("mid");
            }
            if (obj.has("timestamp")) {
                String timestamp = obj.getString("timestamp");
                this.time = TimeUtils.UTCString2Long(timestamp);
            }
            if (obj.has("type")) {
                this.type = obj.getString("type");
            }
            if (obj.has("body")) {
                this.body = obj.getString("body");
                if (type.equals("news")) {
                    try {
                        JSONObject jsonObject = new JSONObject(body);
                        if (jsonObject.has("title")) {
                            this.nTitle = jsonObject.getString("title");
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
                JSONObject bodyObj = new JSONObject(body);
                if (bodyObj.has("tmpId")) {
                    this.tmpId = bodyObj.getString("tmpId");
                }

            }
            if (obj.has("from")) {
                JSONObject jsonObject = obj.getJSONObject("from");
                if (jsonObject.has("uid")) {
                    this.uid = jsonObject.getString("uid");
                }
                if (jsonObject.has("title")) {
                    this.title = jsonObject.getString("title");
                }
                if (jsonObject.has("avatar")) {
                    this.avatar = jsonObject.getString("avatar");
                }
            }
            if (obj.has("privates")) {
                this.privates = obj.getString("privates");
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            LogUtils.exceptionDebug(TAG, e.toString());
        }
    }

    public Message msg2Message() {
        Message message = new Message(this);
        message.setCreationDate(getTime());
        MsgContentRegularFile msgContentRegularFile = new MsgContentRegularFile();
        msgContentRegularFile.setMedia(APIUri.getPreviewUrl(JSONUtils.getString(getBody(), "key", "")));
        msgContentRegularFile.setSize(JSONUtils.getLong(getBody(), "size", 0));
        msgContentRegularFile.setName(JSONUtils.getString(getBody(), "name", ""));
        message.setContent(JSONUtils.toJSONString(msgContentRegularFile));
        return message;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

//    public String getCommentContent() {
//        return commentContent;
//    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getnTitle() {
        return nTitle;
    }

    public void setnTitle(String nTitle) {
        this.nTitle = nTitle;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }


    public String getTmpId() {
        return tmpId;
    }

    public String getCommentMid() {
        JSONObject bodyJsonObject;
        String commentMid = null;
        try {
            bodyJsonObject = new JSONObject(body);
            if (bodyJsonObject.has("mid")) {
                commentMid = bodyJsonObject.getString("mid");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return commentMid;

    }

    public String getPrivates() {
        return privates;
    }

    public void setPrivates(String privates) {
        this.privates = privates;
    }

    public String getImgTypeMsgImg() {
        String imgUrl = "";
        try {
            JSONObject bodyObj = new JSONObject(body);
            if (bodyObj.has("key")) {
                imgUrl = bodyObj.getString("key");
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            imgUrl = "";
        }
        return imgUrl;

    }

    /*
     * 重写equals方法修饰符必须是public,因为是重写的Object的方法. 2.参数类型必须是Object.
     */
    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Msg))
            return false;

        final Msg otherMsg = (Msg) other;
        return getMid().equals(otherMsg.getMid());
    }
}

