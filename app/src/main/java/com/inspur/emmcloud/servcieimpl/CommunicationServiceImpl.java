package com.inspur.emmcloud.servcieimpl;

import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.privates.PushManagerUtils;

/**
 * Created by chenmch on 2019/6/3.
 */

public class CommunicationServiceImpl implements CommunicationService {
    @Override
    public void startWebSocket() {
        WebSocketPush.getInstance().startWebSocket();
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
}
