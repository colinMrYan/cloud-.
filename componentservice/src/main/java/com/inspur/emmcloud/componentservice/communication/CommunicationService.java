package com.inspur.emmcloud.componentservice.communication;

import android.content.Context;

import android.app.Activity;

import com.inspur.emmcloud.componentservice.CoreService;
import com.inspur.emmcloud.componentservice.contact.ContactUser;

import org.json.JSONArray;

import java.util.List;

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

    //打开网络检测
    void startNetWorkStateActivity(Activity activity);

    String getMyAppFragmentHeaderText(String simpleName);

    String getUserIconUrl(ContactUser uid);

    ContactUser getContactUser(String email);

    Class getContactSearchActivity();

    List<ContactUser> getContantUserList(List<String> uidList);
}
