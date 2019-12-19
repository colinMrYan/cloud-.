package com.inspur.emmcloud.componentservice.communication;

import android.content.Context;

import com.inspur.emmcloud.componentservice.CoreService;

import org.json.JSONArray;

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

    void openConversationByChannelId(String cid);

    void MessageSendManagerOnDestroy();

    void sendVoiceCommunicationNotify();

    //退出登录停止语音通话
    void stopVoiceCommunication();

    void createGroupConversation(Context context, JSONArray peopleArray, String groupName,
                                 OnCreateGroupConversationListener onCreateGroupConversationListener);

    String getShowName(Conversation conversation);
}
