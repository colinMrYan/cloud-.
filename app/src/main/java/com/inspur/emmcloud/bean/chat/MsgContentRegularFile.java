package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentRegularFile {
    private String category;
    private String name;
    private long size;
    private String media;
    private String tmpId;

    public MsgContentRegularFile(String content) {
        JSONObject object = JSONUtils.getJSONObject(content);
        category = JSONUtils.getString(object, "category", "");
        name = JSONUtils.getString(object, "name", "");
        media = JSONUtils.getString(object, "media", "");
        size = JSONUtils.getLong(object, "size", 0);
        tmpId = JSONUtils.getString(object, "tmpId", "");
    }

    public MsgContentRegularFile() {

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

    public String getTmpId() {
        return tmpId;
    }

    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }

    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("category", category);
            obj.put("name", name);
            obj.put("size", size);
            obj.put("media", media);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
