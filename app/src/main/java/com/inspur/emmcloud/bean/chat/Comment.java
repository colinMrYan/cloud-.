package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;

import org.json.JSONObject;

import java.io.Serializable;

public class Comment implements Serializable {
    private String mid = "";
    private String uid = "";
    private String title = "";
    private String avatar = "";
    private Long time = 0L;
    private String order = "";
    private String type = "";
    private String preview = "";
    private String content = "";
    private String source = "";
    private String msgBody = "";

    public Comment(String title, String content, String uid, Long time) {
        this.title = title;
        this.source = content;
        this.uid = uid;
        this.time = time;
    }

    public Comment(Msg msg) {
        this.mid = msg.getMid();
        this.uid = msg.getUid();
        this.avatar = msg.getAvatar();
        this.title = msg.getTitle();
        this.time = msg.getTime();
//		this.order = msg.getOrder();
        this.type = msg.getType();
//		this.preview = msg.getPreview();

        String body = msg.getBody();
        try {
            JSONObject jsonObject = new JSONObject(body);
            if (jsonObject.has("source")) {
                this.source = jsonObject.getString("source");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public Comment(JSONObject jsonObject) {

        try {
            if (jsonObject.has("mid")) {
                mid = jsonObject.getString("mid");
            }
            if (jsonObject.has("from")) {

                JSONObject jsonFrom = jsonObject.getJSONObject("from");
                if (jsonFrom.has("uid")) {
                    uid = jsonFrom.getString("uid");
                }
                if (jsonFrom.has("title")) {
                    title = jsonFrom.getString("title");
                }
                if (jsonFrom.has("avatar")) {
                    avatar = jsonFrom.getString("avatar");
                }
            }
            if (jsonObject.has("timestamp")) {
                String timestamp = jsonObject.getString("timestamp");
                time = TimeUtils.UTCString2Long(timestamp);
            }
            if (jsonObject.has("order")) {
                order = jsonObject.getString("order");
            }
            if (jsonObject.has("type")) {
                type = jsonObject.getString("type");
            }
            if (jsonObject.has("body")) {
                msgBody = JSONUtils.getString(jsonObject, "body", "");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }
}
