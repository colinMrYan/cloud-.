package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentMediaImage {
    private String name;
    private int thumbnailWidth;
    private int thumbnailHeight;
    private long thumbnailSize;
    private String thumbnailMedia;
    private int previewWidth;
    private int previewHeight;
    private long previewSize;
    private String previewMedia;
    private int rawWidth;
    private int rawHeight;
    private long rawSize;
    private String rawMedia;
    private String tmpId;

    public MsgContentMediaImage() {

    }

    public MsgContentMediaImage(String content) {
        JSONObject obj = JSONUtils.getJSONObject(content);
        name = JSONUtils.getString(obj, "name", "");
        JSONObject thumbnailObj = JSONUtils.getJSONObject(obj, "thumbnail", new JSONObject());
        thumbnailWidth = JSONUtils.getInt(thumbnailObj, "width", 0);
        thumbnailHeight = JSONUtils.getInt(thumbnailObj, "height", 0);
        thumbnailSize = JSONUtils.getLong(thumbnailObj, "size", 0);
        thumbnailMedia = JSONUtils.getString(thumbnailObj, "media", "");
        JSONObject previewObj = JSONUtils.getJSONObject(obj, "preview", new JSONObject());
        previewWidth = JSONUtils.getInt(previewObj, "width", 0);
        previewHeight = JSONUtils.getInt(previewObj, "height", 0);
        previewSize = JSONUtils.getLong(previewObj, "size", 0);
        previewMedia = JSONUtils.getString(previewObj, "media", "");
        JSONObject rawObj = JSONUtils.getJSONObject(obj, "raw", new JSONObject());
        rawWidth = JSONUtils.getInt(rawObj, "width", 0);
        rawHeight = JSONUtils.getInt(rawObj, "height", 0);
        rawSize = JSONUtils.getLong(rawObj, "size", 0);
        rawMedia = JSONUtils.getString(rawObj, "media", "");
        tmpId = JSONUtils.getString(rawObj, "tmpId", "");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public void setThumbnailHeight(int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public long getThumbnailSize() {
        return thumbnailSize;
    }

    public void setThumbnailSize(long thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }

    public String getThumbnailMedia() {
        return thumbnailMedia;
    }

    public void setThumbnailMedia(String thumbnailMedia) {
        this.thumbnailMedia = thumbnailMedia;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public long getPreviewSize() {
        return previewSize;
    }

    public void setPreviewSize(long previewSize) {
        this.previewSize = previewSize;
    }

    public String getPreviewMedia() {
        return previewMedia;
    }

    public void setPreviewMedia(String previewMedia) {
        this.previewMedia = previewMedia;
    }

    public int getRawWidth() {
        return rawWidth;
    }

    public void setRawWidth(int rawWidth) {
        this.rawWidth = rawWidth;
    }

    public int getRawHeight() {
        return rawHeight;
    }

    public void setRawHeight(int rawHeight) {
        this.rawHeight = rawHeight;
    }

    public long getRawSize() {
        return rawSize;
    }

    public void setRawSize(long rawSize) {
        this.rawSize = rawSize;
    }

    public String getRawMedia() {
        return rawMedia;
    }

    public void setRawMedia(String rawMedia) {
        this.rawMedia = rawMedia;
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
//            JSONObject thumbnailObj = new JSONObject();
//            thumbnailObj.put("width",getThumbnailWidth());
//            thumbnailObj.put("height",getThumbnailHeight());
//            thumbnailObj.put("size",getThumbnailSize());
//            thumbnailObj.put("media",getThumbnailMedia());
//            JSONObject previewObj = new JSONObject();
//            previewObj.put("width",getPreviewWidth());
//            previewObj.put("height",getPreviewHeight());
//            previewObj.put("size",getPreviewSize());
//            previewObj.put("media",getPreviewMedia());
            JSONObject rawObj = new JSONObject();
            rawObj.put("width", getRawWidth());
            rawObj.put("height", getRawHeight());
            rawObj.put("size", getRawSize());
            rawObj.put("media", getRawMedia());
            rawObj.put("tmpId", getTmpId());
            obj.put("name", getName());
//            obj.put("thumbnail", thumbnailObj);
//            obj.put("preview", previewObj);
            obj.put("raw", rawObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
