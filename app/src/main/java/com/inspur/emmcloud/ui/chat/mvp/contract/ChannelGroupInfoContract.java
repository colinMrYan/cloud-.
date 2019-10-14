package com.inspur.emmcloud.ui.chat.mvp.contract;

import com.inspur.emmcloud.basemodule.mvp.BaseView;
import com.inspur.emmcloud.bean.chat.Conversation;

import java.util.List;

/**
 * Created by libaochao on 2019/10/12.
 */

public interface ChannelGroupInfoContract {
    interface Model {

    }

    interface View extends BaseView {

    }

    interface Presenter {
        Conversation getConversation(String uid);

        List<String> getGroupMembersUid(Conversation conversation);
    }
}
