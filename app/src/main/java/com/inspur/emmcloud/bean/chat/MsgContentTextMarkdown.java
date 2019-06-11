package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentTextMarkdown {
    private String title;
    private String text;
    private String tmpId;
    private Map<String, String> mentionsMap = new HashMap<>();

    public MsgContentTextMarkdown(String content) {
        JSONObject object = JSONUtils.getJSONObject(content);
        text = JSONUtils.getString(object, "text", "");
        tmpId = JSONUtils.getString(object, "tmpId", "");
        JSONObject mentionObj = JSONUtils.getJSONObject(object, "mentions", null);
        if (mentionObj != null) {
            mentionsMap = JSONUtils.parseKeyAndValueToMap(object);
        }
        title = JSONUtils.getString(object, "title", "");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
}
