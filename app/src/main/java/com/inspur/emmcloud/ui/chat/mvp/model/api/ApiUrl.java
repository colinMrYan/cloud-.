package com.inspur.emmcloud.ui.chat.mvp.model.api;

import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;

/**
 * Created by libaochao on 2019/10/14.
 */

public class ApiUrl {
    /**
     * EcmChat服务
     *
     * @return
     */
    public static String getECMChatUrl() {
        return WebServiceRouterManager.getInstance().getClusterChat();
//        return "http://10.25.12.114:3000/chat";
    }

    /**
     * 获取chat v1 channel base url
     *
     * @return
     */
    public static String getECMChatConversationBaseUrl() {
        return getECMChatUrl() + "/api/v1";
    }

    /**
     * 设置会话是否消息免打扰
     *
     * @return
     */
    public static String getConversationSetDnd(String id) {
        return getECMChatConversationBaseUrl() + "/channel/" + id + "/dnd";
    }

    /**
     * 设置会话是否置顶
     *
     * @return
     */
    public static String getConversationSetStick(String id) {
        return getECMChatConversationBaseUrl() + "/channel/" + id + "/focus";
    }

    /**
     * 获取退出群聊url
     *
     * @param cid
     * @return
     */
    public static String getQuitChannelGroupUrl(String cid) {
        return getECMChatConversationBaseUrl() + "/channel/group/" + cid + "/participation";
    }

    /**
     * 获取删除频道url
     *
     * @param cid
     * @return
     */
    public static String getDeleteChannelUrl(String cid) {
        return getECMChatConversationBaseUrl() + "/channel/" + cid;
    }


    /**
     * 修改群组成员
     *
     * @param id
     * @return
     */
    public static String getModifyGroupMemberUrl(String id) {
        return getECMChatConversationBaseUrl() + "/channel/group/" + id + "/member";
    }

    /**
     * 获取会话信息
     *
     * @param id
     * @return
     */
    public static String getConversationInfoUrl(String id) {
        return getECMChatConversationBaseUrl() + "/channel/" + id;
    }


}
