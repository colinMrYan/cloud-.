package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentTextPlain {
    private String text;
    private String tmpId;
    private Map<String, String> mentionsMap = new HashMap<>();

    public MsgContentTextPlain(String Json) {
        JSONObject object = JSONUtils.getJSONObject(Json);
        text = JSONUtils.getString(object, "text", "");
        tmpId = JSONUtils.getString(object, "tmpId", "");
        JSONObject mentionObj = JSONUtils.getJSONObject(object, "mentions", null);
        if (mentionObj != null) {
            mentionsMap = JSONUtils.parseKeyAndValueToMap(mentionObj);
        }
    }

    public MsgContentTextPlain() {

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, String> getMentionsMap() {
        return mentionsMap;
    }

    public void setMentionsMap(Map<String, String> mentionsMap) {
        this.mentionsMap = mentionsMap;
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
            obj.put("text", text);
            if (mentionsMap.size() > 0) {
                JSONObject mentionObj = JSONUtils.map2Json(mentionsMap);
                obj.put("mentions", mentionObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
