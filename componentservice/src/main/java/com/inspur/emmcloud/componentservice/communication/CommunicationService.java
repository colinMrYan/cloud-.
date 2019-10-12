package com.inspur.emmcloud.componentservice.communication;

import com.inspur.emmcloud.componentservice.CoreService;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/5/31.
 */

public interface CommunicationService extends CoreService {
    void startWebSocket(boolean isForceReconnect);

    void webSocketSignout();

    void closeWebSocket();

    void sendAppStatus();

    void stopPush();

    boolean isSocketConnect();

    void shareExtendedLinksToConversation(String poster, String title, String subTitle, String url, ShareToConversationListener listener);

    void shareTxtPlainToConversation(String content, ShareToConversationListener listener);

    void openConversationByChannelId(JSONObject jsonObject);
}
