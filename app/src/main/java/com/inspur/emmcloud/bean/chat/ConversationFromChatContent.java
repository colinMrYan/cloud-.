package com.inspur.emmcloud.bean.chat;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.json.JSONArray;

/**
 * Created by libaochao on 2019/8/23.
 */

public class ConversationFromChatContent {

    private Conversation conversation = new Conversation();
    private int messageNum = 0;
    private ContactUser singleChatContactUser;

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

    public ContactUser getSingleChatContactUser() {
        return singleChatContactUser;
    }

    public void setSingleChatContactUser(ContactUser singleChatContactUser) {
        this.singleChatContactUser = singleChatContactUser;
    }

    public void initSingleChatContact() {
        if (conversation != null && conversation.getType().equals(Conversation.TYPE_DIRECT)) {
            String ownerUid = BaseApplication.getInstance().getUid();
            JSONArray members = JSONUtils.getJSONArray(conversation.getMembers(), null);
            try {

                LogUtils.LbcDebug("members" + conversation.getMembers());
                LogUtils.LbcDebug("members0" + members.getString(0));
                LogUtils.LbcDebug("members1" + members.getString(1));
                if (ownerUid.equals(members.getString(0))) {
                    singleChatContactUser = ContactUserCacheUtils.getContactUserByUid(members.getString(1));
                    LogUtils.LbcDebug("11111111111111111111111111111");
                } else {
                    singleChatContactUser = ContactUserCacheUtils.getContactUserByUid(members.getString(0));
                    LogUtils.LbcDebug("33333333333322222222222333333333");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
