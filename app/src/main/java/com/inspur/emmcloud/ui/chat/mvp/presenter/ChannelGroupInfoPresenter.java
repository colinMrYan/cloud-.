package com.inspur.emmcloud.ui.chat.mvp.presenter;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.mvp.BasePresenter;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.ui.chat.mvp.contract.ChannelGroupInfoContract;
import com.inspur.emmcloud.ui.chat.mvp.model.api.ApiServiceImpl;
import com.inspur.emmcloud.ui.chat.mvp.model.api.ApiUrl;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/10/12.
 */

public class ChannelGroupInfoPresenter extends BasePresenter<ChannelGroupInfoContract.View> implements ChannelGroupInfoContract.Presenter {

    Conversation mConversation = new Conversation();

    /**
     * 该方法必须有
     * 且先执行
     **/
    @Override
    public Conversation getConversation(String uid) {
        mConversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), uid);
        return mConversation;
    }

    @Override
    public List<String> getGroupUIMembersUid(Conversation conversation) {
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

    @Override
    public void setConversationStick(final boolean stickyState, final String channelId) {
        mView.showLoading();
        String completeUrl = ApiUrl.getConversationSetStick(channelId);
        ApiServiceImpl.getInstance().setConversationStick(new BaseModuleAPICallback(mView.getContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                mView.dismissLoading();
                mView.showStickyState(stickyState);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                mView.dismissLoading();
                mView.showStickyState(!stickyState);
                WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        setConversationStick(stickyState, channelId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                ApiServiceImpl.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        }, stickyState, channelId);
    }

    @Override
    public void setMuteNotification(final boolean muteNotificationState, final String channelId) {
        mView.showLoading();
        String completeUrl = ApiUrl.getConversationSetDnd(channelId);
        ApiServiceImpl.getInstance().setMuteNotification(new BaseModuleAPICallback(mView.getContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                mView.dismissLoading();
                mView.showDNDState(muteNotificationState);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                mView.dismissLoading();
                mView.showDNDState(!muteNotificationState);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        setMuteNotification(muteNotificationState, channelId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                ApiServiceImpl.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        }, muteNotificationState, channelId);
    }

    @Override
    public void addGroupMembers(final ArrayList<String> uidList, final String conversationId) {
        mView.showLoading();
        String completeUrl = ApiUrl.getModifyGroupMemberUrl(conversationId);
        ApiServiceImpl.getInstance().addGroupMembers(new BaseModuleAPICallback(mView.getContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                mView.dismissLoading();
                ArrayList<String> memberUidList = mConversation.getMemberList();
                memberUidList.addAll(uidList);
                mConversation.setMembers(JSONUtils.toJSONString(memberUidList));
                ConversationCacheUtils.setConversationMember(MyApplication.getInstance(), mConversation.getId(), memberUidList);
                mView.changeConversationTitle(memberUidList.size());
                mView.showGroupMembersHead(getGroupUIMembersUid(mConversation));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                mView.dismissLoading();
                WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addGroupMembers(uidList, conversationId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                ApiServiceImpl.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        }, uidList, conversationId);
    }

    @Override
    public void delGroupMembers(final ArrayList<String> uidList, final String conversationId) {
        mView.showLoading();
        String completeUrl = ApiUrl.getModifyGroupMemberUrl(conversationId);
        ApiServiceImpl.getInstance().addGroupMembers(new BaseModuleAPICallback(mView.getContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                mView.dismissLoading();
                ArrayList<String> memberUidList = mConversation.getMemberList();
                memberUidList.removeAll(uidList);
                mConversation.setMembers(JSONUtils.toJSONString(memberUidList));
                ConversationCacheUtils.setConversationMember(MyApplication.getInstance(), mConversation.getId(), memberUidList);
                mView.changeConversationTitle(memberUidList.size());
                mView.showGroupMembersHead(getGroupUIMembersUid(mConversation));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                mView.dismissLoading();
                WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addGroupMembers(uidList, conversationId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                ApiServiceImpl.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        }, uidList, conversationId);
    }

    @Override
    public void quitGroupChannel() {

    }

    @Override
    public void dismissChannel() {

    }
}
