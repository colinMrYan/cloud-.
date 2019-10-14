package com.inspur.emmcloud.ui.chat.mvp.model.api;

import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;

import java.util.ArrayList;

/**
 * Created by libaochao on 2019/10/14.
 */

public class ApiService {
    interface IGroupInfoActivity {
        /**
         * 置顶设置、消息免打扰设置、修改群成员设置、退出群聊、删除频道
         **/
        void setConversationStick(BaseModuleAPICallback apiCallback, boolean stickyState, String conversationId);

        void setMuteNotification(BaseModuleAPICallback apiCallback, boolean muteNotificationState, String conversationId);

        void addGroupMembers(BaseModuleAPICallback apiCallback, ArrayList<String> uidList, String conversationId);

        void delGroupMembers(BaseModuleAPICallback apiCallback, ArrayList<String> uidList, String conversationId);

        void quitGroupChannel(BaseModuleAPICallback apiCallback, String conversationId);

        void delChannel(BaseModuleAPICallback apiCallback, String conversationId);

    }
}
