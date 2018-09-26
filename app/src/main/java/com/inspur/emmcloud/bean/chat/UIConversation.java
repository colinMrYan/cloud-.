package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chenmch on 2018/9/22.
 */

public class UIConversation {
    private String id;
    private Conversation conversation;
    private List<Message> messageList;
    private String title;
    private long lastUpdate;
    private long unReadCount = 0;
    private String content;
    private String icon="";

    public UIConversation() {
    }

    public UIConversation(String id) {
        this.id = id;
    }

    public UIConversation(Conversation conversation) {
        this.conversation = conversation;
        this.id = conversation.getId();
        this.title = CommunicationUtils.getConversationTitle(conversation);
        messageList = MessageCacheUtil.getHistoryMessageList(MyApplication.getInstance(),id,null,15);
        if (messageList.size()==0){
            lastUpdate = conversation.getLastUpdate();
        }else {
            lastUpdate = messageList.get(messageList.size()-1).getCreationDate();
            unReadCount = MessageCacheUtil.getChannelMessageUnreadCount(MyApplication.getInstance(),id);
        }
        switch (conversation.getType()){
            case Conversation.CONVERSATION_TYPE_GROUP:
                icon = "file://"+MyAppConfig.LOCAL_CACHE_PHOTO_PATH+"/"+MyApplication.getInstance().getTanent() + conversation.getId() + "_100.png1";
                break;
            case Conversation.CONVERSATION_TYPE_DIRECT:
                icon = DirectChannelUtils.getDirectChannelIcon(MyApplication.getInstance(), conversation.getName());
                break;
            case Conversation.CONVERSATION_TYPE_CAST:
                icon=DirectChannelUtils.getRobotIcon(MyApplication.getInstance(), conversation.getName());
                break;
        }
    }

    public static List<UIConversation> conversationList2UIConversationList(List<Conversation> conversationList){
        List<UIConversation> uiConversationList = new ArrayList<>();
        if (conversationList != null && conversationList.size() > 0) {
            for (Conversation conversation : conversationList) {
                uiConversationList.add(new UIConversation(conversation));
            }
        }
        return uiConversationList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(long unReadCount) {
        this.unReadCount = unReadCount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean equals(Object other) { // 重写equals方法，后面最好重写hashCode方法

        if (this == other) // 先检查是否其自反性，后比较other是否为空。这样效率高
            return true;
        if (other == null)
            return false;
        if (!(other instanceof Conversation))
            return false;

        final UIConversation uiConversation = (UIConversation) other;
        return getId().equals(uiConversation.getId());
    }

    public class SortComparator implements Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {
            UIConversation uiConversationA = (UIConversation) lhs;
            UIConversation uiConversationB = (UIConversation) rhs;
            long diff = uiConversationA.getLastUpdate()- uiConversationB.getLastUpdate();
            if (diff > 0) {
                return -1;
            } else if (diff == 0) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
