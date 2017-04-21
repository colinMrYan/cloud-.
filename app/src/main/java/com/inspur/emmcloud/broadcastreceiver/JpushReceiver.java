package com.inspur.emmcloud.broadcastreceiver;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.CalendarEvent;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.ui.work.calendar.CalEventAddActivity;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则： 1) 默认用户会打开主界面 2) 接收不到自定义消息
 */
public class JpushReceiver extends BroadcastReceiver {
	private static final String TAG = "JPush";

	private boolean isAppRunning;
	private Handler handler;
	private static final int LOGIN_SUCCESS = 0;
	private static final int LOGIN_FAIL = 1;

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction()
				+ ", extras: " + printBundle(bundle));
		handMessage(context);
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
			String accessToken = PreferencesUtils.getString(context,
					"accessToken", "");
			((MyApplication)context.getApplicationContext()).clearNotification();
			if (!StringUtils.isBlank(accessToken)) {
				String extra = "";
				if (bundle.containsKey(JPushInterface.EXTRA_EXTRA)) {
					extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
				}

				if (!StringUtils.isBlank(extra)) {
					try {
						JSONObject extraObj = new JSONObject(extra);
						if (extraObj.has("calEvent")) {
							String json = extraObj.getString("calEvent");
							JSONObject calEventObj = new JSONObject(json);
							openCalEvent(context, calEventObj);
							return;
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				Intent indexLogin = new Intent(context, IndexActivity.class);
				indexLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				context.startActivity(indexLogin);
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

	/**
	 * 打开
	 * @param context
	 * @param jsonObject
     */
	private void openCalEvent(Context context, JSONObject jsonObject) {
		// TODO Auto-generated method stub
		CalendarEvent calendarEvent = new CalendarEvent(jsonObject);
		Intent intent = new Intent();
		intent.setClass(context, CalEventAddActivity.class);
		intent.putExtra("calEvent", calendarEvent);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

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

	// send msg to MainActivity
	private void processCustomMessage(Context context, Bundle bundle) {
		// 这段代码用来把透传消息发送到主Activity上去
		// if (MainActivity.isForeground) {
		// String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		// String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
		// Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
		// msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
		// if (!ExampleUtil.isEmpty(extras)) {
		// try {
		// JSONObject extraJson = new JSONObject(extras);
		// if (null != extraJson && extraJson.length() > 0) {
		// msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
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

	/**
	 * 判断应用是否运行
	 * 
	 * @param context
	 * @return
	 */
	public boolean getIsAppRunning(Context context) {
		// boolean flag = false;
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<RunningTaskInfo> list = am.getRunningTasks(100);

		for (RunningTaskInfo info : list) {

			if (info.topActivity.getPackageName().equals("com.inspur.emmcloud")
					&& info.baseActivity.getPackageName().equals(
							"com.inspur.emmcloud")) {
				isAppRunning = true;
				break;
			}

		}
		return isAppRunning;
	}

	/**
	 * 判断某个界面是否在前台
	 * 
	 * @param context
	 * @param className
	 *            某个界面名称
	 */
	private boolean isForeground(Context context, String className) {
		if (context == null || TextUtils.isEmpty(className)) {
			return false;
		}

		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(1);
		if (list != null && list.size() > 0) {
			ComponentName cpn = list.get(0).topActivity;
			if (className.equals(cpn.getClassName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 跳转登录界面
	 */
	private void goLogin(Context context) {
		Intent intent = new Intent(context, LoginActivity.class);
		context.startActivity(intent);
	}

	/**
	 * 跳转主界面
	 */
	private void goIndex(Context context) {
		Intent intent = new Intent(context, IndexActivity.class);
		context.startActivity(intent);
	}

	private void handMessage(final Context context) {
		// TODO Auto-generated method stub
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case LOGIN_SUCCESS:
					goIndex(context);
					break;

				case LOGIN_FAIL:

					goLogin(context);
					break;

				default:
					break;
				}
			}

		};
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
