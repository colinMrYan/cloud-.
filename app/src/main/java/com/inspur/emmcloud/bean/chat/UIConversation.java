package com.inspur.emmcloud.bean.chat;

import android.text.SpannableString;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.util.privates.ChatMsgContentUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.DirectChannelUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.inspur.emmcloud.util.privates.richtext.markdown.MarkDown;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chenmch on 2018/9/22.
 */

public class UIConversation implements Serializable {
    private String id;
    private Conversation conversation;
    private List<Message> messageList;
    private String title;
    private long lastUpdate;
    private long unReadCount = 0;
    private String content;
    private String icon = "";

    public UIConversation() {
    }

    public UIConversation(String id) {
        this.id = id;
    }

    public UIConversation(Conversation conversation) {
        this.conversation = conversation;
        this.id = conversation.getId();
        this.title = CommunicationUtils.getConversationTitle(conversation);
        messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
        if (messageList.size() == 0) {
            lastUpdate = conversation.getCreationDate();
        } else {
            lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
            unReadCount = MessageCacheUtil.getChannelMessageUnreadCount(MyApplication.getInstance(), id);
        }
        conversation.setLastUpdate(lastUpdate);
        setUIConversationIcon();
        setUIConversationContent();

    }

    public static List<UIConversation> conversationList2UIConversationList(List<Conversation> conversationList) {
        List<UIConversation> uiConversationList = new ArrayList<>();
        if (conversationList != null && conversationList.size() > 0) {
            for (Conversation conversation : conversationList) {
                uiConversationList.add(new UIConversation(conversation));
            }
        }
        return uiConversationList;
    }

    private void setUIConversationIcon() {
        switch (conversation.getType()) {
            case Conversation.TYPE_DIRECT:
                icon = DirectChannelUtils.getDirectChannelIcon(MyApplication.getInstance(), conversation.getName());
                break;
            case Conversation.TYPE_CAST:
                icon = DirectChannelUtils.getRobotIcon(MyApplication.getInstance(), conversation.getName());
                break;
            case Conversation.TYPE_LINK:
                icon = conversation.getAvatar();
                break;
            default:
                icon = "drawable//" + R.drawable.icon_channel_group_default;
                break;
        }
    }

    private void setUIConversationContent() {
        String type = conversation.getType();
        if (messageList.size() > 0) {
            Message message = messageList.get(messageList.size() - 1);
            String fromUserName = "";
            String messageType = message.getType();
            if (!StringUtils.isBlank(message.getRecallFrom())) {
                content = CommunicationUtils.getRecallMessageShowContent(message);
            } else {
                if (type.equals(Conversation.TYPE_GROUP) && !message.getFromUser().equals(MyApplication.getInstance().getUid())) {
                    fromUserName = ContactUserCacheUtils.getUserName(message.getFromUser()) + "：";
                }
                switch (messageType) {
                    case Message.MESSAGE_TYPE_TEXT_PLAIN:
                        content = ChatMsgContentUtils.mentionsAndUrl2Span(message.getMsgContentTextPlain().getText(), message.getMsgContentTextPlain().getMentionsMap()).toString();
                        break;
                    case Message.MESSAGE_TYPE_TEXT_MARKDOWN:
                        SpannableString spannableString = ChatMsgContentUtils.mentionsAndUrl2Span(message.getMsgContentTextMarkdown().getText(), message.getMsgContentTextMarkdown().getMentionsMap());
                        content = spannableString.toString();
                        if (!StringUtils.isBlank(content)) {
                            content = MarkDown.fromMarkdown(content);
                        }
                        break;
                    case Message.MESSAGE_TYPE_COMMENT_TEXT_PLAIN:
                        content = MyApplication.getInstance().getString(R.string.send_a_comment);
                        break;
                    case Message.MESSAGE_TYPE_FILE_REGULAR_FILE:
                        content = MyApplication.getInstance().getString(R.string.send_a_file);
                        break;
                    case Message.MESSAGE_TYPE_MEDIA_IMAGE:
                        content = MyApplication.getInstance().getString(R.string.send_a_picture);
                        break;
                    case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                        content = MyApplication.getInstance().getString(R.string.send_a_link);
                        break;
                    case Message.MESSAGE_TYPE_EXTENDED_CONTACT_CARD:
                        content = MyApplication.getInstance().getString(R.string.send_a_link);
                        break;
                    case Message.MESSAGE_TYPE_MEDIA_VOICE:
                        content = MyApplication.getInstance().getString(R.string.send_a_voice);
                        break;
                    case Message.MESSAGE_TYPE_EXTENDED_SELECTED:
                        content = MyApplication.getInstance().getString(R.string.send_action_message);
                        break;
                    default:
                        content = MyApplication.getInstance()
                                .getString(R.string.send_a_message_of_unknown_type);
                        break;
                }
                content = fromUserName + content;
            }

        } else {
            if (type.equals(Conversation.TYPE_CAST)) {
                content = MyApplication.getInstance().getString(R.string.welcome_to_attention) + " " + title;
            } else if (type.equals(Conversation.TYPE_GROUP)) {
                content = MyApplication.getInstance().getString(R.string.group_no_message);
            } else if (type.equals(Conversation.TYPE_LINK)) {
                content = MyApplication.getInstance().getString(R.string.welcome_to) + " " + title;
            } else {
                content = MyApplication.getInstance().getString(R.string.direct_no_message);
            }
        }
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
        if (!(other instanceof UIConversation))
            return false;

        final UIConversation uiConversation = (UIConversation) other;
        return getId().equals(uiConversation.getId());
    }

    public class SortComparator implements Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {
            UIConversation uiConversationA = (UIConversation) lhs;
            UIConversation uiConversationB = (UIConversation) rhs;
            long diff = uiConversationA.getLastUpdate() - uiConversationB.getLastUpdate();
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
