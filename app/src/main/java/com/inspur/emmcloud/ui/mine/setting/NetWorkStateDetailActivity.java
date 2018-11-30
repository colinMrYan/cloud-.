package com.inspur.emmcloud.ui.mine.setting;

import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PingNetEntity;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.qmuiteam.qmui.widget.QMUILoadingView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2018/11/8.
 * try to show the netWork state and details such as net delay/connect State  and so on by PING
 */

public class NetWorkStateDetailActivity extends BaseActivity {
   public static final int SHOW_DNSCONNCTSTATE=2;
   public static final int SHOW_PORTAL_CONNECT=1;
   public static  String []  subUrls  = {"www.baidu.com","www.inspur.com","www.aliyun.com"};
    private String PortalCheckingUrls  = "http://www.inspuronline.com/#/auth/0\\(arc4random() % 100000)";
   private ImageView hardImageView;
   private ImageView portalImageView;
   private QMUILoadingView qmulHardLoadingView ;
   private QMUILoadingView qmulWifiLoadingView ;
   private Drawable drawableError;
   private Drawable drawableSuccess;
   private Drawable drawableDomainError;
   private Drawable drawableDomainSuccess;
   private List<Integer> NetStateintegerData;
   private String   PortalUrl = "";
   private boolean  netHardConnectState =true;
   private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_state_detail);
        iniView();
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
     * */
    void iniView(){
        hardImageView = (ImageView)findViewById(R.id.iv_hard_state_log);
        portalImageView= (ImageView)findViewById(R.id.iv_portal_state_log);
        drawableError=getBaseContext().getResources().getDrawable(R.drawable.ic_netchecking_error);
        drawableSuccess=getBaseContext().getResources().getDrawable(R.drawable.ic_netchecking_ok);
        drawableDomainError = getBaseContext().getResources().getDrawable(R.drawable.ic_checking_domain_error);
        drawableDomainSuccess = getBaseContext().getResources().getDrawable(R.drawable.ic_checking_domain_success);
        qmulHardLoadingView =(QMUILoadingView)findViewById(R.id.qv_checking_hard_loading);
        qmulWifiLoadingView =(QMUILoadingView)findViewById(R.id.qv_checking_portal_loading);
        HandMessage();
    }

    /**
     * 网络状态检查 包含 硬件、小助手(portal)、网络连通性
     * */
    void checkingNet(){
        //检测网络通断
        netHardConnectState= checkingHardState();
        //检测端口
        if(netHardConnectState){
            //检测小助手
            checkingPortalState(PortalCheckingUrls);
            //检测DNS服务
            checkingDNSConnectState();
        }else {
            qmulWifiLoadingView.setVisibility(View.GONE);
            portalImageView.setVisibility(View.VISIBLE);
            portalImageView.setBackground(drawableError);
            findViewById(R.id.iv_ping_baidu_state).setBackground(drawableDomainError);
            findViewById(R.id.iv_ping_inspur_state).setBackground(drawableDomainError);
            findViewById(R.id.iv_ping_ali_state).setBackground(drawableDomainError);
            setShowDnsconnctstateUI(false);
        }
    }

    /**
     * 处理不同Net链接状态的UI显示
     * */
    void HandMessage() {
        handler=new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case SHOW_PORTAL_CONNECT:
                        if((boolean)msg.obj) {
                            qmulWifiLoadingView.setVisibility(View.GONE);
                            portalImageView.setBackground(drawableSuccess);
                            portalImageView.setVisibility(View.VISIBLE);
                        } else {
                                qmulWifiLoadingView.setVisibility(View.GONE);
                                portalImageView.setBackground(drawableError);
                                portalImageView.setVisibility(View.VISIBLE);
                            }
                        break;
                    case SHOW_DNSCONNCTSTATE:
                         List<Boolean>  resultData = (List<Boolean>)msg.obj;
                        findViewById(R.id.iv_ping_baidu_state).setBackground(resultData.get(0)?drawableDomainSuccess:drawableDomainError);
                        findViewById(R.id.iv_ping_inspur_state).setBackground(resultData.get(1)?drawableDomainSuccess:drawableDomainError);
                        findViewById(R.id.iv_ping_ali_state).setBackground(resultData.get(2)?drawableDomainSuccess:drawableDomainError);
                        setShowDnsconnctstateUI(false);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 检测硬件连接问题
     * @return  反馈为硬件连接状态
     * */
    private boolean checkingHardState() {
        NetStateintegerData=NetUtils.getNetWrokState(this);
        if(-1==NetStateintegerData.get(0)) {
            hardImageView.setBackground(drawableError);
            qmulHardLoadingView.setVisibility(View.GONE);
            hardImageView.setVisibility(View.VISIBLE);
            return false;
        }else {
            hardImageView.setBackground(drawableSuccess);
            qmulHardLoadingView.setVisibility(View.GONE);
            hardImageView.setVisibility(View.VISIBLE);
            return true;
        }
    }

    /**
     * 检测小助手连接
     * @param StrUrl 测试小助手url(设定为百度)
     * */
    private void checkingPortalState(final String StrUrl) {
                sendRequest(StrUrl);
    }

    /**
     * 检测DNS服务器状态
     * */
    private  void checkingDNSConnectState() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Boolean> resultState=new ArrayList<>();
                    for (int i=0;i<subUrls.length;i++) {
                        PingNetEntity checkUrlEntity =new PingNetEntity(subUrls[i],1,5,new StringBuffer());
                        PingNetEntity checkResult= NetUtils.ping(checkUrlEntity, (long) 4500);
                        resultState.add(checkResult.isResult());
                    }
                    Message dnsState = new Message();
                    dnsState.what=SHOW_DNSCONNCTSTATE;
                    dnsState.obj=resultState;
                    handler.sendMessage(dnsState);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 加载ViewGONE，改为状态view VISIABLE
     * @param iniState true 初始化时loading显示状态imageview消失，false 相反
     * */
    private void setShowDnsconnctstateUI(Boolean iniState){
        hardImageView.setVisibility(iniState?View.GONE:View.VISIBLE);
        qmulHardLoadingView.setVisibility(iniState?View.VISIBLE:View.GONE);
        portalImageView.setVisibility(iniState?View.GONE:View.VISIBLE);
        qmulWifiLoadingView.setVisibility(iniState?View.VISIBLE:View.GONE);

        findViewById(R.id.iv_ping_baidu_state).setVisibility(iniState?View.GONE:View.VISIBLE);
        findViewById(R.id.qv_ping_baidu_loading).setVisibility(iniState?View.VISIBLE:View.GONE);
        findViewById(R.id.iv_ping_inspur_state).setVisibility(iniState?View.GONE:View.VISIBLE);
        findViewById(R.id.qv_ping_inspur_loading).setVisibility(iniState?View.VISIBLE:View.GONE);
        findViewById(R.id.iv_ping_ali_state).setVisibility(iniState?View.GONE:View.VISIBLE);
        findViewById(R.id.qv_ping_ali_loading).setVisibility(iniState?View.VISIBLE:View.GONE);
    }

    /**
     * portal checking  "http://www.inspuronline.com/#/auth/0\(arc4random() % 100000)"
     * 检测小助手不仅要ping状态还有读取返回内容，故以百度为目标网址（有小助手会有网络劫持现象 即反馈302）
     * @param StrUrl
     * */
    private void sendRequest(final  String  StrUrl) {
        final NetworkInfo.State wifiConnection  =  NetUtils.getNetworkWifiState(getBaseContext());
        new Thread(){
            public void run() {
                HttpURLConnection httpURLConnection=null;
                boolean resultData = false;
                try {
                    PingNetEntity prePingPortal = new PingNetEntity(subUrls[0],1,5,new StringBuffer());
                    PingNetEntity pingPortalState  = NetUtils.ping(prePingPortal,(long)2000);
                    if(pingPortalState.isResult()) {
                        resultData = true ;
                    } else {
                        if (wifiConnection ==NetworkInfo.State.CONNECTED) {
                            URL url=new URL(StrUrl);
                            httpURLConnection=(HttpURLConnection) url.openConnection();//获取到httpURLConnection的实例
                            httpURLConnection.setInstanceFollowRedirects(false);
                            httpURLConnection.setRequestMethod("POST");//设置HTTP请求所使用的方法，GET表示希望从服务器那里获取数据，而POST则表示希望提交数据给服务器
                            httpURLConnection.setReadTimeout(10000);//设置读取超时的毫秒数
                            int responcode = httpURLConnection.getResponseCode();
                            if(responcode>300&&responcode<310){
                               PortalUrl= httpURLConnection.getURL().toString();//路径复制
                            }
                            resultData =false;
                        }
                    }
                    Message msg=new Message();
                    msg.what=SHOW_PORTAL_CONNECT;
                    msg.obj = resultData;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Message msg=new Message();
                    msg.what=SHOW_PORTAL_CONNECT;
                    msg.obj = false;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }finally{
                    if(httpURLConnection!=null){
                        httpURLConnection.disconnect();//将HTTP连接关闭掉
                    }
                }
            }
        }.start();
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_back_net_detail:
                finish();
                break;
            case R.id.rl_net_error_fix:
                IntentUtils.startActivity(this,NetHardConnectCheckActivity.class);
                break;
            case R.id.rl_checking_portal_state:
                String activityName = getResources().getString(R.string.net_network_authentication);
                if(PortalUrl!=null&&PortalUrl!=""){
                    UriUtils.openUrl(this,PortalUrl,activityName);
                }
                break;
            case R.id.rl_checking_dns_state:
                break;
                default:
                break;
        }
    }

}
