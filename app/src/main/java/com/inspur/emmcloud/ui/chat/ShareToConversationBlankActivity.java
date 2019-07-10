package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.Channel;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.componentservice.communication.ShareToConversationListener;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelCacheUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by chenmch on 2019/7/10.
 */

public class ShareToConversationBlankActivity extends BaseActivity {
    private static final int REQUEST_SELECT_CONTACT = 1;
    private static ShareToConversationListener listener;

    public static void startActivity(Context context, Intent intent, ShareToConversationListener listener) {
        ShareToConversationBlankActivity.listener = listener;
        context.startActivity(intent);

    }

    @Override
    public void onCreate() {
        selectContact();
//        String type = getIntent().getStringExtra("type");
//        switch (type){
//            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
//                shareExtendedLinks();
//                break;
//        }
    }

    @Override
    public int getLayoutResId() {
        return 0;
    }

    @Override
    public int getStatusType() {
        return STATUS_TRANSPARENT;
    }

    private void selectContact() {
        Bundle bundle = new Bundle();
        bundle.putInt("select_content", 0);
        bundle.putBoolean(ContactSearchFragment.EXTRA_MULTI_SELECT, false);
        ArrayList<String> uidList = new ArrayList<>();
        uidList.add(BaseApplication.getInstance().getUid());
        bundle.putString(ContactSearchFragment.EXTRA_TITLE, "分享到");
        ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_MEMBER).with(bundle).navigation(this, REQUEST_SELECT_CONTACT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_CONTACT && resultCode == RESULT_OK) {
            String result = data.getStringExtra("searchResult");
            JSONObject jsonObject = JSONUtils.getJSONObject(result);
            if (jsonObject.has("people")) {
                JSONArray peopleArray = JSONUtils.getJSONArray(jsonObject, "people", new JSONArray());
                if (peopleArray.length() > 0) {
                    JSONObject peopleObj = JSONUtils.getJSONObject(peopleArray, 0, new JSONObject());
                    String uid = JSONUtils.getString(peopleObj, "pid", "");
                    if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                        if (!NetUtils.isNetworkConnected(BaseApplication.getInstance(), false)) {
                            return;
                        }
                        Channel channel = ChannelCacheUtils.getDirectChannelToUser(BaseApplication.getInstance(), uid);
                        if (channel == null) {
                            createDirectChannel(uid);
                        } else {
                            sendMessage(channel.getCid());
                        }

                    } else if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
                        Conversation conversation = ConversationCacheUtils.getDirectConversationToUser(BaseApplication.getInstance(), uid);
                        if (conversation == null) {
                            createDirectChannel(uid);
                        } else {
                            sendMessage(conversation.getId());
                        }
                    }


                }
            }
            if (jsonObject.has("channelGroup")) {
                JSONArray channelGroupArray = JSONUtils.getJSONArray(jsonObject, "channelGroup", new JSONArray());
                if (channelGroupArray.length() > 0) {
                    JSONObject cidObj = JSONUtils.getJSONObject(channelGroupArray, 0, new JSONObject());
                    String cid = JSONUtils.getString(cidObj, "cid", "");
                    sendMessage(cid);
                }
            }
        } else {
            callbackCancel();
        }
    }


    /**
     * 创建单聊
     *
     * @param uid
     */
    private void createDirectChannel(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(ShareToConversationBlankActivity.this, uid,
                    new ConversationCreateUtils.OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            sendMessage(conversation.getId());
                        }

                        @Override
                        public void createDirectConversationFail() {
                            callbackFail();

                        }
                    }, false);
        } else {
            new ChatCreateUtils().createDirectChannel(ShareToConversationBlankActivity.this, uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            sendMessage(getCreateSingleChannelResult.getCid());
                        }

                        @Override
                        public void createDirectChannelFail() {
                            callbackFail();
                        }
                    });
        }

    }


    /**
     * 发送消息
     *
     * @param cid
     */
    private void sendMessage(String cid) {
//        if (NetUtils.isNetworkConnected(getApplicationContext())) {
//            if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
//                ChatAPIService apiService = new ChatAPIService(
//                        NewsWebDetailActivity.this);
//                apiService.setAPIInterface(new WebService());
//                JSONObject jsonObject = new JSONObject();
//                try {
//                    jsonObject.put("url", url);
//                    jsonObject.put("poster", groupNews.getPoster());
//                    jsonObject.put("digest", groupNews.getSummary());
//                    jsonObject.put("title", groupNews.getTitle());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                apiService.sendMsg(cid, jsonObject.toString(), "res_link", System.currentTimeMillis() + "");
//            } else {
//                String poster = StringUtils.isBlank(groupNews.getPoster()) ? "" : NewsAPIUri.getPreviewUrl(groupNews.getPoster());
//                Message message = CommunicationUtils.combinLocalExtendedLinksMessage(cid, poster, groupNews.getTitle(), groupNews.getSummary(), url);
//                fakeMessageId = message.getId();
//                WSAPIService.getInstance().sendChatExtendedLinksMsg(message);
//            }
//
//        }

    }

    private void callbackSuccess(String cid) {
        if (listener != null) {
            listener.shareSuccess(cid);
        }
        finish();
        listener = null;
    }

    private void callbackFail() {
        if (listener != null) {
            listener.shareFail();
        }
        finish();
        listener = null;
    }

    private void callbackCancel() {
        if (listener != null) {
            listener.shareCancel();
        }
        finish();
        listener = null;
    }
}
