package com.inspur.emmcloud.push;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.chat.WSPushMessageContent;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.PushInfoUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

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
import io.socket.parseqs.ParseQS;

public class WebSocketPush {

	private static final String TAG = "WebSocketPush";
	private static WebSocketPush webSocketPush=null;
	private  Socket mSocket = null;

	public WebSocketPush() {}

	public static WebSocketPush getInstance(){
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
		if (isForceNew || !isSocketConnect()){
			if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)){
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
			}else {
				sendWebSocketStatusBroadcaset(Socket.EVENT_DISCONNECT);
			}
		}
	}


	private void WebSocketConnect(String chatClientId) {
			String url = APIUri.getWebsocketConnectUrl();
			String path = "/chat/socket/handshake";
			sendWebSocketStatusBroadcaset("socket_connecting");
			IO.Options opts = new IO.Options();
			opts.reconnectionAttempts = 4; // 设置websocket重连次数
			opts.forceNew = true;
			Map<String, String> query = new HashMap<String, String>();
			query.put("client", chatClientId);
			// opts.transports = new String[] { Polling.NAME };
			opts.path = path;
			LogUtils.debug(TAG, "query.toString()=" + ParseQS.encode(query));
			opts.query = ParseQS.encode(query);
			try {
				webSocketSignout();
				Manager manager = new Manager(new URI(url),opts);
				mSocket = manager.socket("/api/v1");
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
										headers.put("Authorization", Arrays.asList(MyApplication.getInstance().getToken()));
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
	private boolean isSocketConnect() {
		if (mSocket != null) {
			return mSocket.connected();
		}
		return false;

	}

	public void sendAppStatus(boolean isActive){
		if (isSocketConnect()){
			String appStatus = isActive?"ACTIVED":"FROZEN";
			LogUtils.debug(TAG,  "发送App状态："+appStatus);
			mSocket.emit("state", appStatus);
		}
	}

	public void webSocketSignout(){
		if (mSocket != null) {
			if (isSocketConnect()) {
				LogUtils.debug(TAG,  "注销");
				mSocket.emit("state", "SIGNOUT");
			}
			mSocket.disconnect();
			mSocket.close();
			mSocket = null;
		}
	}

	private void addListeners() {

		mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				LogUtils.debug(TAG, "连接失败");
				sendWebSocketStatusBroadcaset(Socket.EVENT_CONNECT_ERROR);

			}
		});
		mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				LogUtils.debug(TAG, "连接成功");
				sendWebSocketStatusBroadcaset(Socket.EVENT_CONNECT);
				// 当第一次连接成功后发送App目前的状态消息
				sendAppStatus(MyApplication.getInstance().getIsActive());
			}
		});


//		mSocket.on("message", new Emitter.Listener() {
//
//			@Override
//			public void call(Object... arg0) {
//				// TODO Auto-generated method stub
//				LogUtils.debug(TAG, "message:" + arg0[0].toString());
//				String content = arg0[0].toString();
//				Intent intent = new Intent("com.inspur.msg_1.0");
//				intent.putExtra("content", content);
//				context.sendBroadcast(intent);
//			}
//		});

		mSocket.on("debug", new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				LogUtils.debug(TAG, "debug:" + arg0[0].toString());
			}
		});
		mSocket.on("command", new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				LogUtils.debug(TAG, "command" + arg0[0].toString());
			}
		});

		mSocket.on("com.inspur.ecm.chat", new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				WSPushMessageContent wsPushMessageContent = new WSPushMessageContent(arg0[0].toString());
				//将websocket推送过来的内容通过EventBus发送
				EventBus.getDefault().post(wsPushMessageContent);
			}
		});

		mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				sendWebSocketStatusBroadcaset(Socket.EVENT_DISCONNECT);
				LogUtils.debug(TAG, "断开连接");
				LogUtils.debug(TAG,  arg0[0].toString());
			}
		});

	}

	public void sendChatTextPlainMsg(String content, String cid, List<String> mentionsUidList){
		try {
			if (isSocketConnect()){
				JSONObject object = new JSONObject();
				JSONObject actionObj = new JSONObject();
				actionObj.put("method","post");
				actionObj.put("path","/channel/"+cid+"/message");
				object.put("action",actionObj);
				JSONObject headerObj = new JSONObject();
				headerObj.put("enterprise",MyApplication.getInstance().getCurrentEnterprise().getId());
				headerObj.put("tracer","a"+MyApplication.getInstance().getUid()+System.currentTimeMillis());
				object.put("headers",headerObj);
				JSONObject bodyObj = new JSONObject();
				bodyObj.put("type","text/plain");
				bodyObj.put("text",content);
				//bodyObj.put("mentions", JSONUtils.toJSONArray(mentionsUidList));
				object.put("body",bodyObj);
				LogUtils.jasonDebug("object="+object.toString());
				mSocket.emit("com.inspur.ecm.chat",object, new Emitter.Listener() {
					@Override
					public void call(Object... args) {
						LogUtils.jasonDebug("args==="+args[0]);
					}
				});
			}
		}catch (Exception e){
			e.printStackTrace();
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
