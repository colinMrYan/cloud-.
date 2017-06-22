package com.inspur.emmcloud.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesUtils;

import java.net.URISyntaxException;
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
			String url = "https://ecm.inspur.com/";
			String enterpriseCode = ((MyApplication)context.getApplicationContext()).getCurrentEnterprise().getCode();
			String path = "/"+enterpriseCode+"/socket/handshake";
			WebSocketConnect(url, path);
	}


	public void WebSocketConnect(String url, String path) {
		if (((MyApplication)context.getApplicationContext()).getToken() == null) {
			return;
		}
		synchronized (this) {
			if (!isSocketConnect()){
				sendWebSocketStatusBroadcaset("socket_connecting");
				String username = PreferencesUtils.getString(context, "userRealName");
				String uuid = AppUtils.getMyUUID(context);
				String pushid = PreferencesUtils.getString(context, "JpushRegId", "");
				LogUtils.YfcDebug("走到连接Socket"+AppUtils.GetChangShang().toLowerCase());
				pushid = getPushIdByChangeShang(pushid);

				boolean isTelbet = AppUtils.isTablet(context);
				String name;
				if (isTelbet) {
					name = username + "的平板电脑";
				} else {
					name = username + "的手机";
				}
//		final BlockingQueue<Object> values = new LinkedBlockingQueue<Object>();
				IO.Options opts = new IO.Options();
				opts.reconnectionAttempts = 4; // 设置websocket重连次数
				opts.forceNew = true;
				Map<String, String> query = new HashMap<String, String>();
				query.put("device.id", uuid);
				query.put("device.name", name);
				query.put("device.push", pushid);
				// opts.transports = new String[] { Polling.NAME };
				opts.path = path;
				LogUtils.debug(TAG, "query.toString()=" + ParseQS.encode(query));
				opts.query = ParseQS.encode(query);
				try {
					closeSocket();
					mSocket = IO.socket(url, opts);
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
				} catch (URISyntaxException e) {
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
		}


	}

	/**
	 * 通过厂商确定pushid
	 * @param pushid
	 * @return
     */
	private String getPushIdByChangeShang(String pushid) {
		if(AppUtils.GetChangShang().toLowerCase().startsWith("huawei")){
			//需要对华为单独推送的时候解开这里
//			pushid = AppUtils.getIMEICode(context)+"@push.huawei.com";
		}
		return pushid;
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
			Log.d(TAG, "send----------actived");
			mSocket.emit("state", "ACTIVED");
		}else {
			start();
		}
	}

	public void sendFrozenMsg() {
		if (mSocket != null) {
			Log.d(TAG, "send----------frozen");
			mSocket.emit("state", "FROZEN");
	}
	}
	
	public void webSocketSignout(){
		if (mSocket != null) {
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
				Intent intent = new Intent("com.inspur.msg");
				intent.putExtra("push", content);
				context.sendBroadcast(intent);
			}
		});

		mSocket.on("debug", new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				LogUtils.debug(TAG, "debug:" + arg0[0].toString());

				// Intent intent = new Intent();
				// intent.setAction("com.inspur.msg");
				// intent.putExtra("push", arg0[0].toString());
				// context.sendBroadcast(intent);
			}
		});
		mSocket.on("command", new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				LogUtils.debug(TAG, "command" + arg0[0].toString());
//
//				String content = arg0[0].toString();
//				Intent intent = new Intent("com.inspur.command");
//				intent.putExtra("command", content);
//				context.sendBroadcast(intent);
			}
		});

		mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				sendWebSocketStatusBroadcaset(Socket.EVENT_DISCONNECT);
				LogUtils.debug(TAG, "断开连接");
			}
		});
	}

	public void connectWebSocket() {
		addListeners();
		mSocket.open();
		LogUtils.debug(TAG, "mSocket.open");
		// mSocket.connect();
	}
	
	public void reConnectWebSocket() {
		// mSocket.connect();
		mSocket.connect();
	}

	public void closeSocket() {
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
