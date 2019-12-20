package com.inspur.emmcloud.ui.chat.mvp.presenter;

import com.inspur.emmcloud.basemodule.mvp.BasePresenter;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.ui.chat.mvp.contract.ConversionSearchContract;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConversationSearchPresenter extends BasePresenter<ConversionSearchContract.View> implements ConversionSearchContract.Presenter {

    List<Conversation> conversationList = new ArrayList<>();

    public ConversationSearchPresenter() {
    }

    @Override
    public List<Conversation> getConversationData() {
        List<Conversation> list = ConversationCacheUtils.getConversationListByLastUpdate(mView.getContext());
        Iterator<Conversation> iterator = list.iterator();
        List<Conversation> stickConversationList = new ArrayList<>();
        while (iterator.hasNext()) {
            Conversation conversation = iterator.next();
            if (conversation.isStick()) {
                stickConversationList.add(conversation);
                iterator.remove();
            }
        }
        list.addAll(0, stickConversationList);
        mView.showConversationData(list);
        return list;
    }
}
