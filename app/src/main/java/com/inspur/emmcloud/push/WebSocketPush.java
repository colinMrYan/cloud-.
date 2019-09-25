package com.inspur.emmcloud.push;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.WSAPIService;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.util.AppExceptionCacheUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ClientIDUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.GetVoiceAndVideoResult;
import com.inspur.emmcloud.bean.chat.WSPushContent;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.bean.system.badge.BadgeBodyModel;
import com.inspur.emmcloud.bean.system.badge.GetWebSocketBadgeResult;
import com.inspur.emmcloud.componentservice.login.LoginService;

import org.greenrobot.eventbus.EventBus;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
    private List<EventMessage> requestEventMessageList = new ArrayList<>();
    private int timeCount = 0;
    private Timer timer;
    private Handler handler;
    private boolean isWebsocketConnecting = false;
    private boolean isWSStatusConnectedV1 = false;

    public WebSocketPush() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    timeCount++;
                    Iterator<EventMessage> it = requestEventMessageList.iterator();
                    while (it.hasNext()) {
                        EventMessage eventMessage = it.next();
                        if (eventMessage.getStartQuestTime() + MyAppConfig.WEBSOCKET_REQUEST_TIMEOUT <= timeCount) {
                            setRequestEventMessageTimeout(eventMessage, "socket_send_timeout");
                            it.remove();
                        }
                    }
                    if (requestEventMessageList.size() == 0) {
                        endTimeCount();
                    }

                }
            }
        };
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

    private void startTimeCount() {
        if (timer == null) {
            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (handler != null) {
                        handler.sendEmptyMessage(1);
                    }
                }
            };
            timer.schedule(task, 1000, 1000);
        }
    }

    private void endTimeCount() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void startWebSocket() {
        startWebSocket(false);
    }

    /**
     * 开始WebSocket推送
     *
     * @param isForceReconnect 强制重连
     */
    public void startWebSocket(final boolean isForceReconnect) {
        // TODO Auto-generated method stub
        if (!MyApplication.getInstance().isHaveLogin()) {
            return;
        }
        if (MyApplication.getInstance().getCurrentEnterprise() == null) {
            return;
        }
        //App在后台时不启动websocket
        if (!MyApplication.getInstance().getIsActive()) {
            return;
        }
        if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
            String pushId = PushManagerUtils.getInstance().getPushId(MyApplication.getInstance());
            if (!pushId.equals("UNKNOWN")) {
                WebSocketConnect(isForceReconnect);
            }
        } else if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
                new ClientIDUtils(MyApplication.getInstance(), new ClientIDUtils.OnGetClientIdListener() {
                    @Override
                    public void getClientIdSuccess(String clientId) {
                        WebSocketConnect(isForceReconnect);
                    }

                    @Override
                    public void getClientIdFail() {
                        sendWebSocketStatusBroadcast(Socket.EVENT_DISCONNECT);
                    }
                }).getClientId();
            } else {
                sendWebSocketStatusBroadcast(Socket.EVENT_DISCONNECT);
            }
        }
    }


    private void WebSocketConnect(boolean isForceReconnect) {
        synchronized (this) {
            if (isSocketConnect() || (!isForceReconnect && isWebsocketConnecting)) {
                return;
            }
            String url = APIUri.getWebsocketConnectUrl();
            String path = WebServiceRouterManager.getInstance().isV0VersionChat() ? "/" + MyApplication.getInstance().getCurrentEnterprise().getCode() + "/socket/handshake" :
                    "/chat/socket/handshake";
            sendWebSocketStatusBroadcast(Socket.EVENT_CONNECTING);
            IO.Options opts = new IO.Options();
            opts.reconnectionAttempts = 5; // 设置websocket重连次数
            opts.forceNew = true;
            Map<String, String> query = new HashMap<>();
            try {
                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                    String uuid = AppUtils.getMyUUID(MyApplication.getInstance());
                    String deviceName = AppUtils.getDeviceName(MyApplication.getInstance());
                    String pushId = PushManagerUtils.getInstance().getPushId(MyApplication.getInstance());
                    query.put("device.id", uuid);
                    query.put("device.name", deviceName);
                    query.put("device.push", pushId);
                    query.put("messageVer", "0");
                    query.put("channelVer", "0");
                } else {
                    String clientId = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_CLIENTID, "");
                    query.put("client", clientId);
                    query.put("messageVer", "1");
                    query.put("channelVer", "1");
                }
                query.put("enterprise", MyApplication.getInstance().getCurrentEnterprise().getId());
                opts.path = path;
                LogUtils.debug(TAG, "query.toString()=" + ParseQS.encode(query));
                opts.query = ParseQS.encode(query);
                opts.transports = new String[]{WebSocket.NAME};
                opts.forceNew = true;
                webSocketSignout();
                Manager manager = new Manager(new URI(url), opts);
                mSocket = manager.socket(WebServiceRouterManager.getInstance().getChatSocketNameSpace());
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
                                        if (MyApplication.getInstance().getToken() != null) {
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
                isWebsocketConnecting = true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                isWebsocketConnecting = false;
                e.printStackTrace();
                sendWebSocketStatusBroadcast(Socket.EVENT_CONNECT_ERROR);
                Log.d(TAG, "e=" + e.toString());
                if (mSocket != null) {
                    mSocket.disconnect();
                    mSocket.off();
                    mSocket.close();
                    mSocket = null;
                }
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
            if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                return mSocket.connected();
            } else if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
                return mSocket.connected() && isWSStatusConnectedV1;
            }

        }
        return false;

    }

    public void sendAppStatus() {
        boolean isActive = MyApplication.getInstance().getIsActive();
        if (isSocketConnect()) {
            if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                String appStatus = isActive ? "ACTIVED" : "FROZEN";
                LogUtils.debug(TAG, "发送App状态：" + appStatus);
                mSocket.emit("state", appStatus);
            } else {
                WSAPIService.getInstance().sendAppStatus(isActive ? "ACTIVED" : "SUSPEND");
                LogUtils.debug(TAG, "发送App状态：" + (isActive ? "ACTIVED" : "SUSPEND"));
                if (!isActive) {
                    closeWebsocket();
                }
            }
        } else if (isActive) {
            startWebSocket();
        }
    }

    public void webSocketSignout() {
        if (mSocket != null) {
            if (isSocketConnect()) {
                LogUtils.debug(TAG, "注销");
                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
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
        isWebsocketConnecting = false;
        endTimeCount();
        if (mSocket != null) {
            mSocket.disconnect();
            removeListeners();
            mSocket.close();
            mSocket = null;
        }
        for (EventMessage eventMessage : requestEventMessageList) {
            setRequestEventMessageTimeout(eventMessage, "socket_force_close");
        }
        requestEventMessageList.clear();
    }

    private void removeListeners() {
        mSocket.off("message");
        mSocket.off("com.inspur.ecm.chat");
        mSocket.off("status");
        mSocket.off(Socket.EVENT_CONNECT_ERROR);
        mSocket.off(Socket.EVENT_CONNECT);
        mSocket.off(Socket.EVENT_DISCONNECT);
        mSocket.off(Socket.EVENT_CONNECTING);
        mSocket.off(Socket.EVENT_RECONNECTING);
    }

    private void addListeners() {
        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... arg0) {
                // TODO Auto-generated method stub
                isWebsocketConnecting = false;
                LogUtils.debug(TAG, "连接失败");
                if (arg0[0] != null) {
                    try {
                        ((Exception) arg0[0]).printStackTrace();
                        LogUtils.debug(TAG, "arg0[0]==" + arg0[0].toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                sendWebSocketStatusBroadcast(Socket.EVENT_CONNECT_ERROR);

            }
        });

        mSocket.on(Socket.EVENT_CONNECTING, new Emitter.Listener() {

            @Override
            public void call(Object... arg0) {
                // TODO Auto-generated method stub
                isWebsocketConnecting = true;
                LogUtils.debug(TAG, "正在连接");
            }
        });
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... arg0) {
                // TODO Auto-generated method stub
                if (WebServiceRouterManager.getInstance().isV0VersionChat()) {
                    LogUtils.debug(TAG, "连接成功");
                    sendWebSocketStatusBroadcast(Socket.EVENT_CONNECT);
                    // 当第一次连接成功后发送App目前的状态消息
                    sendAppStatus();
                    isWebsocketConnecting = false;
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
                    isWSStatusConnectedV1 = true;
                    sendWebSocketStatusBroadcast(Socket.EVENT_CONNECT);
                    // 当第一次连接成功后发送App目前的状态消息
                    sendAppStatus();
                } else if (code == 401) {
                    closeWebsocket();
                    sendWebSocketStatusBroadcast(Socket.EVENT_CONNECT_ERROR);
                    Router router = Router.getInstance();
                    if (router.getService(LoginService.class) != null) {
                        LoginService service = router.getService(LoginService.class);
                        service.refreshToken(null, System.currentTimeMillis());
                    }
                }
                isWebsocketConnecting = false;
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
                try {
                    LogUtils.debug(TAG, "arg0[0].toString()=" + arg0[0].toString());
                    WSPushContent wsPushContent = new WSPushContent(arg0[0].toString());
                    String path = wsPushContent.getPath();
                    //客户端主动请求
                    if (StringUtils.isBlank(path)) {
                        String tracer = wsPushContent.getTracer();
                        int index = requestEventMessageList.indexOf(new EventMessage(tracer));
                        if (index != -1) {
                            EventMessage eventMessage = requestEventMessageList.get(index);
                            requestEventMessageList.remove(index);
                            String body = wsPushContent.getBody();
                            eventMessage.setContent(body);
                            eventMessage.setStatus(wsPushContent.getStatus());
                            EventBus.getDefault().post(eventMessage);
                            if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE) && eventMessage.getStatus() != EventMessage.RESULT_OK) {
                                saveWSSendMessageException(2, body, wsPushContent.getStatus());
                            }
                        }
                    } else {
                        switch (path) {
                            case "/channel/message":
                                if (wsPushContent.getMethod().equals("post")) {
                                    EventMessage eventMessagea = new EventMessage("", Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE, wsPushContent.getBody());
                                    EventBus.getDefault().post(eventMessagea);
                                }
                                break;
                            case "/channel/message/state/unread":
                                if (wsPushContent.getMethod().equals("delete")) {
                                    EventMessage eventMessagea = new EventMessage("", Constant.EVENTBUS_TAG_RECERIVER_MESSAGE_STATE_READ, wsPushContent.getBody());
                                    EventBus.getDefault().post(eventMessagea);
                                }
                                break;
                            case "/unread-count":
                                if (wsPushContent.getMethod().equals("put")) {
                                    GetWebSocketBadgeResult getWebSocketBadgeResult = new GetWebSocketBadgeResult(wsPushContent.getBody());
                                    BadgeBodyModel badgeBodyModel = getWebSocketBadgeResult.getBadgeBodyModel();
                                    EventBus.getDefault().post(badgeBodyModel);
                                }
                                break;
                            case "/command/client":
                                if (wsPushContent.getMethod().equals("post")) {
                                    GetVoiceAndVideoResult getVoiceAndVideoResult = new GetVoiceAndVideoResult(wsPushContent.getBody());
                                    EventBus.getDefault().post(getVoiceAndVideoResult);
                                }
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... arg0) {
                // TODO Auto-generated method stub
                isWSStatusConnectedV1 = false;
                isWebsocketConnecting = false;
                sendWebSocketStatusBroadcast(Socket.EVENT_DISCONNECT);
                LogUtils.debug(TAG, "断开连接");
                LogUtils.debug(TAG, arg0[0].toString());

                if (arg0[0] != null) {
                    try {
                        ((Exception) arg0[0]).printStackTrace();
                        LogUtils.debug(TAG, "arg0[0]==" + arg0[0].toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public void sendEventMessage(EventMessage eventMessage, Object content, String tracer) {
        if (isSocketConnect()) {
            LogUtils.debug(TAG, "eventMessage.getTag()=" + eventMessage.getTag());
            LogUtils.debug(TAG, "eventMessage.content=" + content);
            eventMessage.setStartQuestTime(timeCount);
            requestEventMessageList.add(eventMessage);
            startTimeCount();
            sendContent(content);
        } else {
            LogUtils.jasonDebug("isSocketConnect=false");
            setRequestEventMessageTimeout(eventMessage, "socket_disconnect");
            startWebSocket();
        }
    }

    private void setRequestEventMessageTimeout(EventMessage eventMessage, String timeoutType) {
        if (eventMessage != null) {
            eventMessage.setContent("time out");
            eventMessage.setStatus(-1);
            EventBus.getDefault().post(eventMessage);
            if (eventMessage.getTag().equals(Constant.EVENTBUS_TAG_RECERIVER_SINGLE_WS_MESSAGE)) {
                saveWSSendMessageException(3, timeoutType, 1001);
            }
        }
    }

    public void sendContent(Object content) {
        if (isSocketConnect()) {
            mSocket.emit("com.inspur.ecm.chat", content);
        }
    }

    private void sendWebSocketStatusBroadcast(String event) {
        if (MyApplication.getInstance().isIndexActivityRunning()) {
            Intent intent = new Intent("message_notify");
            intent.putExtra("status", event);
            intent.putExtra("command", "websocket_status");
            LocalBroadcastManager.getInstance(MyApplication.getInstance()).sendBroadcast(intent);
        }
    }

    /**
     * 获取websocket目前的状态
     *
     * @return
     */
    public String getWebsocketStatus() {
        if (isWebsocketConnecting) {
            return Socket.EVENT_CONNECTING;
        } else if (isSocketConnect()) {
            return Socket.EVENT_CONNECT;
        } else {
            return Socket.EVENT_DISCONNECT;
        }
    }

    private void saveWSSendMessageException(final int errorLevel, final String error, final int responseCode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppExceptionCacheUtils.saveAppException(MyApplication.getInstance(), errorLevel, "ws_send_message", error, responseCode);
            }
        }).start();

    }

}
