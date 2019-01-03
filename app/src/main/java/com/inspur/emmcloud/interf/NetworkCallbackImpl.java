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
    public NetworkCallbackImpl(Context context){
        this.context = context;
    }

    //网络不可用
    @Override
    public void onUnavailable() {
        super.onUnavailable();
        new NetWorkStateChangeUtils().netWorkStateChange(context);
    }

    //网络可用
    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        new NetWorkStateChangeUtils().netWorkStateChange(context);
    }

    //网络即将断开时调用 maxMsToLive 应用将尝试保持网络连接的时间，此时网络随时可能断开
    @Override
    public void onLosing(Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
    }

    //网络已经断开
    @Override
    public void onLost(Network network) {
        super.onLost(network);
        new NetWorkStateChangeUtils().netWorkStateChange(context);
    }

    //网络发生了改变，但仍然可用 NetWorkCapabilities 这个网络的新功能
    @Override
    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        new NetWorkStateChangeUtils().netWorkStateChange(context);
    }

    //网络属性发生了改变
    @Override
    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties);
        new NetWorkStateChangeUtils().netWorkStateChange(context);
    }
}
