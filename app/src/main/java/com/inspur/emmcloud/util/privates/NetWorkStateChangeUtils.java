package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.AppBadgeUtils;
import com.inspur.emmcloud.basemodule.util.CheckingNetStateUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.push.WebSocketPush;

/**
 * Created by yufuchang on 2019/1/2.
 */

public class NetWorkStateChangeUtils {


    private static NetWorkStateChangeUtils netWorkStateChangeUtils;
    private CheckingNetStateUtils checkingNetStateUtils = new CheckingNetStateUtils(MyApplication.getInstance(), NetUtils.pingUrls, (new NetUtils()).getHttpUrls());
    private static boolean isColdReboot = true;

    private NetWorkStateChangeUtils() {

    }

    public static NetWorkStateChangeUtils getInstance() {
        if (netWorkStateChangeUtils == null) {
            synchronized (NetWorkStateChangeUtils.class) {
                if (netWorkStateChangeUtils == null) {
                    netWorkStateChangeUtils = new NetWorkStateChangeUtils();
                }
            }
        }
        return netWorkStateChangeUtils;
    }

    public void netWorkStateChange() {
        try {
            boolean isAppOnForeground = MyApplication.getInstance().getIsActive();
            if (isAppOnForeground) {
                boolean isConnected = false;
                ConnectivityManager connectivity = (ConnectivityManager) MyApplication.getInstance()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivity != null) {
                    NetworkInfo[] info = connectivity.getAllNetworkInfo();
                    if (info != null) {
                        for (NetworkInfo networkInfo : info) {
                            if (networkInfo.getState() == NetworkInfo.State.CONNECTED || networkInfo.getState() == NetworkInfo.State.CONNECTING) {
                                isConnected = true;
                                break;
                            }
                        }
                    }
                }
                if (isConnected) {
                    getBadgeFromServer(MyApplication.getInstance());
                    WebSocketPush.getInstance().startWebSocket();
                }
                checkingNetStateUtils = new CheckingNetStateUtils(MyApplication.getInstance(), NetUtils.pingUrls, (new NetUtils()).getHttpUrls());
                checkingNetStateUtils.getNetStateResult(5);
                if (!isColdReboot) {
                    ToastUtils.show(Res.getString("network_change_tip") + checkingNetStateUtils.getNetworksType());
                }
                isColdReboot = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在已经登录，前台条件下，当断网重连时需要重新获取一遍角标
     */
    private void getBadgeFromServer(Context context) {
        if (MyApplication.getInstance().isHaveLogin()) {
            new AppBadgeUtils(context).getAppBadgeCountFromServer(false);
        }
    }
}
