package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;

import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentMediaVoice {
    private int duration;
    private String media;
    private String result;
    private JSONObject jsonObject;

    public MsgContentMediaVoice(String content) {
        JSONObject object = JSONUtils.getJSONObject(content);
        duration = JSONUtils.getInt(object,"duration",0);
        if (duration == 0){
            duration = 1;
        }
        media = JSONUtils.getString(object,"media","");
        jsonObject = JSONUtils.getJSONObject(object,"subtitles",new JSONObject());
        result = getFinalResult(jsonObject);
    }

    /**
     * 获取显示文字
     * @param jsonObject
     * @return
     */
    private String getFinalResult(JSONObject jsonObject) {
        String resultStr = "";
        Iterator<String> jsonObjectKeyIter = jsonObject.keys();
        while (jsonObjectKeyIter.hasNext()){
            String key = jsonObjectKeyIter.next();
            String value = JSONUtils.getString(jsonObject,key,"");
            if(!StringUtils.isBlank(value)){
                resultStr = value;
            }
        }
        return resultStr;
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

    public String getResult() {
        return result;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonResults(String results) {
        JSONObject jsonObj = new JSONObject();
        try {
            switch(AppUtils.getCurrentAppLanguage(MyApplication.getInstance())){
                case "zh-Hans":
                    jsonObj.put("zh-cn",results);
                    break;
                case "en":
                    jsonObj.put("en-us",results);
                    break;
                default:
                    jsonObj.put("zh-cn",results);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.jsonObject = jsonObj;
    }

    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("duration", duration);
            obj.put("media", media);
            obj.put("subtitles",jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
