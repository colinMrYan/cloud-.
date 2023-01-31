package com.inspur.emmcloud.ui.chat.mvp.contract;

import com.inspur.emmcloud.basemodule.mvp.BaseView;
import com.inspur.emmcloud.componentservice.communication.Conversation;

import java.util.List;

public interface ConversionSearchContract {

    interface Model {

    }

    interface View extends BaseView {
        void showConversationData(List<Conversation> conversationList);

    }

    interface Presenter {
        List<Conversation> getConversationData();

        void getTransmitConversationData();
    }
}
