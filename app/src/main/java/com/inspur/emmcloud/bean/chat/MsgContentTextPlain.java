package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentTextPlain {
    private String text;
    private Map<String,String> mentionsMap = new HashMap<>();
    public MsgContentTextPlain(String Json){
        JSONObject object = JSONUtils.getJSONObject(Json);
        text = JSONUtils.getString(object,"text","");
        mentionsMap = JSONUtils.parseKeyAndValueToMap(object);
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
}
