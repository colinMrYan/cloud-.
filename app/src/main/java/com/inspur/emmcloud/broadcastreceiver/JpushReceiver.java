package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.CalendarEvent;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.chat.ChannelActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.work.calendar.CalEventAddActivity;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则： 1) 默认用户会打开主界面 2) 接收不到自定义消息
 */
public class JpushReceiver extends BroadcastReceiver {
	private static final String TAG = "JPush";

	private static final int LOGIN_SUCCESS = 0;
	private static final int LOGIN_FAIL = 1;

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction()
				+ ", extras: " + printBundle(bundle));
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			String regId = bundle
					.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
			PreferencesUtils.putString(context, "JpushRegId", regId);
			// send the Registration Id to your server...

			// PreferencesUtils.put

		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
				.getAction())) {
			Log.d(TAG,
					"[MyReceiver] 接收到推送下来的自定义消息: "
							+ bundle.getString(JPushInterface.EXTRA_MESSAGE));
			processCustomMessage(context, bundle);

		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent
				.getAction())) {
			Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
			int notifactionId = bundle
					.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
			Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);

		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
				.getAction())) {
			Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
			Log.d(TAG, "extra=" + bundle.getString(JPushInterface.EXTRA_EXTRA));
			((MyApplication)context.getApplicationContext()).clearNotification();
			if (((MyApplication)context.getApplicationContext()).isHaveLogin()){
				String extra = "";
				if (bundle.containsKey(JPushInterface.EXTRA_EXTRA)) {
					extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
				}
				if (!((MyApplication) context.getApplicationContext()).getIsActive()){
					Intent indexIntent = new Intent(context, IndexActivity.class);
					Intent targetIntent = null;
					indexIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					if (!StringUtils.isBlank(extra)) {
						try {
							JSONObject extraObj = new JSONObject(extra);
							if (extraObj.has("calEvent")) {
								String json = extraObj.getString("calEvent");
								JSONObject calEventObj = new JSONObject(json);
								CalendarEvent calendarEvent = new CalendarEvent(calEventObj);
								targetIntent = new Intent(context, CalEventAddActivity.class);
								targetIntent.putExtra("calEvent", calendarEvent);
								targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							}else{
								if (extraObj.has("action")){
									JSONObject actionObj = extraObj.getJSONObject("action");
									String type = actionObj.getString("type");
									if (type.equals("open-url")){
										String scheme = actionObj.getString("url");
										targetIntent =new Intent(Intent.ACTION_VIEW);
										targetIntent.setData(Uri.parse(scheme));
										targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									}
								}else if(extraObj.has("channel")){
									String cid = JSONUtils.getString(extraObj,"channel","");
									if (!StringUtils.isBlank(cid)){
										targetIntent = new Intent(context, ChannelActivity.class);
										targetIntent.putExtra("get_new_msg",true);
										targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									}
								}

							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (!((MyApplication)context.getApplicationContext()).isIndexActivityRunning()){
						indexIntent.putExtra("command","open_notification");
					}else {
						indexIntent.setClass(context,MainActivity.class);
					}
					context.startActivity(indexIntent);
					if (targetIntent != null && NetUtils.isNetworkConnected(context,false)){
						context.startActivity(targetIntent);
					}
				}

			}else {

				Intent loginIntent = new Intent(context, LoginActivity.class);
				loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(loginIntent);
			}

		} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent
				.getAction())) {
			Log.d(TAG,
					"[MyReceiver] 用户收到到RICH PUSH CALLBACK: "
							+ bundle.getString(JPushInterface.EXTRA_EXTRA));
			// 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
			// 打开一个网页等..

		} else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent
				.getAction())) {
			boolean connected = intent.getBooleanExtra(
					JPushInterface.EXTRA_CONNECTION_CHANGE, false);
			Log.w(TAG, "[MyReceiver]" + intent.getAction()
					+ " connected state change to " + connected);
		} else {
			Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
		}
	}


	// 打印所有的 intent extra 数据
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			} else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
				if (bundle.getString(JPushInterface.EXTRA_EXTRA).isEmpty()) {
					Log.i(TAG, "This message has no Extra data");
					continue;
				}

				try {
					JSONObject json = new JSONObject(
							bundle.getString(JPushInterface.EXTRA_EXTRA));
					Iterator<String> it = json.keys();

					while (it.hasNext()) {
						String myKey = it.next().toString();
						sb.append("\nkey:" + key + ", value: [" + myKey + " - "
								+ json.optString(myKey) + "]");
					}
				} catch (JSONException e) {
					Log.e(TAG, "Get message extra JSON error!");
				}

			} else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}

	// send msg to MyCameraActivity
	private void processCustomMessage(Context context, Bundle bundle) {
		// 这段代码用来把透传消息发送到主Activity上去
		// if (MyCameraActivity.isForeground) {
		// String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		// String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
		// Intent msgIntent = new Intent(MyCameraActivity.MESSAGE_RECEIVED_ACTION);
		// msgIntent.putExtra(MyCameraActivity.KEY_MESSAGE, message);
		// if (!ExampleUtil.isEmpty(extras)) {
		// try {
		// JSONObject extraJson = new JSONObject(extras);
		// if (null != extraJson && extraJson.length() > 0) {
		// msgIntent.putExtra(MyCameraActivity.KEY_EXTRAS, extras);
		// }
		// } catch (JSONException e) {
		//
		// }
		//
		// }
		// context.sendBroadcast(msgIntent);
		// }

		String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
	}




	// public void buildNotification(){
	// BasicPushNotificationBuilder builder = new
	// BasicPushNotificationBuilder(context);
	// // builder.statusBarDrawable = R.drawable.jpush_notification_icon;
	// builder.notificationFlags = Notification.FLAG_AUTO_CANCEL; //设置为自动消失
	// builder.notificationDefaults = Notification.DEFAULT_SOUND ｜
	// Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS; // 设置为铃声与震动都要
	// JPushInterface.setPushNotificationBuilder(1, builder);
	//
	// }
}
