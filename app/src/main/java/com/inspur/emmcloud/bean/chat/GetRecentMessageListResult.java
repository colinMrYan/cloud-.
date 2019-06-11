package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenmch on 2018/9/13.
 */

public class GetRecentMessageListResult {
    private List<ChannelMessageSet> channelMessageSetList = new ArrayList<>();
    private List<Message> messageList = new ArrayList<>();

    public GetRecentMessageListResult(String response) {
        JSONObject unreadObj = JSONUtils.getJSONObject(response, "unread", new JSONObject());
        JSONObject messagesObj = JSONUtils.getJSONObject(response, "messages", new JSONObject());

        Iterator<String> messageKeys = messagesObj.keys();
        while (messageKeys.hasNext()) {
            List<Message> channelMessageList = new ArrayList<>();
            String key = messageKeys.next();
            JSONArray array = JSONUtils.getJSONArray(messagesObj, key, new JSONArray());
            int unread = JSONUtils.getInt(unreadObj, key, 0);
            if (unread > 100) {
                unread = 100;
            }
            int parseSize = Math.max(unread, 15);
            int messageSize = array.length();
            parseSize = Math.min(parseSize, messageSize);
            for (int i = messageSize - parseSize; i < messageSize; i++) {
                JSONObject obj = JSONUtils.getJSONObject(array, i, new JSONObject());
                Message message = new Message(obj);
                if (messageSize - i <= unread) {
                    message.setRead(0);
                } else {
                    message.setRead(1);
                }
                channelMessageList.add(message);
            }
            if (parseSize > 1) {
                ChannelMessageSet channelMessageSet = new ChannelMessageSet(channelMessageList.get(0).getChannel(), new MatheSet(channelMessageList.get(0).getCreationDate(), channelMessageList.get(parseSize - 1).getCreationDate()));
                channelMessageSetList.add(channelMessageSet);
            }
            messageList.addAll(channelMessageList);
        }

    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    public List<ChannelMessageSet> getChannelMessageSetList() {
        return channelMessageSetList;
    }

    public void setChannelMessageSetList(List<ChannelMessageSet> channelMessageSetList) {
        this.channelMessageSetList = channelMessageSetList;
    }
}
