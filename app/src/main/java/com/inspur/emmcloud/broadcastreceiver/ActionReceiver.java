package com.inspur.emmcloud.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.basemodule.util.AppUtils;

/**
 * 在华为mate9，系统8.0.0上测试
 * 广播接收器会接收到广播信息
 * 监听的广播有屏幕点亮和熄灭（未接收到）
 * 网络改变  可以监听
 * 应用安装和卸载  可以监听
 * 屏幕解锁  可以监听
 * 开机广播 未监听到
 * 闹钟广播 没确定怎么用
 */
public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppUtils.judgeAndStartPush(context);
    }

}
