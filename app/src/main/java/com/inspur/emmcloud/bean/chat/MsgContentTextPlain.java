package com.inspur.emmcloud.bean.chat;

import android.text.TextUtils;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenmch on 2018/2/6.
 */

public class MsgContentTextPlain {
    private String text;
    private String tmpId;
    private Map<String, String> mentionsMap = new HashMap<>();
    private List<String> whisperUsers = new ArrayList<>();
    private String msgType = "";

    public MsgContentTextPlain(String Json) {
        JSONObject object = JSONUtils.getJSONObject(Json);
        text = JSONUtils.getString(object, "text", "");
        tmpId = JSONUtils.getString(object, "tmpId", "");
        JSONObject mentionObj = JSONUtils.getJSONObject(object, "mentions", null);
        if (mentionObj != null) {
            mentionsMap = JSONUtils.parseKeyAndValueToMap(mentionObj);
        }
        whisperUsers = JSONUtils.getStringList(object, "whispers", new ArrayList<String>());
        msgType = JSONUtils.getString(object, "messageType", "");
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

    public List<String> getWhisperUsers() {
        return whisperUsers;
    }

    public void setWhisperUsers(List<String> whisperUsers) {
        this.whisperUsers = whisperUsers;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("text", text);
            if (mentionsMap.size() > 0) {
                JSONObject mentionObj = JSONUtils.map2Json(mentionsMap);
                obj.put("mentions", mentionObj);
            }
            if (whisperUsers.size() > 0) {
                JSONArray whisperObj = JSONUtils.toJSONArray(whisperUsers);
                obj.put("whispers", whisperObj);
            }
            if (!TextUtils.isEmpty(msgType)) {
                obj.put("messageType", msgType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
