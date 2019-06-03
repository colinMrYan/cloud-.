package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.util.privates.LanguageManager;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentMediaVoice {
    private int duration;
    private String media;
    private String result;
    private String tmpId;
    private JSONObject jsonObject;

    public MsgContentMediaVoice(String content) {
        JSONObject object = JSONUtils.getJSONObject(content);
        duration = JSONUtils.getInt(object, "duration", 0);
        tmpId = JSONUtils.getString(object, "tmpId", "");
        if (duration == 0) {
            duration = 1;
        }
        media = JSONUtils.getString(object, "media", "");
        jsonObject = JSONUtils.getJSONObject(object, "subtitles", new JSONObject());
        result = getFinalResult(jsonObject);
    }

    public MsgContentMediaVoice() {

    }

    /**
     * 获取显示文字
     *
     * @param jsonObject
     * @return
     */
    private String getFinalResult(JSONObject jsonObject) {
        String resultStr = "";
        Iterator<String> jsonObjectKeyIter = jsonObject.keys();
        while (jsonObjectKeyIter.hasNext()) {
            String key = jsonObjectKeyIter.next();
            String value = JSONUtils.getString(jsonObject, key, "");
            if (!StringUtils.isBlank(value)) {
                resultStr = value;
            }
        }
        return resultStr;
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

    public String getResult() {
        return result;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonResults(String results) {
        JSONObject jsonObj = new JSONObject();
        try {
            switch (LanguageManager.getInstance().getCurrentAppLanguage()) {
                case "zh-Hans":
                    jsonObj.put("zh-cn", results);
                    break;
                case "en":
                    jsonObj.put("en-us", results);
                    break;
                default:
                    jsonObj.put("zh-cn", results);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.jsonObject = jsonObj;
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
            obj.put("duration", duration);
            obj.put("media", media);
            obj.put("subtitles", jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
