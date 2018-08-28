package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentMediaVoice {
    private int duration;
    private String media;
    public MsgContentMediaVoice(String content) {
        JSONObject object = JSONUtils.getJSONObject(content);
        duration = JSONUtils.getInt(object,"duration",0);
        if (duration == 0){
            duration = 1;
        }
        media = JSONUtils.getString(object,"media","");
    }

    public MsgContentMediaVoice(){

    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("duration", duration);
            obj.put("media", media);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
