package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.componentservice.communication.ShareToConversationListener;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

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
        bundle.putString(ContactSearchFragment.EXTRA_TITLE, getString(R.string.baselib_share_to));
        ARouter.getInstance().build(Constant.AROUTER_CLASS_CONTACT_SEARCH).with(bundle).navigation(this, REQUEST_SELECT_CONTACT);
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
                    Conversation conversation = ConversationCacheUtils.getDirectConversationToUser(BaseApplication.getInstance(), uid);
                    if (conversation == null) {
                        createDirectChannel(uid);
                    } else {
                        sendMessage(conversation.getId());
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
    }


    /**
     * 发送消息
     *
     * @param cid
     */
    private void sendMessage(String cid) {
        String type = getIntent().getStringExtra("type");
        switch (type) {
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                break;
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
                sendTxtPlainMessage(cid);
                break;
        }
    }

    private void sendTxtPlainMessage(String cid) {
        String content = getIntent().getStringExtra("content");
        Message message = CommunicationUtils.combinLocalTextPlainMessage(content, cid, new HashMap<String, String>());
        message.setSendStatus(Message.MESSAGE_SEND_ING);
        MessageCacheUtil.saveMessage(ShareToConversationBlankActivity.this, message);
        WSAPIService.getInstance().sendChatTextPlainMsg(message);
        // 通知沟通页面更新列表状态
        Intent intent = new Intent("message_notify");
        intent.putExtra("command", "sort_session_list");
        LocalBroadcastManager.getInstance(ShareToConversationBlankActivity.this).sendBroadcast(intent);
        callbackSuccess(cid);
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
