package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Date：2022/7/21
 * Author：wang zhen
 * Description 视频消息内容
 */
public class MsgContentMediaVideo {
    private String tmpId;
    private String name;
    private String media; // 视频地址
    private int videoDuration; // 秒
    private String imagePath; // 图片地址，上传到服务后，当前逻辑会将本地地址替换成服务返的图片地址 "/image/IMG_1658477943643.781006.jpg"
    private int imageWidth;
    private int imageHeight;
    private long videoSize;
    private String originMediaPath;

    public MsgContentMediaVideo() {
    }

    public MsgContentMediaVideo(String content) {
        JSONObject object = JSONUtils.getJSONObject(content);
        JSONObject picInfo = JSONUtils.getJSONObject(object, "thumbnail", new JSONObject());
        imagePath = JSONUtils.getString(picInfo, "media", "");
        imageWidth = JSONUtils.getInt(picInfo, "width", 0);
        imageHeight = JSONUtils.getInt(picInfo, "height", 0);
        name = JSONUtils.getString(object, "name", "");
        videoSize = JSONUtils.getLong(object, "size", 0);
        media = JSONUtils.getString(object, "media", "");
        videoDuration = JSONUtils.getInt(object, "duration", 0);
        originMediaPath = JSONUtils.getString(object, "originMediaPath", "");
    }


    public String getTmpId() {
        return tmpId;
    }

    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public int getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(int videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public long getVideoSize() {
        return videoSize;
    }

    public void setVideoSize(long videoSize) {
        this.videoSize = videoSize;
    }

    public String getOriginMediaPath() {
        return originMediaPath;
    }

    public void setOriginMediaPath(String originMediaPath) {
        this.originMediaPath = originMediaPath;
    }

    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            JSONObject picObj = new JSONObject();
            picObj.put("media", getImagePath());
            picObj.put("width", getImageWidth());
            picObj.put("height", getImageHeight());
            obj.put("thumbnail", picObj);
            obj.put("name", getName());
            obj.put("size", getVideoSize());
            obj.put("media", getMedia());
            obj.put("duration", getVideoDuration());
            obj.put("originMediaPath", getOriginMediaPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
