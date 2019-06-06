package com.inspur.emmcloud.login.communication;

import com.inspur.emmcloud.login.CoreService;

/**
 * Created by chenmch on 2019/5/31.
 */

public interface CommunicationService extends CoreService {
    void startWebSocket();

    void webSocketSignout();

    void closeWebsocket();

    void sendAppStatus();

    void stopPush();

    boolean isSocketConnect();
}
