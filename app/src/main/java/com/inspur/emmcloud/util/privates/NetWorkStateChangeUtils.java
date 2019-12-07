package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.basemodule.util.CheckingNetStateUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.push.WebSocketPush;

/**
 * Created by yufuchang on 2019/1/2.
 */

public class NetWorkStateChangeUtils {


    private static NetWorkStateChangeUtils netWorkStateChangeUtils;
    private CheckingNetStateUtils checkingNetStateUtils = new CheckingNetStateUtils(MyApplication.getInstance(), NetUtils.pingUrls, NetUtils.httpUrls);


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
            if (isAppOnForeground){
                boolean isConnected = false;
                ConnectivityManager connectivity = (ConnectivityManager) MyApplication.getInstance()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivity != null) {
                    NetworkInfo[] info = connectivity.getAllNetworkInfo();
                    if (info != null) {
                        for (int i = 0; i < info.length; i++) {
                            if (info[i].getState() == NetworkInfo.State.CONNECTED || info[i].getState() == NetworkInfo.State.CONNECTING) {
                                isConnected = true;
                                break;
                            }
                        }
                    }
                }

                if (isConnected){
                    getBadgeFromServer(MyApplication.getInstance());
                    WebSocketPush.getInstance().startWebSocket();
                }
                checkingNetStateUtils.getNetStateResult(5);
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
            new AppBadgeUtils(context).getAppBadgeCountFromServer();
        }
    }
}
