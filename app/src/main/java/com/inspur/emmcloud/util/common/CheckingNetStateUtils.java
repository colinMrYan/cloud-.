package com.inspur.emmcloud.util.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/2/18.
 */

public class CheckingNetStateUtils {

    private Context context;

    private String[] urls;
    private String[] httpUrls;

    /**
     * 针对单个Url 检测通断的发送处理结果
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            PingUrlStateAction pingIdAndData = (PingUrlStateAction) msg.obj;
            LogUtils.LbcDebug("action:" + pingIdAndData.getAction() + "data" + pingIdAndData.isPingState());
            EventBus.getDefault().post(new SimpleEventMessage(pingIdAndData.getAction(), pingIdAndData));
            super.handleMessage(msg);
        }
    };
    private List<PingUrlAndConnectState> pingUrlAndConnectStates = new ArrayList<>();
    /**
     * 发送最终的网络异常状态
     */
    private Handler handlerNetHint = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            PingUrlStateAction pingUrlStateAction = (PingUrlStateAction) msg.obj;
            boolean ResultState = false;
            int netResult = getPingConnectedNet(pingUrlStateAction.getUrl(), pingUrlStateAction.isPingState());
            if (netResult >= 0) {
                if (netResult == 0) {
                    ResultState = false;
                } else if (netResult == 1) {
                    ResultState = true;
                }

                if (!NetUtils.isNetworkConnected(context, false)) {
                    ResultState = false;
                }
                EventBus.getDefault().post(new SimpleEventMessage(pingUrlStateAction.getAction(), ResultState));
            }

        }
    };

    public CheckingNetStateUtils(Context context) {
        this.context = context;
    }

    public CheckingNetStateUtils(Context context, String[] Urls, String[] httpUrls) {
        this.context = context;
        for (int i = 0; i < Urls.length; i++) {
            PingUrlAndConnectState pingUrlAndConnectState = new PingUrlAndConnectState(Urls[i]);
            pingUrlAndConnectStates.add(pingUrlAndConnectState);
        }
        for (int i = 0; i < httpUrls.length; i++) {
            PingUrlAndConnectState pingUrlAndConnectState = new PingUrlAndConnectState(httpUrls[i]);
            pingUrlAndConnectStates.add(pingUrlAndConnectState);
        }
        this.urls = Urls;
        this.httpUrls = httpUrls;
    }

    /**
     * 获取网络状态最终结果并显示于UI
     */
    public void getNetStateResult(int timeout) {
        final String action = Constant.EVENTBUS_TAG_NET_EXCEPTION_HINT;
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
                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_NET_EXCEPTION_HINT, true));
                }
            } else if ((wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) && NetUtils.isVpnConnected()) {
                if (isAppOnForeground) {
                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_NET_EXCEPTION_HINT, true));
                }
            } else if ((wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) && !NetUtils.isVpnConnected()) {
                clearUrlsStates();
                CheckNetPingAndHttpThreadStartForHint(urls, httpUrls, timeout, action, handlerNetHint);
            } else if (isAppOnForeground) {
                EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_NET_EXCEPTION_HINT, false));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Ping  网络通断状态检测（用于显示网络状态异常框）
     */
    public void CheckNetPingThreadStart(final String[] StrUrl, final int WaiteTime, final String eventBusAction) {
        for (int i = 0; i < StrUrl.length; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PingNetEntity pingNetEntity = new PingNetEntity(StrUrl[finalI], 1, WaiteTime, new StringBuffer());
                        pingNetEntity = NetUtils.ping(pingNetEntity, (long) WaiteTime);
                        PingUrlStateAction pingUrlStateAction = new PingUrlStateAction(eventBusAction, StrUrl[finalI], pingNetEntity.isResult());
                        Message message = new Message();
                        message.obj = pingUrlStateAction;
                        handler.sendMessage(message);
                        LogUtils.LbcDebug("Pinf发送网络EventBus事件:" + " Action" + eventBusAction + "URL" + StrUrl[finalI] + "状态" + pingNetEntity.isResult());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * Ping  网络通断状态检测（用于显示网络状态异常框）
     */
    public void CheckNetPingThreadStartForHint(final String[] StrUrl, final int WaiteTime, final String eventBusAction, final Handler handlerHint) {
        for (int i = 0; i < StrUrl.length; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PingNetEntity pingNetEntity = new PingNetEntity(StrUrl[finalI], 1, WaiteTime, new StringBuffer());
                        pingNetEntity = NetUtils.ping(pingNetEntity, (long) WaiteTime);
                        PingUrlStateAction pingUrlStateAction = new PingUrlStateAction(eventBusAction, StrUrl[finalI], pingNetEntity.isResult());
                        Message message = new Message();
                        message.obj = pingUrlStateAction;
                        handlerHint.sendMessage(message);
                        LogUtils.LbcDebug("Pinf发送网络EventBus事件:" + " Action" + eventBusAction + "URL" + StrUrl[finalI] + "状态" + pingNetEntity.isResult());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * Ping  网络通断状态检测（用于显示网络状态异常框）
     */
    public void CheckNetPingAndHttpThreadStartForHint(final String[] StrUrl, final String[] httpUrls, final int WaiteTime, final String eventBusAction, final Handler handlerHint) {
        for (int i = 0; i < StrUrl.length; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PingNetEntity pingNetEntity = new PingNetEntity(StrUrl[finalI], 1, WaiteTime, new StringBuffer());
                        pingNetEntity = NetUtils.ping(pingNetEntity, (long) WaiteTime);
                        PingUrlStateAction pingUrlStateAction = new PingUrlStateAction(eventBusAction, StrUrl[finalI], pingNetEntity.isResult());
                        Message message = new Message();
                        message.obj = pingUrlStateAction;
                        handlerHint.sendMessage(message);
                        LogUtils.LbcDebug("Pinf发送网络EventBus事件:" + " Action" + eventBusAction + "URL" + StrUrl[finalI] + "状态" + pingNetEntity.isResult());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        CheckNetHttpThreadStart(httpUrls);
    }

    /**
     * 通过Http 请求判断网络通断
     */
    public void CheckNetHttpThreadStart(final String[] StrUrl) {
        for (int i = 0; i < StrUrl.length; i++) {
            AppAPIService apiService = new AppAPIService(context);
            apiService.setAPIInterface(new WebHttpService());
            apiService.getCloudConnectStateUrl(StrUrl[i]);
        }
    }

    /**
     * 网络连接状态 如果GPRS连接或者Wifi&&VPN时返回true 否则返回false
     */
    public boolean isConnectedNet() {
        if (NetworkInfo.State.CONNECTED == NetUtils.getNetworkMobileState(context)
                || NetworkInfo.State.CONNECTING == NetUtils.getNetworkMobileState(context)
                || (NetworkInfo.State.CONNECTED == NetUtils.getNetworkWifiState(context) && NetUtils.isVpnConnected())) {
            return true;
        } else {
            return false;
        }
    }
    /***/


    /**
     * 多个Url 有一个连接即返回true
     */
    public int getPingConnectedNet(String Url, boolean connectedState) {
        int isFalse = 0;
        //检测1
        for (int i = 0; i < pingUrlAndConnectStates.size(); i++) {
            if (pingUrlAndConnectStates.get(i).getState() == 1) {
                return -1;
            }
        }
        //赋值
        for (int i = 0; i < pingUrlAndConnectStates.size(); i++) {
            if (Url.equals(pingUrlAndConnectStates.get(i).getUrl())) {
                int intConnectedState = connectedState ? 1 : 0;
                pingUrlAndConnectStates.get(i).setState(intConnectedState);
                if (intConnectedState == 1) {
                    return 1;
                }
            }
        }
        //求和，判定为0
        int sum = 0;
        for (int i = 0; i < pingUrlAndConnectStates.size(); i++) {
            sum = sum + pingUrlAndConnectStates.get(i).getState();
        }
        if (sum == 0) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * 清除原有Url 的状态值
     */
    public void clearUrlsStates() {
        for (int i = 0; i < pingUrlAndConnectStates.size(); i++) {
            pingUrlAndConnectStates.get(i).clearState();
        }
    }

    /**
     * Http 检测网路状态 回调
     */
    public class WebHttpService extends APIInterfaceInstance {
        @Override
        public void returnCheckCloudPluseConnectionSuccess(byte[] arg0, final String url) {
            CheckingNetStateUtils.PingUrlStateAction handlerUrlStateAction = new CheckingNetStateUtils.PingUrlStateAction(Constant.EVENTBUS_TAG_NET_EXCEPTION_HINT, url, true);
            Message message = new Message();
            message.obj = handlerUrlStateAction;
            handlerNetHint.sendMessage(message);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    LogUtils.LbcDebug("http 返回成功" + url);
                    CheckingNetStateUtils.PingUrlStateAction pingUrlStateAction = new CheckingNetStateUtils.PingUrlStateAction("", url, true);
                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_NET_HTTP_POST_CONNECTION, pingUrlStateAction));
                }
            });
        }

        @Override
        public void returnCheckCloudPluseConnectionError(String error, int responseCode, final String url) {
            CheckingNetStateUtils.PingUrlStateAction handlerUrlStateAction = new CheckingNetStateUtils.PingUrlStateAction(Constant.EVENTBUS_TAG_NET_EXCEPTION_HINT, url, false);
            Message message = new Message();
            message.obj = handlerUrlStateAction;
            handlerNetHint.sendMessage(message);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    LogUtils.LbcDebug("http 返回失败");
                    CheckingNetStateUtils.PingUrlStateAction pingUrlStateAction = new CheckingNetStateUtils.PingUrlStateAction("", url, false);
                    EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_NET_HTTP_POST_CONNECTION, pingUrlStateAction));
                }
            });
        }
    }

    /**
     * 包含Url  和该Url 的通断状态
     */
    public class PingUrlAndConnectState {
        private String url = "";
        private int state = -1; //-1 未设置 0 为false 1为true

        public PingUrlAndConnectState(String Url) {
            this.url = Url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public void clearState() {
            state = -1;
        }
    }

    /**
     * 包含Url  、该Url 的通断状态、Action
     */
    public class PingUrlStateAction {
        private String url;
        private String action;
        private boolean pingState;

        public PingUrlStateAction() {
            this.url = "";
            this.action = "";
            this.pingState = false;
        }

        public PingUrlStateAction(String Action, String Url, Boolean PingState) {
            this.url = Url;
            this.action = Action;
            this.pingState = PingState;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public boolean isPingState() {
            return pingState;
        }

        public void setPingState(boolean pingState) {
            this.pingState = pingState;
        }

    }

}
