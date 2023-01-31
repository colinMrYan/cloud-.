package com.inspur.emmcloud.ui.chat.mvp.presenter;

import android.text.TextUtils;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.mvp.BasePresenter;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.UIConversation;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.ui.chat.mvp.contract.ConversionSearchContract;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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

    @Override
    public void getTransmitConversationData() {
        // 外部分享时存在聊天列表更新不及时的问题，导致"lastUpdate"有误，因此每次给conversation设置lastUpdate
        // 服务端conversation的lastUpdate字段不代表conversation最新时间lastUpdate，需要结合conversation的最新message时间
        Observable.create(new ObservableOnSubscribe<List<Conversation>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<Conversation>> emitter) throws Exception {
                        List<Conversation> conversationList = ConversationCacheUtils.getConversationList(MyApplication.getInstance());
                        if (conversationList.size() > 0) {
                            for (Conversation conversation : conversationList) {
                                String id = conversation.getId();
                                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
                                long lastUpdate;
                                if (messageList.size() == 0) {
                                    lastUpdate = conversation.getCreationDate();
                                } else {
                                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
                                }
                                conversation.setLastUpdate(lastUpdate);
                            }
                            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
                        }

                        List<Conversation> list = ConversationCacheUtils.getConversationListByLastUpdate(mView.getContext());
                        Iterator<Conversation> iterator = list.iterator();
                        List<Conversation> stickConversationList = new ArrayList<>();
                        String myUid = BaseApplication.getInstance().getUid();
                        while (iterator.hasNext()) {
                            Conversation conversation = iterator.next();
                            if (conversation.isSilent() && !TextUtils.equals(myUid, conversation.getOwner()) &&
                                    !conversation.getAdministratorList().contains(myUid)) {
                                iterator.remove();
                                continue;
                            }
                            // 移除已解散的群组
                            if ("REMOVED".equals(conversation.getState())) {
                                iterator.remove();
                                continue;
                            }
                            // 隐藏的聊天，且未读数为0
                            if (conversation.isHide()) {
                                long channelMessageUnreadCount = MessageCacheUtil.getChannelMessageUnreadCount(MyApplication.getInstance(), conversation.getId());
                                if (channelMessageUnreadCount == 0){
                                    iterator.remove();
                                    continue;
                                }
                            }
                            if (conversation.isStick()) {
                                stickConversationList.add(conversation);
                                iterator.remove();
                            }

                        }
                        list.addAll(0, stickConversationList);
                        emitter.onNext(list);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Conversation>>() {
                    @Override
                    public void accept(List<Conversation> conversationList) throws Exception {
                        mView.showConversationData(conversationList);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
//        List<Conversation> conversationList = ConversationCacheUtils.getConversationList(MyApplication.getInstance());
//        if (conversationList.size() > 0) {
//            for (Conversation conversation : conversationList) {
//                String id = conversation.getId();
//                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
//                long lastUpdate;
//                if (messageList.size() == 0) {
//                    lastUpdate = conversation.getCreationDate();
//                } else {
//                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
//                }
//                conversation.setLastUpdate(lastUpdate);
//            }
//            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
//        }
//        if (conversationList.size() > 0) {
//            for (Conversation conversation : conversationList) {
//                String id = conversation.getId();
//                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
//                long lastUpdate;
//                if (messageList.size() == 0) {
//                    lastUpdate = conversation.getCreationDate();
//                } else {
//                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
//                }
//                conversation.setLastUpdate(lastUpdate);
//            }
//            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
//        }
//        if (conversationList.size() > 0) {
//            for (Conversation conversation : conversationList) {
//                String id = conversation.getId();
//                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
//                long lastUpdate;
//                if (messageList.size() == 0) {
//                    lastUpdate = conversation.getCreationDate();
//                } else {
//                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
//                }
//                conversation.setLastUpdate(lastUpdate);
//            }
//            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
//        }
//        if (conversationList.size() > 0) {
//            for (Conversation conversation : conversationList) {
//                String id = conversation.getId();
//                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
//                long lastUpdate;
//                if (messageList.size() == 0) {
//                    lastUpdate = conversation.getCreationDate();
//                } else {
//                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
//                }
//                conversation.setLastUpdate(lastUpdate);
//            }
//            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
//        }
//        if (conversationList.size() > 0) {
//            for (Conversation conversation : conversationList) {
//                String id = conversation.getId();
//                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
//                long lastUpdate;
//                if (messageList.size() == 0) {
//                    lastUpdate = conversation.getCreationDate();
//                } else {
//                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
//                }
//                conversation.setLastUpdate(lastUpdate);
//            }
//            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
//        }
//        if (conversationList.size() > 0) {
//            for (Conversation conversation : conversationList) {
//                String id = conversation.getId();
//                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
//                long lastUpdate;
//                if (messageList.size() == 0) {
//                    lastUpdate = conversation.getCreationDate();
//                } else {
//                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
//                }
//                conversation.setLastUpdate(lastUpdate);
//            }
//            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
//        }
//        if (conversationList.size() > 0) {
//            for (Conversation conversation : conversationList) {
//                String id = conversation.getId();
//                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
//                long lastUpdate;
//                if (messageList.size() == 0) {
//                    lastUpdate = conversation.getCreationDate();
//                } else {
//                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
//                }
//                conversation.setLastUpdate(lastUpdate);
//            }
//            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
//        }
//        if (conversationList.size() > 0) {
//            for (Conversation conversation : conversationList) {
//                String id = conversation.getId();
//                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
//                long lastUpdate;
//                if (messageList.size() == 0) {
//                    lastUpdate = conversation.getCreationDate();
//                } else {
//                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
//                }
//                conversation.setLastUpdate(lastUpdate);
//            }
//            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
//        }
//        if (conversationList.size() > 0) {
//            for (Conversation conversation : conversationList) {
//                String id = conversation.getId();
//                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
//                long lastUpdate;
//                if (messageList.size() == 0) {
//                    lastUpdate = conversation.getCreationDate();
//                } else {
//                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
//                }
//                conversation.setLastUpdate(lastUpdate);
//            }
//            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
//        }
//        if (conversationList.size() > 0) {
//            for (Conversation conversation : conversationList) {
//                String id = conversation.getId();
//                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
//                long lastUpdate;
//                if (messageList.size() == 0) {
//                    lastUpdate = conversation.getCreationDate();
//                } else {
//                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
//                }
//                conversation.setLastUpdate(lastUpdate);
//            }
//            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
//        }
//        if (conversationList.size() > 0) {
//            for (Conversation conversation : conversationList) {
//                String id = conversation.getId();
//                List<Message> messageList = MessageCacheUtil.getHistoryMessageListIncludeEditingMessage(MyApplication.getInstance(), id, null, 15);
//                long lastUpdate;
//                if (messageList.size() == 0) {
//                    lastUpdate = conversation.getCreationDate();
//                } else {
//                    lastUpdate = messageList.get(messageList.size() - 1).getCreationDate();
//                }
//                conversation.setLastUpdate(lastUpdate);
//            }
//            ConversationCacheUtils.updateConversationList(MyApplication.getInstance(), conversationList, "lastUpdate");
//        }
//
//        List<Conversation> list = ConversationCacheUtils.getConversationListByLastUpdate(mView.getContext());
//        List<Conversation> testList = new ArrayList<>(list);
//        testList.addAll(list);
//        testList.addAll(list);
//        Iterator<Conversation> iterator = testList.iterator();
//        List<Conversation> stickConversationList = new ArrayList<>();
//        String myUid = BaseApplication.getInstance().getUid();
//        while (iterator.hasNext()) {
//            Conversation conversation = iterator.next();
//            if (conversation.isSilent() && !TextUtils.equals(myUid, conversation.getOwner()) &&
//                    !conversation.getAdministratorList().contains(myUid)) {
//                iterator.remove();
//                continue;
//            }
//            // 移除已解散的群组
//            if ("REMOVED".equals(conversation.getState())) {
//                iterator.remove();
//                continue;
//            }
//            // 隐藏的聊天，且未读数为0
//            if (conversation.isHide()) {
//                long channelMessageUnreadCount = MessageCacheUtil.getChannelMessageUnreadCount(MyApplication.getInstance(), conversation.getId());
//                if (channelMessageUnreadCount == 0){
//                    iterator.remove();
//                    continue;
//                }
//            }
//            if (conversation.isStick()) {
//                stickConversationList.add(conversation);
//                iterator.remove();
//            }
//
//        }
//        testList.addAll(0, stickConversationList);
//        mView.showConversationData(testList);
//        return testList;
    }
}
