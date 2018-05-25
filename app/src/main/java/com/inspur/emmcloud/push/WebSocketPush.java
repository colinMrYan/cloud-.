package com.inspur.emmcloud.push;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.bean.chat.WSPushContent;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.PushInfoUtils;

import org.greenrobot.eventbus.EventBus;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;
import io.socket.engineio.client.transports.WebSocket;
import io.socket.parseqs.ParseQS;

public class WebSocketPush {

    private static final String TAG = "WebSocketPush";
    private static WebSocketPush webSocketPush = null;
    private Socket mSocket = null;
    private Map<String, EventMessage> tracerMap = new HashMap<>();

    public WebSocketPush() {
        tracerMap = new HashMap<>();
    }

    public static WebSocketPush getInstance() {
        if (webSocketPush == null) {
            synchronized (WebSocketPush.class) {
                if (webSocketPush == null) {
                    webSocketPush = new WebSocketPush();
                }
            }
        }
        return webSocketPush;
    }

    /**
     * 开始WebSocket推送
     */
    public void init(boolean isForceNew) {
        // TODO Auto-generated method stub
        if (isForceNew || !isSocketConnect()) {
            if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
                new PushInfoUtils(MyApplication.getInstance(), new PushInfoUtils.OnGetChatClientIdListener() {
                    @Override
                    public void getChatClientIdSuccess(String chatClientId) {
                        WebSocketConnect(chatClientId);
                    }

                    @Override
                    public void getChatClientIdFail() {
                        sendWebSocketStatusBroadcaset(Socket.EVENT_DISCONNECT);
                    }
                }).getChatClientId(isForceNew);
            } else {
                sendWebSocketStatusBroadcaset(Socket.EVENT_DISCONNECT);
            }
        }
    }


    private void WebSocketConnect(String chatClientId) {
        String url = APIUri.getWebsocketConnectUrl();
        String path = MyApplication.getInstance().isChatVersionV0() ? "/" + MyApplication.getInstance().getCurrentEnterprise().getCode() + "/socket/handshake" :
                "/chat/socket/handshake";
        sendWebSocketStatusBroadcaset("socket_connecting");
        IO.Options opts = new IO.Options();
        opts.reconnectionAttempts = 5; // 设置websocket重连次数
        opts.forceNew = true;
        Map<String, String> query = new HashMap<String, String>();
        if (MyApplication.getInstance().isChatVersionV0()) {
            String uuid = AppUtils.getMyUUID(MyApplication.getInstance());
            String deviceName = AppUtils.getDeviceName(MyApplication.getInstance());
            String pushId = AppUtils.getPushId(MyApplication.getInstance());
            query.put("device.id", uuid);
            query.put("device.name", deviceName);
            query.put("device.push", pushId);
        } else {
            query.put("client", chatClientId);
            query.put("enterprise",MyApplication.getInstance().getCurrentEnterprise().getId());
        }
        opts.path = path;
        LogUtils.debug(TAG, "query.toString()=" + ParseQS.encode(query));
        opts.query = ParseQS.encode(query);
        opts.transports = new String[] { WebSocket.NAME};
        try {
            webSocketSignout();
            Manager manager = new Manager(new URI(url), opts);
            mSocket = manager.socket(MyApplication.getInstance().getChatSocketNameSpace());
            mSocket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    Transport transport = (Transport) args[0];
                    transport.on(Transport.EVENT_REQUEST_HEADERS,
                            new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, List<String>> headers = (Map<String, List<String>>) args[0];
                                    if (MyApplication.getInstance().getToken() != null){
                                        headers.put("Authorization", Arrays.asList(MyApplication.getInstance().getToken()));
                                    }
                                }
                            }).on(Transport.EVENT_RESPONSE_HEADERS,
                            new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                }
                            });
                }
            });
            addListeners();
            mSocket.open();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sendWebSocketStatusBroadcaset(Socket.EVENT_CONNECT_ERROR);
            Log.d(TAG, "e=" + e.toString());
            if (mSocket != null) {
                mSocket.disconnect();
                mSocket.off();
                mSocket.close();
                mSocket = null;
            }
        }
    }


    /**
     * 判断websocket是否已连接
     *
     * @return
     */
    public boolean isSocketConnect() {
        if (mSocket != null) {
            return mSocket.connected();
        }
        return false;

    }

    public void sendAppStatus(boolean isActive) {
        if (isSocketConnect()) {
            if (MyApplication.getInstance().isChatVersionV0()) {
                String appStatus = isActive ? "ACTIVED" : "FROZEN";
                LogUtils.debug(TAG, "发送App状态：" + appStatus);
                mSocket.emit("state", appStatus);
            } else {
                WSAPIService.getInstance().sendAppStatus(isActive ? "ACTIVED" : "SUSPEND");
                LogUtils.debug(TAG, "发送App状态：" + (isActive ? "ACTIVED" : "SUSPEND"));
            }

        }
    }

    public void webSocketSignout() {
        if (mSocket != null) {
            if (isSocketConnect()) {
                LogUtils.debug(TAG, "注销");
                if (MyApplication.getInstance().isChatVersionV0()) {
                    mSocket.emit("state", "SIGNOUT");
                } else {
                    WSAPIService.getInstance().sendAppStatus("REMOVED");
                }
            }
        }
        closeWebsocket();
    }

    /**
     * 切换租户的时候直接断开Websocket
     */
    public void closeWebsocket() {
        if (mSocket != null) {
            mSocket.disconnect();
            removeListeners();
            mSocket.close();
            mSocket = null;
        }
        if (tracerMap != null) {
            tracerMap.clear();
        }
    }

    private void removeListeners() {
        mSocket.off("message");
        mSocket.off("com.inspur.ecm.chat");
        mSocket.off(Socket.EVENT_CONNECT_ERROR);
        mSocket.off(Socket.EVENT_CONNECT);
        mSocket.off(Socket.EVENT_DISCONNECT);
    }

    private void addListeners() {

        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... arg0) {
                // TODO Auto-generated method stub
                LogUtils.debug(TAG, "连接失败");
                if (arg0[0] != null){
                    try {
                        ((Exception) arg0[0]).printStackTrace();
                        LogUtils.debug(TAG, "arg0[0]=="+arg0[0].toString());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                sendWebSocketStatusBroadcaset(Socket.EVENT_CONNECT_ERROR);

            }
        });
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... arg0) {
                // TODO Auto-generated method stub
                if (MyApplication.getInstance().isChatVersionV0()) {
                    LogUtils.debug(TAG, "连接成功");
                    sendWebSocketStatusBroadcaset(Socket.EVENT_CONNECT);
                    // 当第一次连接成功后发送App目前的状态消息
                    sendAppStatus(MyApplication.getInstance().getIsActive());
                }
            }
        });

        mSocket.on("status", new Emitter.Listener() {

            @Override
            public void call(Object... arg0) {
                // TODO Auto-generated method stub
                LogUtils.debug(TAG, "连接成功");
                int code = JSONUtils.getInt(arg0[0].toString(), "code", 0);
                if (code == 100) {
                    sendWebSocketStatusBroadcaset(Socket.EVENT_CONNECT);
                    // 当第一次连接成功后发送App目前的状态消息
                    sendAppStatus(MyApplication.getInstance().getIsActive());
                }
            }
        });


        mSocket.on("message", new Emitter.Listener() {

            @Override
            public void call(Object... arg0) {
                // TODO Auto-generated method stub
                LogUtils.debug(TAG, "message:" + arg0[0].toString());

                String content = arg0[0].toString();
                Intent intent = new Intent("com.inspur.msg");
                intent.putExtra("push", content);
                LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
            }
        });

        mSocket.on("com.inspur.ecm.chat", new Emitter.Listener() {

            @Override
            public void call(Object... arg0) {
                // TODO Auto-generated method stub
                LogUtils.debug(TAG, "arg0[0].toString()=" + arg0[0].toString());
                WSPushContent wsPushContent = new WSPushContent(arg0[0].toString());
                String path = wsPushContent.getPath();
                //客户端主动请求
                //	if (StringUtils.isBlank(path)){
                String tracer = wsPushContent.getTracer();
                EventMessage eventMessage = tracerMap.get(tracer);
                if (eventMessage != null) {
                    tracerMap.remove(tracer);
                    String body = wsPushContent.getBody();
                    eventMessage.setContent(body);
                    eventMessage.setStatus(wsPushContent.getStatus());
                    EventBus.getDefault().post(eventMessage);
                }
                //		}
                else {
                    if (path.equals("/channel/message") && wsPushContent.getMethod().equals("post")) {
                        EventMessage eventMessagea = new EventMessage(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, wsPushContent.getBody());
                        EventBus.getDefault().post(eventMessagea);
                    }
                }
            }
        });
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... arg0) {
                // TODO Auto-generated method stub
                sendWebSocketStatusBroadcaset(Socket.EVENT_DISCONNECT);
                LogUtils.debug(TAG, "断开连接");
                LogUtils.debug(TAG, arg0[0].toString());
                LogUtils.debug(TAG, arg0[1].toString());
                LogUtils.debug(TAG, arg0[2].toString());
            }
        });

    }

    public void sendEventMessage(EventMessage eventMessage, Object content, String tracer) {
        if (isSocketConnect()) {
            LogUtils.debug(TAG, "eventMessage.getTag()=" + eventMessage.getTag());
            LogUtils.debug(TAG, "eventMessage.content=" + content);
            sendContent(content);
            tracerMap.put(tracer, eventMessage);
        } else {
            LogUtils.jasonDebug("isSocketConnect=false");
            eventMessage.setContent("time out");
            eventMessage.setStatus(-1);
            EventBus.getDefault().post(eventMessage);
        }
    }

    public void sendContent(Object content) {
        if (isSocketConnect()) {
            mSocket.emit("com.inspur.ecm.chat", content);
        }
    }

    private void sendWebSocketStatusBroadcaset(String event) {
        if (MyApplication.getInstance().isIndexActivityRunning()) {
            Intent intent = new Intent("message_notify");
            intent.putExtra("status", event);
            intent.putExtra("command", "websocket_status");
            LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
        }
    }

}
