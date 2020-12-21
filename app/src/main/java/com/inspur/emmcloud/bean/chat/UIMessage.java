package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MarkDownLinkCacheUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.inspur.emmcloud.basemodule.bean.ChannelMessageStates.DELIVERED;
import static com.inspur.emmcloud.basemodule.bean.ChannelMessageStates.READ;
import static com.inspur.emmcloud.basemodule.bean.ChannelMessageStates.SENT;

public class UIMessage implements Serializable {
    private String id;
    private Message message;
    private String senderName;
    private Long creationDate;
    private String senderPhotoUrl;
    private int sendStatus = 1;//0 发送中  1发送成功  2发送失败
    private int read = 0;  //0 未读，1 已读
    private List<MarkDownLink> markDownLinkList = new ArrayList<>();
    private int voicePlayState = 0;//0 未下载，1下载中，2，下载完成，3，未播放,4，播放中，5，播放完成，6，播放停止
    private Map<String, Set<String>> statesMap = new HashMap<>(); //消息的已读未读列表

    public UIMessage(Message message) {
        this.message = message;
        this.id = message.getId();
        this.creationDate = message.getCreationDate();
        this.sendStatus = message.getSendStatus();
        this.read = message.getRead();
        senderName = ContactUserCacheUtils.getUserName(message.getFromUser());
        senderPhotoUrl = APIUri.getUserIconUrl(MyApplication.getInstance(), message.getFromUser());
        if (message.getType().equals(Message.MESSAGE_TYPE_TEXT_MARKDOWN)) {
            markDownLinkList = MarkDownLinkCacheUtils.getMarkDownLinkListByMid(MyApplication.getInstance(), message.getId());
        }
        dealStates(message.getStates());
    }

    private void dealStates(String states) {
        try {
            JSONObject statesJson = new JSONObject(states);
            if (statesJson.has(SENT)) {
                JSONArray sentArray = statesJson.optJSONArray(SENT);
                Set<String> sentList = new HashSet<>();
                for (int i = 0; i < sentArray.length(); i++) {
                    sentList.add(sentArray.getString(i));
                }
                statesMap.put(SENT, sentList);
            }
            if (statesJson.has(DELIVERED)) {
                JSONArray deliveredArray = statesJson.optJSONArray(DELIVERED);
                Set<String> deliveredList = new HashSet<>();
                for (int i = 0; i < deliveredArray.length(); i++) {
                    deliveredList.add(deliveredArray.getString(i));
                }
                statesMap.put(DELIVERED, deliveredList);
            }
            if (statesJson.has(READ)) {
                JSONArray readArray = statesJson.optJSONArray(READ);
                Set<String> readList = new HashSet<>();
                for (int i = 0; i < readArray.length(); i++) {
                    readList.add(readArray.getString(i));
                }
                statesMap.put(READ, readList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public UIMessage() {
    }

    public UIMessage(String id) {
        this.id = id;
    }

    public static List<UIMessage> MessageList2UIMessageList(List<Message> messageList) {
        List<UIMessage> UIMessageList = new ArrayList<>();
        if (messageList != null && messageList.size() > 0) {
            for (Message message : messageList) {
                UIMessageList.add(new UIMessage(message));
            }
        }
        return UIMessageList;

    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getId() {
        return id;
    }

    public int getRead() {
        return read;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Map<String, Set<String>> getStatesMap(){
        return statesMap;
    }

    public void setStatesMap(Map<String, Set<String>> statesMap){
        this.statesMap = statesMap;
    }

    public List<MarkDownLink> getMarkDownLinkList() {
        return markDownLinkList;
    }

    public void setMarkDownLinkList(List<MarkDownLink> markDownLinkList) {
        this.markDownLinkList = markDownLinkList;
    }

    public String getSenderPhotoUrl() {
        return senderPhotoUrl;
    }

    public void setSenderPhotoUrl(String senderPhotoUrl) {
        this.senderPhotoUrl = senderPhotoUrl;
    }

    public int getVoicePlayState() {
        return voicePlayState;
    }

    public void setVoicePlayState(int voicePlayState) {
        this.voicePlayState = voicePlayState;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof UIMessage))
            return false;

        final UIMessage otherUIMsg = (UIMessage) other;
        return getId().equals(otherUIMsg.getId());
    }


}

