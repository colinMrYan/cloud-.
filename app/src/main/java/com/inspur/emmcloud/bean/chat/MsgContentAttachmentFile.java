package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentAttachmentFile {
    private String category;
    private String name;
    private long size;
    private String media;
    public MsgContentAttachmentFile(String content) {
        JSONObject object = JSONUtils.getJSONObject(content);
        category = JSONUtils.getString(object,"category","");
        name = JSONUtils.getString(object,"name","");
        media = JSONUtils.getString(object,"media","");
        size = JSONUtils.getLong(object,"size",0);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }
}
