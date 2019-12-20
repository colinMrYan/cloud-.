package com.inspur.emmcloud.servcieimpl;

import android.app.Activity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.componentservice.communication.OnCreateGroupConversationListener;
import com.inspur.emmcloud.componentservice.communication.ShareToConversationListener;
import com.inspur.emmcloud.componentservice.contact.ContactUser;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.chat.ConversationActivity;
import com.inspur.emmcloud.ui.chat.ConversationBaseActivity;
import com.inspur.emmcloud.ui.chat.ShareToConversationBlankActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.mine.setting.NetWorkStateDetailActivity;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.util.privates.CommunicationUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.MessageSendManager;
import com.inspur.emmcloud.util.privates.NotifyUtil;
import com.inspur.emmcloud.util.privates.VoiceCommunicationManager;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by chenmch on 2019/6/3.
 */

public class CommunicationServiceImpl implements CommunicationService {
    @Override
    public void startWebSocket(boolean isForceReconnect) {
        WebSocketPush.getInstance().startWebSocket(isForceReconnect);
    }

    @Override
    public void webSocketSignout() {
        WebSocketPush.getInstance().webSocketSignout();
    }

    @Override
    public void sendAppStatus() {
        WebSocketPush.getInstance().sendAppStatus();
    }

    @Override
    public void closeWebSocket() {
        WebSocketPush.getInstance().closeWebsocket();
    }

    @Override
    public void stopPush() {
        PushManagerUtils.getInstance().stopPush();
    }

    @Override
    public boolean isSocketConnect() {
        return WebSocketPush.getInstance().isSocketConnect();
    }

    @Override
    public void shareExtendedLinksToConversation(String poster, String title, String subTitle, String url, ShareToConversationListener listener) {
        Intent intent = new Intent();
        intent.putExtra("type", Message.MESSAGE_TYPE_EXTENDED_LINKS);
        intent.putExtra("poster", poster);
        intent.putExtra("title", title);
        intent.putExtra("subTitle", subTitle);
        intent.putExtra("url", url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(BaseApplication.getInstance(), ShareToConversationBlankActivity.class);
        ShareToConversationBlankActivity.startActivity(BaseApplication.getInstance(), intent, listener);
    }

    @Override
    public void shareTxtPlainToConversation(String content, ShareToConversationListener listener) {
        Intent intent = new Intent();
        intent.putExtra("type", Message.MESSAGE_TYPE_TEXT_PLAIN);
        intent.putExtra("content", content);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(BaseApplication.getInstance(), ShareToConversationBlankActivity.class);
        ShareToConversationBlankActivity.startActivity(BaseApplication.getInstance(), intent, listener);
    }

    /**
     * 扫码后接收来自网页的插件调用
     *
     * @param cid
     */
    @Override
    public void openConversationByChannelId(String cid) {
        try {
            Intent intent = new Intent();
            intent.putExtra(ConversationBaseActivity.EXTRA_CID, cid);
            intent.putExtra(ConversationActivity.EXTRA_NEED_GET_NEW_MESSAGE, true);
            intent.putExtra(ConversationActivity.EXTRA_COME_FROM_SCANCODE, true);
            intent.setClass(BaseApplication.getInstance(), ConversationActivity.class);
            BaseApplication.getInstance().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void MessageSendManagerOnDestroy() {
        MessageSendManager.getInstance().onDestroy();
    }

    @Override
    public void sendVoiceCommunicationNotify() {
        NotifyUtil.sendNotifyMsg(BaseApplication.getInstance());
    }

    @Override
    public void stopVoiceCommunication() {
        if (VoiceCommunicationManager.getInstance().isVoiceBusy()) {
            VoiceCommunicationManager.getInstance().handleDestroy();
        }
    }

    @Override
    public void createGroupConversation(Context context, JSONArray peopleArray, String groupName, OnCreateGroupConversationListener onCreateGroupConversationListener) {
        new ConversationCreateUtils().createGroupConversation((Activity) context, peopleArray, groupName, onCreateGroupConversationListener);
    }


    @Override
    public String getShowName(Conversation conversation) {
        if (conversation != null) {
            return CommunicationUtils.getConversationTitle(conversation);
        } else {
            return "";
        }
    }

    @Override
    public void startNetWorkStateActivity(Activity activity) {
        IntentUtils.startActivity(activity, NetWorkStateDetailActivity.class);
    }

    @Override
    public String getMyAppFragmentHeaderText(String simpleName) {
        return AppTabUtils.getTabTitle(BaseApplication.getInstance(), simpleName);
    }

    @Override
    public String getUserIconUrl(ContactUser contactUser) {
        return APIUri.getUserIconUrl(BaseApplication.getInstance(), contactUser.getId());
    }

    @Override
    public ContactUser getContactUser(String email) {
        return ContactUserCacheUtils.getContactUserByEmail(email);
    }

    @Override
    public Class getContactSearchActivity() {
        return ContactSearchActivity.class;
    }

    @Override
    public List<ContactUser> getContantUserList(List<String> uidList) {
        return ContactUserCacheUtils.getSoreUserList(uidList);
    }
}
