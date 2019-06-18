package com.inspur.emmcloud.servcieimpl;

import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.push.WebSocketPush;

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
    public void closeWebsocket() {
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
}
