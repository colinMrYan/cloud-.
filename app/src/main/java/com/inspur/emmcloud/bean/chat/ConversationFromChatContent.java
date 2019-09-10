package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.bean.contact.Contact;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.json.JSONArray;

import java.io.Serializable;

/**
 * Created by libaochao on 2019/8/23.
 */

public class ConversationFromChatContent implements Serializable {

    private Conversation conversation = new Conversation();
    private int messageNum = 0;
    private Contact singleChatContactUser;
    public ConversationFromChatContent(Conversation conversation, int messageNum) {
        this.conversation = conversation;
        this.messageNum = messageNum;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public int getMessageNum() {
        return messageNum;
    }

    public void setMessageNum(int messageNum) {
        this.messageNum = messageNum;
    }

    public Contact getSingleChatContactUser() {
        return singleChatContactUser;
    }

    public void setSingleChatContactUser(Contact singleChatContactUser) {
        this.singleChatContactUser = singleChatContactUser;
    }

    public void initSingleChatContact() {
        if (conversation != null && conversation.getType().equals(Conversation.TYPE_DIRECT)) {
            String ownerUid = BaseApplication.getInstance().getUid();
            JSONArray members = JSONUtils.getJSONArray(conversation.getMembers(), null);
            try {
                if (ownerUid.equals(members.getString(0))) {
                    ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(members.getString(1));
                    singleChatContactUser = new Contact(contactUser);
                } else {
                    ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(members.getString(0));
                    singleChatContactUser = new Contact(contactUser);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
