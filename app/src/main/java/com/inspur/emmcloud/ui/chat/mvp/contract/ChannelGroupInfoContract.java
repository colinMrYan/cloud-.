package com.inspur.emmcloud.ui.chat.mvp.contract;

import com.inspur.emmcloud.basemodule.mvp.BaseView;
import com.inspur.emmcloud.bean.chat.Conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/10/12.
 */

public interface ChannelGroupInfoContract {
    interface Model {

    }

    interface View extends BaseView {
        void showGroupMembersHead(List<String> uiUidList);

        void showStickyState(boolean isSticky);

        void showDNDState(boolean isDND);

        void changeConversationTitle(int memberSize);

        void finishActivity();
    }

    interface Presenter {
        Conversation getConversation(String uid);

        List<String> getGroupUIMembersUid(Conversation conversation);

        void setConversationStick(boolean stickyState, String conversationId);

        void setMuteNotification(boolean muteNotificationState, String conversationId);

        void addGroupMembers(ArrayList<String> uidList, String conversationId);

        void delGroupMembers(ArrayList<String> uidList, String conversationId);

        void quitGroupChannel();

        void delChannel();


    }
}
