package com.inspur.emmcloud.mail.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/1/1.
 */

public class MailAttachment {
    private String id;
    private String name;
    private String data;
    private int size;
    private boolean isAttachment;
    private String contentId;

    public MailAttachment(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "");
        contentId = JSONUtils.getString(obj, "contentId", "");
        name = JSONUtils.getString(obj, "name", "");
        size = JSONUtils.getInt(obj, "size", -1);
        isAttachment = JSONUtils.getBoolean(obj, "isAttachment", true);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getSize() {
        return size;
    }

    public boolean isAttachment() {
        return isAttachment;
    }

    public void setAttachment(boolean attachment) {
        isAttachment = attachment;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
}
