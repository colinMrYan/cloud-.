package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.common.CheckingNetStateUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;

/**
 * Created by yufuchang on 2019/1/2.
 */

public class NetWorkStateChangeUtils {


    private static NetWorkStateChangeUtils netWorkStateChangeUtils;
    private CheckingNetStateUtils checkingNetStateUtils = new CheckingNetStateUtils(MyApplication.getInstance(), NetUtils.pingUrls);
    ;

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
            Context context = MyApplication.getInstance();
            ConnectivityManager conMan = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo.State mobile = conMan.getNetworkInfo(
                    ConnectivityManager.TYPE_MOBILE).getState();

            NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .getState();
            boolean isAppOnForeground = ((MyApplication) context.getApplicationContext()).getIsActive();
            if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                if (isAppOnForeground) {
                    getBadgeFromServer(context);
                }
                WebSocketPush.getInstance().startWebSocket();
            } else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                if (isAppOnForeground) {
                    getBadgeFromServer(context);
                }
                WebSocketPush.getInstance().startWebSocket();
            } else if (isAppOnForeground) {
            }
            if (isAppOnForeground) {
                checkingNetStateUtils.getNetStateResult(5);
            }
        } catch (Exception e) {
            LogUtils.debug("NetWorkStateChangeUtils", e.getMessage());
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
