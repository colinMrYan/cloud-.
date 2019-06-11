package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenmch on 2018/9/13.
 */

public class ChannelMessageReadStateResult {
    private List<String> messageReadIdList = new ArrayList<>();

    public ChannelMessageReadStateResult(String response) {
        JSONObject messagesObj = JSONUtils.getJSONObject(response);
        Iterator<String> messageIdKeys = messagesObj.keys();
        while (messageIdKeys.hasNext()) {
            String key = messageIdKeys.next();
            List<String> channelMessageReadIdList = JSONUtils.getStringList(messagesObj, key, new ArrayList<String>());
            messageReadIdList.addAll(channelMessageReadIdList);
        }
    }

    public List<String> getMessageReadIdList() {
        return messageReadIdList;
    }

    public void setMessageReadIdList(List<String> messageReadIdList) {
        this.messageReadIdList = messageReadIdList;
    }
}
