package com.inspur.emmcloud.componentservice.communication;

import com.inspur.emmcloud.componentservice.CoreService;

/**
 * Created by chenmch on 2019/5/31.
 */

public interface CommunicationService extends CoreService {
    void startWebSocket(boolean isForceReconnect);

    void webSocketSignout();

    void closeWebsocket();

    void sendAppStatus();

    void stopPush();

    boolean isSocketConnect();
}
