package com.inspur.imp.plugin.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.util.DialogUtil;
import com.inspur.imp.util.StrUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * 发送短信
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class SmsService extends ImpPlugin {

	private String tel;// 电话号码
	private String msg;// 短信内容
	private JSONArray tels;// 电话号码

	private String successFunt;
	private String errorFunt;
	private BroadcastReceiver sendmessage;

	/** 发送与接收的广播 **/
	String SENT_SMS_ACTION = "SENT_SMS_ACTION";

	@Override
	public void execute(String action, JSONObject paramsObject) {
		// 打开系统发送短信的界面，根据传入参数自动填写好相关信息
		if ("open".equals(action)) {
			open(paramsObject);
		}
		// 直接发送短
		else if ("send".equals(action)) {
			send(paramsObject);
		}
		// 直接发送短
		else if ("batchSend".equals(action)) {
			batchSend(paramsObject);
		}else{
			DialogUtil.getInstance(getActivity()).show();
		}

	}

	@Override
	public String executeAndReturn(String action, JSONObject paramsObject) {
		DialogUtil.getInstance(getActivity()).show();
		return super.executeAndReturn(action, paramsObject);
	}

	/**
	 * 打开系统发送短信的界面
	 * 
	 * @param paramsObject
	 */
	private void open(JSONObject paramsObject) {
		// 打开系统发送短信的界面
		try {
			if (!paramsObject.isNull("tel"))
				tel = paramsObject.getString("tel");
			if (!paramsObject.isNull("msg"))
				msg = paramsObject.getString("msg");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (!StrUtil.strIsNotNull(tel) || !StrUtil.strIsNotNull(msg)) {
			Toast.makeText(this.context, "电话号码或信息不能为空！", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
		sendIntent.setData(Uri.parse("smsto:" + tel));
		sendIntent.putExtra("sms_body", msg);
		((Activity)this.context).startActivityForResult(sendIntent, ImpActivity.DO_NOTHING_RESULTCODE);
	}

	/**
	 * 群发短信
	 * 
	 * @param paramsObject
	 */
	private void batchSend(JSONObject paramsObject) {
		// 解析json串获取到传递过来的参数
		try {
			if (!paramsObject.isNull("telArray"))
				tels = paramsObject.getJSONArray("telArray");
			if (!paramsObject.isNull("msg"))
				msg = paramsObject.getString("msg");
			if (!paramsObject.isNull("successCb"))
				successFunt = paramsObject.getString("successCb");
			if (!paramsObject.isNull("errorCb"))
				errorFunt = paramsObject.getString("errorCb");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < tels.length(); i++) {
			try {
				String tel = tels.getString(i);
				if (!StrUtil.strIsNotNull(tel) || !StrUtil.strIsNotNull(msg)) {
					Toast.makeText(this.context, "电话号码或信息不能为空！",Toast.LENGTH_SHORT).show();
					return;
				}
				// 短信管理器
				SmsManager manager = SmsManager.getDefault();
				
				sendmessage = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						// 判断短信是否发送成功
						switch (getResultCode()) {
						case Activity.RESULT_OK:
							jsCallback(successFunt);
							context.unregisterReceiver(this);
							break;
						default:
							jsCallback(errorFunt);
							context.unregisterReceiver(this);
							break;
						}
					}
				};
				context.registerReceiver(sendmessage, new IntentFilter(SENT_SMS_ACTION));
				
				// 发送状态确认
				Intent sentIntent = new Intent(SENT_SMS_ACTION);
				PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
						sentIntent, 0);

				// 拆分短信内容
				ArrayList<String> texts = manager.divideMessage(msg);
				for (String text : texts) {
					// 第四个参数是短信发送状态，最后一个参数对方接收短信的状态
					manager.sendTextMessage(tel, null, text, sentPI, null);
				}
					// 将短信插入到数据库中
					ContentValues values = new ContentValues();
					// 设置短信内容
					values.put("body", msg);
					// 设置发送短信的日期（单位：毫秒）
					values.put("date", new Date().getTime());
					// 设置目标电话号码
					values.put("address", tel);
					// 该字段值：1标识接受到的短信，2标识发送的短信
					values.put("type", 2);
					// 短信数据库的位置
					Uri uri = Uri.parse("content://sms/sent");
					// 通过该资源位置，插入该条短信
					context.getContentResolver().insert(uri, values);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 直接发送短信
	 * 
	 * @param paramsObject
	 */
	private void send(JSONObject paramsObject) {
		// 解析json串获取到传递过来的参数
		try {
			if (!paramsObject.isNull("tel"))
				tel = paramsObject.getString("tel");
			if (!paramsObject.isNull("msg"))
				msg = paramsObject.getString("msg");
			if (!paramsObject.isNull("successCb"))
				successFunt = paramsObject.getString("successCb");
			if (!paramsObject.isNull("errorCb"))
				errorFunt = paramsObject.getString("errorCb");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (!StrUtil.strIsNotNull(tel) || !StrUtil.strIsNotNull(msg)) {
			Toast.makeText(this.context, "电话号码或信息不能为空！", Toast.LENGTH_SHORT).show();
			return;
		}
		// 短信管理器
		SmsManager manager = SmsManager.getDefault();
		
		sendmessage = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// 判断短信是否发送成功
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					jsCallback(successFunt);
					context.unregisterReceiver(sendmessage);
					break;
				default:
					jsCallback(errorFunt);
					context.unregisterReceiver(sendmessage);
					break;
				}
			}
		};
		LocalBroadcastManager.getInstance(context).registerReceiver(sendmessage, new IntentFilter(SENT_SMS_ACTION));
		
		
		// 发送状态确认
		Intent sentIntent = new Intent(SENT_SMS_ACTION);
		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
				sentIntent, 0);

		// 拆分短信内容
		ArrayList<String> texts = manager.divideMessage(msg);
		for (String text : texts) {
			// 第四个参数是短信发送状态，最后一个参数对方接收短信的状态
			manager.sendTextMessage(tel, null, text, sentPI, null);
		}
			// 将短信插入到数据库中
			ContentValues values = new ContentValues();
			// 设置短信内容
			values.put("body", msg);
			// 设置发送短信的日期（单位：毫秒）
			values.put("date", new Date().getTime());
			// 设置目标电话号码
			values.put("address", tel);
			// 该字段值：1标识接受到的短信，2标识发送的短信
			values.put("type", 2);
			// 短信数据库的位置
			Uri uri = Uri.parse("content://sms/sent");
			// 通过该资源位置，插入该条短信
			context.getContentResolver().insert(uri, values);
			
			
		}

	@Override
	public void onDestroy() {
		
	}
}
