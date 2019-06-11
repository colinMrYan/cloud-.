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

public class GetOfflineMessageListResult {
    private List<ChannelMessageSet> channelMessageSetList = new ArrayList<>();
    private List<Message> messageList = new ArrayList<>();
    private List<Message> mediaVoiceMessageList = new ArrayList<>();

    public GetOfflineMessageListResult(String response) {
        JSONObject messagesObj = JSONUtils.getJSONObject(response);
        Iterator<String> messageKeys = messagesObj.keys();
        while (messageKeys.hasNext()) {
            List<Message> channelMessageList = new ArrayList<>();
            String key = messageKeys.next();
            JSONArray array = JSONUtils.getJSONArray(messagesObj, key, new JSONArray());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = JSONUtils.getJSONObject(array, i, new JSONObject());
                Message message = new Message(obj);
                if (message.getType().equals(Message.MESSAGE_TYPE_MEDIA_VOICE)) {
                    mediaVoiceMessageList.add(message);
                }
                channelMessageList.add(message);
            }
            if (channelMessageList.size() > 1) {
                ChannelMessageSet channelMessageSet = new ChannelMessageSet(channelMessageList.get(0).getChannel(), new MatheSet(channelMessageList.get(0).getCreationDate(), channelMessageList.get(channelMessageList.size() - 1).getCreationDate()));
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

    public List<Message> getMediaVoiceMessageList() {
        return mediaVoiceMessageList;
    }

    public void setMediaVoiceMessageList(List<Message> mediaVoiceMessageList) {
        this.mediaVoiceMessageList = mediaVoiceMessageList;
    }
}
