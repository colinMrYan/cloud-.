package com.inspur.emmcloud.bean.chat;

import android.content.Context;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;

import org.json.JSONObject;

public class GroupFileInfo {
    private String url = "";
    private String name = "";
    private String size = "";
    private long time = 0L;
    private String owner = "";
    private String messageId = "";

    public GroupFileInfo(Msg msg) {
        owner = msg.getTitle();
        time = msg.getTime();
        messageId = msg.getMid();
        try {
            JSONObject jsonObject = new JSONObject(msg.getBody());
            if (jsonObject.has("size")) {
                size = jsonObject.getString("size");
            }
            if (jsonObject.has("name")) {
                name = jsonObject.getString("name");
            }
            if (jsonObject.has("key")) {
                url = jsonObject.getString("key");
                url = APIUri.getPreviewUrl(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GroupFileInfo(String url, String name, String size, long time, String owner, String messageId) {
        this.url = url;
        this.name = name;
        this.size = size;
        this.time = time;
        this.owner = owner;
        this.messageId = messageId;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return FileUtils.formatFileSize(size);
    }

    public String getTime(Context context) {
        return TimeUtils.timeLong2YMDString(context, time);
    }

    public String getTime() {
        return time + "";
    }

    public long getLongTime() {
        return time;
    }

    public String getOwner() {
        return owner;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
