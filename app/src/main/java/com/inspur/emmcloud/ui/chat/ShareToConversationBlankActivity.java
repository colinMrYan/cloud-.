package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.GetSendMsgResult;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.componentservice.communication.ShareToConversationListener;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
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
    private LoadingDialog loadingDlg;
    private String cid = "";

    public static void startActivity(Context context, Intent intent, ShareToConversationListener listener) {
        ShareToConversationBlankActivity.listener = listener;
        context.startActivity(intent);

    }

    @Override
    public void onCreate() {
        loadingDlg = new LoadingDialog(this);
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
        bundle.putStringArrayList(ContactSearchFragment.EXTRA_EXCLUDE_SELECT, uidList);
        String type = getIntent().getStringExtra("type");
        switch (type) {
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                String title = getIntent().getStringExtra("title");
                title = getString(R.string.baselib_share_link) + " " + title;
                bundle.putString("show_sure_dialog", "sure");
                bundle.putString("show_sure_dialog_with_message", title);
                break;
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
                String content = getIntent().getStringExtra("content");
                bundle.putString("show_sure_dialog", "sure");
                bundle.putString("show_sure_dialog_with_message", content);
                break;
        }
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
                        cid = conversation.getId();
                        sendMessage();
                    }

                }
            }
            if (jsonObject.has("channelGroup")) {
                JSONArray channelGroupArray = JSONUtils.getJSONArray(jsonObject, "channelGroup", new JSONArray());
                if (channelGroupArray.length() > 0) {
                    JSONObject cidObj = JSONUtils.getJSONObject(channelGroupArray, 0, new JSONObject());
                    cid = JSONUtils.getString(cidObj, "cid", "");
                    sendMessage();
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
                            cid = conversation.getId();
                            sendMessage();
                        }

                        @Override
                        public void createDirectConversationFail() {
                            callbackFail();

                        }
                    }, false);
        } else if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            new ChatCreateUtils().createDirectChannel(ShareToConversationBlankActivity.this, uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            cid = getCreateSingleChannelResult.getCid();
                            sendMessage();
                        }

                        @Override
                        public void createDirectChannelFail() {
                        }
                    });
        }

    }


    /**
     * 发送消息
     *
     */
    private void sendMessage() {
        String type = getIntent().getStringExtra("type");
        switch (type) {
            case Message.MESSAGE_TYPE_EXTENDED_LINKS:
                sendExtendedLinksMessage();
                break;
            case Message.MESSAGE_TYPE_TEXT_PLAIN:
                sendTxtPlainMessage();
                break;
        }
    }

    private void sendTxtPlainMessage() {
        String content = getIntent().getStringExtra("content");
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            Message message = CommunicationUtils.combinLocalTextPlainMessage(content, cid, new HashMap<String, String>());
            message.setSendStatus(Message.MESSAGE_SEND_ING);
            MessageCacheUtil.saveMessage(ShareToConversationBlankActivity.this, message);
            WSAPIService.getInstance().sendChatTextPlainMsg(message);
            notifyMessageDataChanged();
            callbackSuccess();
        } else if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
                ChatAPIService apiService = new ChatAPIService(ShareToConversationBlankActivity.this);
                apiService.setAPIInterface(new WebService());
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("source", content);
                    jsonObject.put("mentions", new JSONArray());
                    jsonObject.put("urls", new JSONArray());
                    jsonObject.put("tmpId", AppUtils.getMyUUID(MyApplication.getInstance()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                apiService.sendMsg(cid, jsonObject.toString(), "txt_rich", System.currentTimeMillis() + "");
            } else {
                callbackFail();
            }
        }

    }

    private void sendExtendedLinksMessage() {
        String poster = getIntent().getStringExtra("poster");
        String title = getIntent().getStringExtra("title");
        String subTitle = getIntent().getStringExtra("subTitle");
        String url = getIntent().getStringExtra("url");
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            Message message = CommunicationUtils.combinLocalExtendedLinksMessage(cid, poster, title, subTitle, url);
            message.setSendStatus(Message.MESSAGE_SEND_ING);
            MessageCacheUtil.saveMessage(ShareToConversationBlankActivity.this, message);
            WSAPIService.getInstance().sendChatExtendedLinksMsg(message);
            notifyMessageDataChanged();
            callbackSuccess();
        } else if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
                ChatAPIService apiService = new ChatAPIService(ShareToConversationBlankActivity.this);
                apiService.setAPIInterface(new WebService());
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("url", url);
                    jsonObject.put("poster", poster);
                    jsonObject.put("digest", subTitle);
                    jsonObject.put("res_link", title);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                apiService.sendMsg(cid, jsonObject.toString(), "txt_rich", System.currentTimeMillis() + "");
            } else {
                callbackFail();
            }
        }


    }

    private void notifyMessageDataChanged() {
        // 通知沟通页面更新列表状态
        Intent intent = new Intent("message_notify");
        intent.putExtra("command", "sort_session_list");
        LocalBroadcastManager.getInstance(ShareToConversationBlankActivity.this).sendBroadcast(intent);
    }

    private void callbackSuccess() {
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

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnSendMsgSuccess(GetSendMsgResult getSendMsgResult,
                                         String fakeMessageId) {
            LoadingDialog.dimissDlg(loadingDlg);
            callbackSuccess();
        }

        @Override
        public void returnSendMsgFail(String error, String fakeMessageId, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            callbackFail();
        }
    }
}
