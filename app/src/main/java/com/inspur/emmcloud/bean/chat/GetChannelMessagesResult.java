package com.inspur.emmcloud.bean.chat;


import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GetChannelMessagesResult {
    private List<Message> messageList = new ArrayList<>();

    public GetChannelMessagesResult(String response) {
        JSONObject obj = JSONUtils.getJSONObject(response);
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            JSONArray array = JSONUtils.getJSONArray(obj, key, new JSONArray());
            for (int i = 0; i < array.length(); i++) {
                JSONObject messageObj = JSONUtils.getJSONObject(array, i, new JSONObject());
                Message message = new Message(messageObj);
                message.setRead(1);
                messageList.add(message);
            }
        }
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }
}
