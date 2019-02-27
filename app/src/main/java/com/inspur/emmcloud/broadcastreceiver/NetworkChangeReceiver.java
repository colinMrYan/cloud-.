package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.util.privates.NetWorkStateChangeUtils;

/**
 * 监控网络的变化，并给出相应的提示
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NetWorkStateChangeUtils.getInstance().netWorkStateChange();
    }
}
