package com.inspur.emmcloud.servcieimpl;

import android.content.Intent;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.componentservice.communication.ShareToConversationListener;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.ui.chat.ShareToConversationBlankActivity;

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
}
