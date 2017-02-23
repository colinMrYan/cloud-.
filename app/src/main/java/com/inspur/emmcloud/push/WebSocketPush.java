package com.inspur.emmcloud.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.GetMyInfoResult;
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
			webSocketPush = new WebSocketPush(context);
		}
		return webSocketPush;
	}
	
	/**
	 * 开始WebSocket推送
	 */
	public void start() {
		// TODO Auto-generated method stub
			String url = "https://ecm.inspur.com/";
			String myInfo = PreferencesUtils.getString(context, "myInfo","");
			GetMyInfoResult myInfoResult = new GetMyInfoResult(myInfo); 
			String enterpriseCode = myInfoResult.getEnterpriseCode();
			String path = "/"+enterpriseCode+"/socket/handshake";
			WebSocketConnect(url, path);
			LogUtils.debug("yfcLog", "socketPath:"+path);
	}


	public void WebSocketConnect(String url, String path) {
		if (((MyApplication)context.getApplicationContext()).getToken() == null) {
			return;
		}
		String username = PreferencesUtils.getString(context, "userRealName");
		String uuid = AppUtils.getMyUUID(context);
		String pushid = PreferencesUtils.getString(context, "JpushRegId", "");
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
	

	public void sendActivedMsg() {
		if (mSocket != null) {
			Log.d("jason", "send----------actived");
			mSocket.emit("state", "ACTIVED");
		}
	}

	public void sendFrozenMsg() {
		if (mSocket != null) {
			Log.d("jason", "send----------frozen");
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

			}
		});
		mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

			@Override
			public void call(Object... arg0) {
				// TODO Auto-generated method stub
				LogUtils.debug(TAG, "连接成功");
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
				
				LogUtils.debug(TAG, "断开连接");
			}
		});
	}

	public void connectWebSocket() {
		addListeners();
		mSocket.open();
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


}
