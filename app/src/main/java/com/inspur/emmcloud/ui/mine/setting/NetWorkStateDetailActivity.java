package com.inspur.emmcloud.ui.mine.setting;

import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.PingNetEntity;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.CustomLoadingView;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.util.privates.CheckingNetStateUtils;
import com.inspur.emmcloud.util.privates.UriUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by libaochao on 2018/11/8.
 * try to show the netWork state and details such as net delay/connect State  and so on by PING
 */
@Route(path = "/app/networkDetail")
public class NetWorkStateDetailActivity extends BaseActivity {
    public static String[] subUrls = {"www.baidu.com", "www.aliyun.com"};
    CheckingNetStateUtils checkingNetStateUtils;
    private String PortalCheckingUrls = NetUtils.httpUrls[0];
    private String[] CheckHttpUrls = NetUtils.httpUrls;
    private ImageView hardImageView;
    private ImageView portalImageView;
    private ImageView ping1UrlImageView;
    private ImageView ping2UrlImageView;
    private ImageView ping3UrlImageView;
    private RelativeLayout checkPortalLayout;
    private RelativeLayout checkUrlsConnectionLayout;
    private RelativeLayout portalCheckTipLayout;
    private CustomLoadingView qmulHardLoadingView;
    private CustomLoadingView qmulWifiLoadingView;
    private CustomLoadingView ping1UrlLoadingView;
    private CustomLoadingView ping2UrlLoadingView;
    private CustomLoadingView ping3UrlLoadingView;
    private Drawable drawableError;
    private Drawable drawableSuccess;
    private Drawable drawableRightArrow;
    private Drawable drawableDomainError;
    private Drawable drawableDomainSuccess;
    private String PortalUrl = "";
    private boolean netHardConnectState = true;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate() {
        checkingNetStateUtils = new CheckingNetStateUtils(this);
        iniView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_network_state_detail;
    }

    @Override
    protected void onResume() {
        super.onResume();
        PortalUrl = "";
        setShowDnsconnctstateUI(true);
        checkingNet();
    }

    /**
     * 初始化View
     */
    void iniView() {
        hardImageView = findViewById(R.id.iv_hard_state_log);
        portalImageView = findViewById(R.id.iv_portal_state_log);
        drawableError = getBaseContext().getResources().getDrawable(R.drawable.ic_netchecking_error);
        drawableRightArrow = getBaseContext().getResources().getDrawable(R.drawable.ic_fix_left_arrow);
        drawableSuccess = getBaseContext().getResources().getDrawable(R.drawable.ic_netchecking_ok);
        drawableDomainError = getBaseContext().getResources().getDrawable(R.drawable.ic_checking_domain_error);
        drawableDomainSuccess = getBaseContext().getResources().getDrawable(R.drawable.ic_checking_domain_success);
        qmulHardLoadingView = findViewById(R.id.qv_checking_hard_loading);
        qmulWifiLoadingView = findViewById(R.id.qv_checking_portal_loading);
        ping1UrlImageView = findViewById(R.id.iv_ping_baidu_state);
        ping2UrlImageView = findViewById(R.id.iv_ping_inspur_state);
        ping3UrlImageView = findViewById(R.id.iv_ping_ali_state);
        ping1UrlLoadingView = findViewById(R.id.qv_ping_baidu_loading);
        ping2UrlLoadingView = findViewById(R.id.qv_ping_inspur_loading);
        ping3UrlLoadingView = findViewById(R.id.qv_ping_ali_loading);
        checkPortalLayout = findViewById(R.id.rl_checking_portal_state);
        checkUrlsConnectionLayout = findViewById(R.id.rl_checking_dns_state);
        portalCheckTipLayout = findViewById(R.id.rl_portal_tip);
    }

    /**
     * 网络状态检查 包含 硬件、小助手(portal)、网络连通性
     */
    void checkingNet() {
        //检测网络通断
        netHardConnectState = checkingHardState();
        if (netHardConnectState) {
            //检测小助手 判断是否有wifi连接
            if (NetworkInfo.State.CONNECTED == NetUtils.getNetworkWifiState(this)) {
                checkPortalLayout.setVisibility(View.VISIBLE);
                checkingPortalState(PortalCheckingUrls);
            } else {
                checkPortalLayout.setVisibility(View.GONE);
            }
            if (NetUtils.isVpnConnected()) {
                checkUrlsConnectionLayout.setVisibility(View.GONE);
            } else {
                checkUrlsConnectionLayout.setVisibility(View.VISIBLE);
            }
            checkingNetConnectState();
        } else {
            checkUrlsConnectionLayout.setVisibility(View.GONE);
            checkPortalLayout.setVisibility(View.GONE);
            qmulWifiLoadingView.setVisibility(View.GONE);
            portalImageView.setVisibility(View.VISIBLE);
            portalImageView.setBackground(drawableError);
            ping1UrlImageView.setBackground(drawableDomainError);
            ping2UrlImageView.setBackground(drawableDomainError);
            ping3UrlImageView.setBackground(drawableDomainError);
            setShowDnsconnctstateUI(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 检测硬件连接问题
     *
     * @return 反馈为硬件连接状态
     */
    private boolean checkingHardState() {
        if (!NetUtils.isNetworkConnected(this, false)) {
            hardImageView.setVisibility(View.GONE);
            qmulHardLoadingView.setVisibility(View.GONE);
            findViewById(R.id.rl_to_fix).setVisibility(View.VISIBLE);
            findViewById(R.id.rl_net_error_fix).setClickable(true);
            return false;
        } else {
            hardImageView.setBackground(drawableSuccess);
            qmulHardLoadingView.setVisibility(View.GONE);
            hardImageView.setVisibility(View.VISIBLE);
            findViewById(R.id.rl_to_fix).setVisibility(View.GONE);
            findViewById(R.id.rl_net_error_fix).setClickable(false);
            return true;
        }
    }

    /**
     * 检测小助手连接
     *
     * @param StrUrl 测试小助手url(设定为百度)
     */
    private void checkingPortalState(final String StrUrl) {
        sendRequest(StrUrl);
    }

    /**
     * 通过个Url检测网络状态
     */
    private void checkingNetConnectState() {
        checkingNetStateUtils.CheckNetPingThreadStart(subUrls, 5, Constant.EVENTBUS_TAG_NET_PING_CONNECTION);
        checkingNetStateUtils.CheckNetHttpThreadStart(CheckHttpUrls);
    }

    /**
     * 加载View GONE，改为状态view VISIABLE
     *
     * @param iniState true 初始化时loading显示状态imageview消失，false 相反
     */
    private void setShowDnsconnctstateUI(Boolean iniState) {
        qmulHardLoadingView.setVisibility(iniState ? View.VISIBLE : View.GONE);
        portalImageView.setVisibility(iniState ? View.GONE : View.VISIBLE);
        qmulWifiLoadingView.setVisibility(iniState ? View.VISIBLE : View.GONE);

        ping1UrlImageView.setVisibility(iniState ? View.GONE : View.VISIBLE);
        ping1UrlLoadingView.setVisibility(iniState ? View.VISIBLE : View.GONE);
        ping2UrlImageView.setVisibility(iniState ? View.GONE : View.VISIBLE);
        ping2UrlLoadingView.setVisibility(iniState ? View.VISIBLE : View.GONE);
        ping3UrlImageView.setVisibility(iniState ? View.GONE : View.VISIBLE);
        ping3UrlLoadingView.setVisibility(iniState ? View.VISIBLE : View.GONE);
        portalCheckTipLayout.setVisibility(View.GONE);
    }

    /**
     * portal checking
     * 检测小助手不仅要ping状态还有读取返回内容，故以百度为目标网址（有小助手会有网络劫持现象 即反馈302）
     *
     * @param StrUrl
     */
    private void sendRequest(final String StrUrl) {
        final NetworkInfo.State wifiConnection = NetUtils.getNetworkWifiState(getBaseContext());
        new Thread() {
            public void run() {
                HttpURLConnection httpURLConnection = null;
                boolean isConnected = false;
                try {
                    PingNetEntity prePingPortal = new PingNetEntity(subUrls[0], 1, 5, new StringBuffer());
                    PingNetEntity pingPortalState = NetUtils.ping(prePingPortal, (long) 2000);
                    if (pingPortalState.isResult()) {
                        isConnected = true;
                    } else {
                        if (wifiConnection == NetworkInfo.State.CONNECTED && !NetUtils.isVpnConnected()) {
                            URL url = new URL(StrUrl);
                            httpURLConnection = (HttpURLConnection) url.openConnection();
                            httpURLConnection.setInstanceFollowRedirects(false);
                            httpURLConnection.setRequestMethod("POST");
                            httpURLConnection.setReadTimeout(10000);
                            int responcode = httpURLConnection.getResponseCode();
                            if ((responcode >= 200) && (responcode < 300)) {
                                isConnected = true;
                            } else if (302 == responcode) {
                                PortalUrl = httpURLConnection.getURL().toString();
                                isConnected = false;
                            } else if (301 == responcode) {
                                isConnected = true;
                            } else {
                                isConnected = false;
                            }
                        } else {
                            isConnected = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    final boolean finalIsConnected = isConnected;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_NET_PORTAL_HTTP_POST, finalIsConnected));
                        }
                    });
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();//将HTTP连接关闭掉
                    }
                }
            }
        }.start();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_net_error_fix:
                IntentUtils.startActivity(this, NetHardConnectCheckActivity.class);
                break;
            case R.id.rl_portal_tip:
                String activityName = getResources().getString(R.string.net_network_authentication);
                if (PortalUrl != null && PortalUrl != "") {
                    UriUtils.openUrl(this, PortalUrl, activityName,true);
                }
                break;
            case R.id.rl_checking_dns_state:
                break;
            default:
                break;
        }
    }

    /**
     * 沟通页网络异常提示框
     *
     * @param netState 通过Action获取操作类型
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dealCheckingPingUrls(SimpleEventMessage netState) {
        if (netState.getAction().equals(Constant.EVENTBUS_TAG_NET_PING_CONNECTION)) {
            CheckingNetStateUtils.PingUrlStateAction idAndData = (CheckingNetStateUtils.PingUrlStateAction) netState.getMessageObj();
            if (idAndData.getUrl().equals(subUrls[0])) {
                ping1UrlImageView.setBackground(idAndData.isPingState() ? drawableDomainSuccess : drawableDomainError);
                ping1UrlImageView.setVisibility(View.VISIBLE);
                ping1UrlLoadingView.setVisibility(View.GONE);
            }
            if (idAndData.getUrl().equals(subUrls[1])) {
                ping3UrlImageView.setBackground(idAndData.isPingState() ? drawableDomainSuccess : drawableDomainError);
                ping3UrlImageView.setVisibility(View.VISIBLE);
                ping3UrlLoadingView.setVisibility(View.GONE);
            }
        } else if (netState.getAction().equals(Constant.EVENTBUS_TAG_NET_PORTAL_HTTP_POST)) {
            if ((boolean) netState.getMessageObj()) {
                qmulWifiLoadingView.setVisibility(View.GONE);
                portalImageView.setBackground(drawableSuccess);
                portalImageView.setVisibility(View.VISIBLE);
                portalCheckTipLayout.setVisibility(View.GONE);
            } else {
                qmulWifiLoadingView.setVisibility(View.GONE);
                portalImageView.setVisibility(View.VISIBLE);
                if (StringUtils.isBlank(PortalUrl)) {
                    portalImageView.setBackground(drawableError);
                    portalCheckTipLayout.setVisibility(View.GONE);
                } else {
                    portalImageView.setBackground(drawableRightArrow);
                    portalCheckTipLayout.setVisibility(View.VISIBLE);
                }
            }
        } else if (netState.getAction().equals(Constant.EVENTBUS_TAG_NET_HTTP_POST_CONNECTION)) {
            CheckingNetStateUtils.PingUrlStateAction idAndData = (CheckingNetStateUtils.PingUrlStateAction) netState.getMessageObj();
            if (idAndData.getUrl().equals(CheckHttpUrls[0])) {
                ping2UrlImageView.setBackground(idAndData.isPingState() ? drawableDomainSuccess : drawableDomainError);
                ping2UrlImageView.setVisibility(View.VISIBLE);
                ping2UrlLoadingView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != handler) {
            handler = null;
        }
        EventBus.getDefault().unregister(this);
    }

}
