package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.ToastUtils;

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
				if (isAppOnForeground) {
					ToastUtils.show(context, R.string.Network_Mobile);
				}
				((MyApplication) context.getApplicationContext())
						.startWebSocket();
			} else if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
				if (isAppOnForeground) {
					ToastUtils.show(context, R.string.Network_WIFI);
				}
				((MyApplication) context.getApplicationContext())
						.startWebSocket();
			} else if (isAppOnForeground) {
				ToastUtils.show(context, R.string.network_exception);
			}
		} catch (Exception e) {
			// TODO: handle exception
			LogUtils.debug(TAG, e.toString());
		}

	}

}
