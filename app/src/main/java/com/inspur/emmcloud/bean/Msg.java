package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.LogUtils;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Transient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Msg implements Serializable {
    private static final String TAG = "Msg";
    // @Id 如果主键没有命名名为id或_id的时，需要为主键添加此注解
    //  @NoAutoIncrement  int,long类型的id默认自增，不想使用自增时添加此注解
    @Id
    private String mid = "";
    private String time = "";
    private String type = "";
    private String body = "";
    private String uid = "";
    private String title = "";
    private String avatar = "";
    private String commentContent = "";
    private String originalMsgMid = "";
    private String originalMsgPreview = "";
    private String cid = "";  //channel id
    private String nid = "";
    private String nTitle = "";
    private String nDigest = "";
    private String nAuthor = "";
    private String nUrl = "";
    private String nPublisher = "";
    private String nPostTime = "";
    private boolean isHaveRead = false;
    private String privates = "";
    @Transient
    private int sendStatus = 1;//0 发送中  1发送成功  2发送失败

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
                this.time = obj.getString("timestamp");
            }
            if (obj.has("type")) {
                this.type = obj.getString("type");
            }
            if (obj.has("body")) {
                this.body = obj.getString("body");
                if (type.equals("news")) {
                    try {
                        JSONObject jsonObject = new JSONObject(body);
                        if (jsonObject.has("nid")) {
                            this.nid = jsonObject.getString("nid");
                        }
                        if (jsonObject.has("title")) {
                            this.nTitle = jsonObject.getString("title");
                        }
                        if (jsonObject.has("digest")) {
                            this.nDigest = jsonObject.getString("digest");
                        }
                        if (jsonObject.has("author")) {
                            this.nAuthor = jsonObject.getString("author");
                        }
                        if (jsonObject.has("publisher")) {
                            this.nPublisher = jsonObject.getString("publisher");
                        }
                        if (jsonObject.has("url")) {
                            this.nUrl = jsonObject.getString("url");
                        }
                        if (jsonObject.has("posttime")) {
                            this.nPostTime = jsonObject.getString("posttime");
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
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
            if (type.equals("txt_comment")) {
                JSONObject bodyJsonObject = new JSONObject(body);
                if (bodyJsonObject.has("content")) {
                    this.commentContent = bodyJsonObject.getString("content");
                }


                if (bodyJsonObject.has("quote")) {
                    JSONObject quoteJsonObject = new JSONObject(bodyJsonObject.getString("quote"));
                    if (quoteJsonObject.has("preview")) {
                        originalMsgPreview = quoteJsonObject.getString("preview");
                        originalMsgMid = quoteJsonObject.getString("mid");
                    }
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

    public String getMid() {
        return mid;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    public String getUid() {
        return uid;
    }

    public String getCid() {
        return cid;
    }

    public String getTitle() {
        return title;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getNPublisher() {
        return nPublisher;
    }

    public String getNTitle() {
        return nTitle;
    }

    public String getCommentOriMid() {
        return originalMsgMid;
    }

    public String getCommentOriPre() {
        return originalMsgPreview;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setCid(String cid){
        this.cid = cid;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public void setHaveRead(boolean isHaveRead) {
        this.isHaveRead = isHaveRead;
    }

    public boolean getIsHaveRead() {
        return isHaveRead;
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
        if (!getMid().equals(otherMsg.getMid()))
            return false;
        return true;
    }
}

