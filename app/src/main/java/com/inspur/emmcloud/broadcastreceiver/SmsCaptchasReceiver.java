package com.inspur.emmcloud.broadcastreceiver;

import android.app.Activity;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.common.systool.permission.PermissionMangerUtils;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestManagerUtils;
import com.yanzhenjie.permission.Permission;

import java.util.List;

/**
 * 监听短信数据库
 * 
 * @author Administrator
 *
 */
public class SmsCaptchasReceiver extends ContentObserver {

	private static final int SENDED_CAPTCHAS_MSG = 2;
	private Cursor cursor = null;
	private Handler handler;
	private Activity context;

	public SmsCaptchasReceiver(Activity context,Handler handler) {
		super(handler);
		this.context = context;
		this.handler = handler;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		new PermissionMangerUtils(context, Permission.READ_SMS, new PermissionRequestCallback() {
			@Override
			public void onPermissionRequestSuccess(List<String> permissions) {
				readSMSMessages();
			}

			@Override
			public void onPermissionRequestFail(List<String> permissions) {
				ToastUtils.show(context, PermissionRequestManagerUtils.getInstance().getPermissionToast(context,permissions));
			}

		}).start();

	}

	private void readSMSMessages() {
		// 读取收件箱中指定号码的短信
		cursor = context.managedQuery(Uri.parse("content://sms/inbox"), new String[] {
						"_id", "address", "read", "body" }, " address=? and read=?",
				new String[] { "10655010187420709105", "0" }, "_id desc");// 按id排序，如果按date排序的话，修改手机时间后，读取的短信就不准了
		if (cursor != null && cursor.getCount() > 0) {
			ContentValues values = new ContentValues();
			values.put("read", "1"); // 修改短信为已读模式
			cursor.moveToNext();
			int smsbodyColumn = cursor.getColumnIndex("body");
			String smsBody = cursor.getString(smsbodyColumn);
			//ToastUtils.show(context, smsBody);
			Message msg = new Message();
			msg.obj = smsBody;
			msg.what = SENDED_CAPTCHAS_MSG;
			if (handler != null) {
				handler.sendMessage(msg);
			}
		}

		// 在用managedQuery的时候，不能主动调用close()方法， 否则在Android 4.0+的系统上， 会发生崩溃
		if (Build.VERSION.SDK_INT < 14) {
			cursor.close();
		}
	}
}
