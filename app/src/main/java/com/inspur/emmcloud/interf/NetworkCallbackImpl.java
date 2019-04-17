package com.inspur.emmcloud.interf;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import com.inspur.emmcloud.util.privates.NetWorkStateChangeUtils;

/**
 * Created by yufuchang on 2019/1/2.
 */
@TargetApi(Build.VERSION_CODES.N)
public class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
    private Context context;

    public NetworkCallbackImpl(Context context) {
        this.context = context;
    }

    //如果在指定的超时时间（timeout时间）内找不到网络，则调用
    @Override
    public void onUnavailable() {
        super.onUnavailable();
        NetWorkStateChangeUtils.getInstance().netWorkStateChange();
    }

    //当框framework连接并已声明新网络可供使用时调用，在这之后总是会立即调用onCapabilitiesChanged， onLinkPropertiesChanged
    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        //  NetWorkStateChangeUtils.getInstance().netWorkStateChange();
    }

    //断开或重连时调用，maxMsToLive 应用将尝试保持网络连接的时间，此时网络随时可能断开
    @Override
    public void onLosing(Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
    }

    //当框架发生网络硬丢失或优雅的故障结束时调用。
    @Override
    public void onLost(Network network) {
        super.onLost(network);
        NetWorkStateChangeUtils.getInstance().netWorkStateChange();
    }

    //当此请求的框架连接到的网络更改功能，但仍满足所述需求时调用。
    @Override
    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        NetWorkStateChangeUtils.getInstance().netWorkStateChange();
    }

    //网络属性发生了改变
    @Override
    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties);
        //NetWorkStateChangeUtils.getInstance().netWorkStateChange();
    }
}
