package com.inspur.emmcloud.ui.chat.mvp.presenter;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.mvp.BasePresenter;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.ui.chat.mvp.contract.ChannelGroupInfoContract;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/10/12.
 */

public class ChannelGroupInfoPresenter extends BasePresenter<ChannelGroupInfoContract.View> implements ChannelGroupInfoContract.Presenter {


    @Override
    public Conversation getConversation(String uid) {
        Conversation conversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), uid);
        return conversation;
    }

    @Override
    public List<String> getGroupMembersUid(Conversation conversation) {
        /**
         * 过滤不存在的群成员算法
         */
        //查三十人，如果不满三十人则查实际人数保证查到的人都是存在的群成员
        List<String> conversationMembersList = conversation.getMemberList();
        List<String> uiMemberUidList = new ArrayList<>();
        List<ContactUser> contactUserList = ContactUserCacheUtils.getContactUserListByIdListOrderBy(conversationMembersList, 13);
        ArrayList<String> contactUserIdList = new ArrayList<>();
        for (ContactUser contactUser : contactUserList) {
            contactUserIdList.add(contactUser.getId());
        }
        uiMemberUidList.addAll(contactUserIdList);
        if (conversation.getOwner().equals(BaseApplication.getInstance().getUid())) {
            uiMemberUidList.add("addUser");
            uiMemberUidList.add("deleteUser");
        } else {
            uiMemberUidList.add("addUser");
        }

        return uiMemberUidList;
    }
}
