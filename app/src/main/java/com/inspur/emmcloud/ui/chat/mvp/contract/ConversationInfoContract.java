package com.inspur.emmcloud.ui.chat.mvp.contract;

import com.inspur.emmcloud.basemodule.mvp.BaseView;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.SearchModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/10/12.
 */

public interface ConversationInfoContract {
    interface Model {

    }

    interface View extends BaseView {

        void initView(Conversation conversation);

        void showGroupMembersHead(List<String> uiUidList);

        void showStickyState(boolean isSticky);

        void showDNDState(boolean isDND);

        void changeConversationTitle(int memberSize);

        void updateUiConversation(Conversation conversation);

        void updateMoreMembers(boolean isShow);

        void quitGroupSuccess();

        void deleteGroupSuccess();

        void createGroupSuccess(Conversation conversation);

        void activityFinish();

        void updateGroupNameSuccess();

    }

    interface Presenter {
        Conversation getConversation(String uid);

        List<String> getConversationUIMembersUid(Conversation conversation);

        List<String> getConversationSingleChatUIMembersUid(Conversation conversation);

        void setConversationStick(boolean stickyState, String conversationId);

        void setMuteNotification(boolean muteNotificationState, String conversationId);

        void addConversationMembers(ArrayList<String> uidList, String conversationId);

        void delConversationMembers(ArrayList<String> uidList, String conversationId);

        void updateSearchMoreState();

        void createGroup(List<SearchModel> addSearchModelList);

        void getConversationInfo(String cid);

        int getConversationRealMemberSize();

        void quitGroupChannel();

        void delChannel();
    }
}
