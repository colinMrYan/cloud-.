package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.ToastUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by yufuchang on 2019/1/2.
 */

public class NetWorkStateChangeUtils {

    public static final String NET_GPRS_STATE_OK = "net_gprs_state_ok";
    public static final String NET_WIFI_STATE_OK = "net_wifi_state_ok";
    public static final String NET_STATE_ERROR = "net_state_error";
    private static NetWorkStateChangeUtils netWorkStateChangeUtils;

    private NetWorkStateChangeUtils(){

    }

    public static NetWorkStateChangeUtils getInstance(){
        if(netWorkStateChangeUtils == null){
            synchronized (NetWorkStateChangeUtils.class){
                if(netWorkStateChangeUtils == null){
                    netWorkStateChangeUtils = new NetWorkStateChangeUtils();
                }
            }
        }
        return netWorkStateChangeUtils;
    }

    public void netWorkStateChange(){
        try {
            Context context = MyApplication.getInstance();
            ConnectivityManager conMan = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo.State mobile = conMan.getNetworkInfo(
                    ConnectivityManager.TYPE_MOBILE).getState();

            NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .getState();
            boolean isAppOnForeground = ((MyApplication)context.getApplicationContext()).getIsActive();
            if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                if (isAppOnForeground) {
                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG__NET_STATE_CHANGE,NET_GPRS_STATE_OK));
                    ToastUtils.show(context, R.string.Network_Mobile);
                    getBadgeFromServer(context);
                }
                WebSocketPush.getInstance().startWebSocket();
            } else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                if (isAppOnForeground) {
                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG__NET_STATE_CHANGE,NET_WIFI_STATE_OK));
                    ToastUtils.show(context, R.string.Network_WIFI);
                    getBadgeFromServer(context);
                }
                WebSocketPush.getInstance().startWebSocket();
            } else if (isAppOnForeground) {
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG__NET_STATE_CHANGE,NET_STATE_ERROR));
                ToastUtils.show(context, R.string.network_exception);
            }
        } catch (Exception e) {
            LogUtils.debug("NetWorkStateChangeUtils", e.getMessage());
        }
    }

    /**
     * 在已经登录，前台条件下，当断网重连时需要重新获取一遍角标
     */
    private void getBadgeFromServer(Context context) {
        if(MyApplication.getInstance().isHaveLogin()){
            new AppBadgeUtils(context).getAppBadgeCountFromServer();
        }
    }
}
