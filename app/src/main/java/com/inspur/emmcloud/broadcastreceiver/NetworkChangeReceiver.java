package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.ToastUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 
 * 监控网络的变化，并给出相应的提示
 * 
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

	private static final String TAG = "NetworkChangeReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		ConnectivityManager conMan = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			State mobile = conMan.getNetworkInfo(
					ConnectivityManager.TYPE_MOBILE).getState();
			State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
					.getState();
			boolean isAppOnForeground = ((MyApplication)context.getApplicationContext()).getIsActive();
			if (mobile == State.CONNECTED || mobile == State.CONNECTING) {
				EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG__NET_STATE_CHANGE,"event_tag_net_state_ok"));
				if (isAppOnForeground) {
					ToastUtils.show(context, R.string.Network_Mobile);
				}
				WebSocketPush.getInstance().startWebSocket();
			} else if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
				EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG__NET_STATE_CHANGE,"event_tag_net_state_change"));

				if (isAppOnForeground) {
					ToastUtils.show(context, R.string.Network_WIFI);
				}
				WebSocketPush.getInstance().startWebSocket();
			} else if (isAppOnForeground) {
				EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG__NET_STATE_CHANGE,"event_tag_net_state_error"));
				ToastUtils.show(context, R.string.network_exception);
			}
		} catch (Exception e) {
			// TODO: handle exception
			LogUtils.debug(TAG, e.toString());
		}
	}
}
