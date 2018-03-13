package com.inspur.emmcloud.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.interf.CommonCallBack;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.PushInfoUtils;

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
	private Context context;
	private boolean isWebsocketConnnecting =false;

	public WebSocketPush(Context context) {
		this.context = context;
	}

	public static WebSocketPush getInstance(Context context){
		if (webSocketPush == null) {
			synchronized (WebSocketPush.class) {
				if (webSocketPush == null) {
					webSocketPush = new WebSocketPush(context);
				}
			}
		}
		return webSocketPush;
	}
	
	/**
	 * 开始WebSocket推送
	 */
	public void start() {
		// TODO Auto-generated method stub
		final String pushId = AppUtils.getPushId(context);
		if(((MyApplication)context.getApplicationContext()).isHaveLogin() && !StringUtils.isBlank(pushId)){
			new PushInfoUtils(context, new CommonCallBack() {
				@Override
				public void execute() {
					if (NetUtils.isNetworkConnected(context, false)) {
						String url = APIUri.getWebsocketConnectUrl();
						LogUtils.jasonDebug("url="+url);
						String path = "/chat/socket/handshake";
						WebSocketConnect(url, path,pushId);
					}
				}
			}).getChatClientId();




		}else {
			sendWebSocketStatusBroadcaset(Socket.EVENT_DISCONNECT);
		}
	}


	public void WebSocketConnect(String url, String path,String pushId) {
			if (!isSocketConnect() && !isWebsocketConnnecting){
				isWebsocketConnnecting = true;
				sendWebSocketStatusBroadcaset("socket_connecting");
				IO.Options opts = new IO.Options();
				opts.reconnectionAttempts = 4; // 设置websocket重连次数
				opts.forceNew = true;
				String clientId = PreferencesByUserAndTanentUtils.getString(context, Constant.PREF_CHAT_CLIENTID, "");
				Map<String, String> query = new HashMap<String, String>();
				query.put("client", clientId);
				// opts.transports = new String[] { Polling.NAME };
				opts.path = path;
				LogUtils.debug(TAG, "query.toString()=" + ParseQS.encode(query));
				opts.query = ParseQS.encode(query);
				try {
					closeSocket();
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
											headers.put("Authorization", Arrays.asList(((MyApplication)context.getApplicationContext()).getToken()));
										}
									}).on(Transport.EVENT_RESPONSE_HEADERS,
									new Emitter.Listener() {
										@Override
										public void call(Object... args) {
										}
									});
						}
					});

					connectWebSocket();
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
				isWebsocketConnnecting = false;
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
	

	public void sendActivedMsg() {
		if (mSocket != null && isSocketConnect()) {
			LogUtils.debug(TAG,"sendActivedMsg----");
			mSocket.emit("state", "ACTIVED");
		}else {
			start();
		}
	}

	public void sendFrozenMsg() {
		if (mSocket != null) {
			LogUtils.debug(TAG,"sendFrozenMsg----");
			mSocket.emit("state", "FROZEN");
	}
	}
	
	public void webSocketSignout(){
		if (mSocket != null) {
			LogUtils.debug(TAG,"webSocketSignout----");
			mSocket.emit("state", "SIGNOUT");
			closeSocket();
		}
	}

	private void addListeners() {

		mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				LogUtils.debug(TAG, "连接失败");
				LogUtils.debug(TAG,  arg0[0].toString());
				sendWebSocketStatusBroadcaset(Socket.EVENT_CONNECT_ERROR);

			}
		});
		mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				LogUtils.debug(TAG, "连接成功");
				sendWebSocketStatusBroadcaset(Socket.EVENT_CONNECT);
				if (((MyApplication) context.getApplicationContext())
						.getIsActive()) { // 当第一次连接成功后发送消息
					sendActivedMsg();
				} else {
					sendFrozenMsg();
				}
			}
		});


		mSocket.on("message", new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				LogUtils.debug(TAG, "message:" + arg0[0].toString());

				String content = arg0[0].toString();
				Intent intent = new Intent("com.inspur.msg_1.0");
				intent.putExtra("content", content);
				context.sendBroadcast(intent);
			}
		});

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
				String content = arg0[0].toString();
				Intent intent = new Intent("com.inspur.msg");
				intent.putExtra("content", content);
				context.sendBroadcast(intent);
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

	public void connectWebSocket() {
		addListeners();
		mSocket.open();
		LogUtils.debug(TAG, "mSocket.open");
	}

	public void setChatTextPlainMsg(String content,String cid,List<String> mentionsUidList){
		try {
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
			mSocket.emit("com.inspur.ecm.chat",object);
		}catch (Exception e){
			e.printStackTrace();
		}


	}

	public void closeSocket() {
		Log.d(TAG, "closeSocket------00");
		if (mSocket != null) {
			Log.d(TAG, "closeSocket------");
			mSocket.disconnect();
			mSocket.close();
			mSocket = null;
		}
	}

	private void sendWebSocketStatusBroadcaset(String event){
		if (((MyApplication) context.getApplicationContext())
				.isIndexActivityRunning()){
			Intent intent = new Intent("message_notify");
			intent.putExtra("status",event);
			intent.putExtra("command","websocket_status");
			context.sendBroadcast(intent);
		}
	}


}
