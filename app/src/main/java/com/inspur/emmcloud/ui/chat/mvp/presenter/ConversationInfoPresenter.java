package com.inspur.emmcloud.ui.chat.mvp.presenter;

import android.content.Intent;
import android.support.annotation.Nullable;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.mvp.BasePresenter;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.ui.chat.mvp.contract.ConversationInfoContract;
import com.inspur.emmcloud.ui.chat.mvp.model.api.ApiServiceImpl;
import com.inspur.emmcloud.ui.chat.mvp.model.api.ApiUrl;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/10/12.
 */

public class ConversationInfoPresenter extends BasePresenter<ConversationInfoContract.View> implements ConversationInfoContract.Presenter {

    private static final int QEQUEST_ADD_MEMBER = 2;
    private static final int QEQUEST_DEL_MEMBER = 3;
    private static final int QEQUEST_FILE_TRANSFER = 4;

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
    public List<String> getConversationUIMembersUid(Conversation conversation) {
        /**
         * 过滤不存在的群成员算法
         */
        //查实际人数保证查到的人都是存在的群成员
        Boolean isOwner = conversation.getOwner().equals(BaseApplication.getInstance().getUid());
        List<String> conversationMembersList = conversation.getMemberList();
        List<String> uiMemberUidList = new ArrayList<>();
        List<ContactUser> contactUserList = ContactUserCacheUtils.getContactUserListByIdListOrderBy(conversationMembersList, isOwner ? 43 : 44);
        ArrayList<String> contactUserIdList = new ArrayList<>();
        for (ContactUser contactUser : contactUserList) {
            contactUserIdList.add(contactUser.getId());
        }
        uiMemberUidList.addAll(contactUserIdList);
        if (conversation.getType().equals(Conversation.TYPE_TRANSFER)) {
            uiMemberUidList.add("fileTransfer");
        } else {
            if (isOwner) {
                uiMemberUidList.add("addUser");
                uiMemberUidList.add("deleteUser");
            } else {
                uiMemberUidList.add("addUser");
            }
        }
        return uiMemberUidList;
    }

    @Override
    public List<String> getConversationSingleChatUIMembersUid(Conversation conversation) {
        List<String> uiUidList = new ArrayList<>();
        String uid = CommunicationUtils.getDirectChannelOtherUid(MyApplication.getInstance(), conversation.getName());
        if (conversation.getType().equals(Conversation.TYPE_TRANSFER)) {
            uiUidList.add("fileTransfer");
        } else {
            uiUidList.add(uid);
            uiUidList.add("addUser");
        }
        return uiUidList;
    }

    @Override
    public void setConversationStick(final boolean stickyState, final String channelId) {
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
    public void addConversationMembers(final ArrayList<String> uidList, final String conversationId) {
        String completeUrl = ApiUrl.getModifyGroupMemberUrl(conversationId);
        ApiServiceImpl.getInstance().addGroupMembers(new BaseModuleAPICallback(mView.getContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                mView.dismissLoading();
                ArrayList<String> memberUidList = mConversation.getMemberList();
                memberUidList.addAll(uidList);
                mConversation.setMembers(JSONUtils.toJSONString(memberUidList));
                ConversationCacheUtils.setConversationMember(MyApplication.getInstance(), mConversation.getId(), memberUidList);
                mView.updateUiConversation(mConversation);
                mView.changeConversationTitle(getConversationRealMemberSize());
                mView.showGroupMembersHead(getConversationUIMembersUid(mConversation));
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
                        addConversationMembers(uidList, conversationId);
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
    public void delConversationMembers(final ArrayList<String> uidList, final String conversationId) {
        String completeUrl = ApiUrl.getModifyGroupMemberUrl(conversationId);
        ApiServiceImpl.getInstance().delGroupMembers(new BaseModuleAPICallback(mView.getContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                mView.dismissLoading();
                ArrayList<String> memberUidList = mConversation.getMemberList();
                memberUidList.removeAll(uidList);
                mConversation.setMembers(JSONUtils.toJSONString(memberUidList));
                ConversationCacheUtils.setConversationMember(MyApplication.getInstance(), mConversation.getId(), memberUidList);
                mView.updateUiConversation(mConversation);
                mView.changeConversationTitle(getConversationRealMemberSize());
                mView.showGroupMembersHead(getConversationUIMembersUid(mConversation));
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
                        addConversationMembers(uidList, conversationId);
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
    public void updateSearchMoreState() {
        Boolean isOwner = mConversation.getOwner().equals(BaseApplication.getInstance().getUid());
        boolean isShowMoreMember = getConversationRealMemberSize() > (isOwner ? 43 : 44);
        mView.updateMoreMembers(isShowMoreMember);
    }

    @Override
    public void getConversationInfo(final String cid) {
        String completeUrl = ApiUrl.getQuitChannelGroupUrl(cid);
        ApiServiceImpl.getInstance().getConversationInfo(new BaseModuleAPICallback(mView.getContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                JSONObject object = JSONUtils.getJSONObject(new String(arg0));
                mConversation = new Conversation(object);
                mView.initView(mConversation);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                mConversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), cid);
                mView.initView(mConversation);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getConversationInfo(cid);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                ApiServiceImpl.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        }, cid);
    }

    @Override
    public void createGroup(List<SearchModel> addSearchList) {
        JSONArray peopleArray = new JSONArray();
        String uid = CommunicationUtils.getDirectChannelOtherUid(MyApplication.getInstance(), mConversation.getName());
        ContactUser contactUser = ContactUserCacheUtils.getContactUserByUid(uid);
        JSONObject jsonObjectOther = new JSONObject();
        try {
            for (int i = 0; i < addSearchList.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("pid", addSearchList.get(i).getId());
                jsonObject.put("name", addSearchList.get(i).getName());
                peopleArray.put(jsonObject);
            }
            jsonObjectOther.put("pid", uid);
            jsonObjectOther.put("name", contactUser.getName());
            peopleArray.put(jsonObjectOther);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // TODO Auto-generated method stub
        new ConversationCreateUtils().createGroupConversation(mView.getActivity(), peopleArray,
                new ConversationCreateUtils.OnCreateGroupConversationListener() {

                    @Override
                    public void createGroupConversationSuccess(Conversation conversation) {
                        mView.dismissLoading();
                        mView.createGroupSuccess(conversation);
                    }

                    @Override
                    public void createGroupConversationFail() {
                        mView.dismissLoading();
                        ToastUtils.show(R.string.chat_conversation_create_error);
                    }
                });
    }

    @Override
    public int getConversationRealMemberSize() {
        List<String> memberUidList = mConversation.getMemberList();
        int memberSize = ContactUserCacheUtils.getContactUserListById(memberUidList).size();
        return memberSize;
    }

    @Override
    public void quitGroupChannel() {
        final String conversationId = mConversation.getId();
        String completeUrl = ApiUrl.getQuitChannelGroupUrl(mConversation.getId());
        ApiServiceImpl.getInstance().quitGroupChannel(new BaseModuleAPICallback(mView.getContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                mView.dismissLoading();
                mView.quitGroupSuccess();
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
                        quitGroupChannel();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                ApiServiceImpl.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        }, conversationId);
    }

    @Override
    public void delChannel() {
        final String conversationId = mConversation.getId();
        String completeUrl = ApiUrl.getDeleteChannelUrl(conversationId);
        ApiServiceImpl.getInstance().delChannel(new BaseModuleAPICallback(mView.getContext(), completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                mView.dismissLoading();
                mView.deleteGroupSuccess();
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
                        delChannel();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                ApiServiceImpl.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        }, conversationId);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            switch (requestCode) {
                case QEQUEST_ADD_MEMBER:
                    ArrayList<String> addUidList = new ArrayList<>();
                    List<SearchModel> addMemberList = (List<SearchModel>) data
                            .getSerializableExtra("selectMemList");
                    if (addMemberList.size() > 0) {
                        for (int i = 0; i < addMemberList.size(); i++) {
                            addUidList.add(addMemberList.get(i).getId());
                        }
                        mView.showLoading();
                        if (mConversation.getType().equals(Conversation.TYPE_GROUP)) {
                            addConversationMembers(addUidList, mConversation.getId());
                        } else if (mConversation.getType().equals(Conversation.TYPE_DIRECT)) {
                            createGroup(addMemberList);
                        }
                    }
                    break;
                case QEQUEST_DEL_MEMBER:
                    ArrayList<String> delUidList = (ArrayList<String>) data.getSerializableExtra("selectMemList");
                    if (delUidList.size() > 0) {
                        mView.showLoading();
                        delConversationMembers(delUidList, mConversation.getId());
                    }
                    break;
                case QEQUEST_FILE_TRANSFER:
                    mView.activityFinish();
                    break;
                default:
                    break;
            }
        }

    }
}
