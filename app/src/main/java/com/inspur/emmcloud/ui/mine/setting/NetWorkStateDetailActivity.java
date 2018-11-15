package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PingNetEntity;
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

    ImageView hardImageView;
    ImageView portalImageView;
    ImageView dnsImageView;

    List<Integer> NetStateintegerData;
    String   PortalUrl = "";
    boolean  netHardConnectState =true;
    Drawable drawableError;
    Drawable drawableSuccess;
    QMUILoadingView qmulHardLoadingView ;
    QMUILoadingView qmulWifiLoadingView ;
    QMUILoadingView qmulDnsLoadingView;
    public static final int SHOW_DNSCONNCTSTATE=2;
    public static final int SHOW_RESPONSE=1;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_state_detail);
        iniView();
        checkingNet();
    }

    /**
     * 初始化View
     * */
    void iniView(){
        hardImageView = (ImageView)findViewById(R.id.iv_hard_state_log);
        portalImageView= (ImageView)findViewById(R.id.iv_portal_state_log);
        dnsImageView   = (ImageView)findViewById(R.id.iv_dns_state_log);
        drawableError=getBaseContext().getResources().getDrawable(R.drawable.ic_netchecking_error);
        drawableSuccess=getBaseContext().getResources().getDrawable(R.drawable.ic_netchecking_ok);
        qmulHardLoadingView =(QMUILoadingView)findViewById(R.id.qv_checking_hard_loading);
        qmulWifiLoadingView =(QMUILoadingView)findViewById(R.id.qv_checking_portal_loading);
        qmulDnsLoadingView =(QMUILoadingView)findViewById(R.id.qv_checking_dns_loading);
        HandMessage();
    }

    /**
     * 网络状态检查
     * */
    void checkingNet(){
        //检测网络通断
        netHardConnectState= checkingHardState();
        //检测端口
        if(netHardConnectState){
            //检测小助手
            checkingPortalState();
            //检测DNS服务
            LogUtils.LbcDebug("dddddddddddddddddddddddddddddd");
            DNSConnectState("","","","");
        }else {
            qmulDnsLoadingView.setVisibility(View.GONE);
            qmulWifiLoadingView.setVisibility(View.GONE);
            portalImageView.setVisibility(View.VISIBLE);
            dnsImageView.setVisibility(View.VISIBLE);
            portalImageView.setBackground(drawableError);
            dnsImageView.setBackground(drawableError);
        }
    }

    /**
     * 处理不同Net链接状态的UI显示
     * */
    void HandMessage() {
        handler=new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case SHOW_RESPONSE:
                        if(1==NetStateintegerData.get(0)) {
                            List<String> bundleata =(List<String>)msg.obj;
                            String httpResNum = bundleata.get(0);
                            String content=bundleata.get(1);
                            if((-1!=content.indexOf("&firsturl"))||(-1!=httpResNum.indexOf("NETWORK 302"))){
                                PortalUrl = content.substring(0,content.indexOf("&firsturl"));
                                portalImageView.setBackground(drawableError);
                            }else {
                                portalImageView.setBackground(drawableSuccess);
                            }
                            portalImageView.setVisibility(View.VISIBLE);
                        } else {
                            portalImageView.setBackground(drawableError);
                        }
                        portalImageView.setVisibility(View.VISIBLE);
                        qmulWifiLoadingView.setVisibility(View.GONE);
                        break;
                    case SHOW_DNSCONNCTSTATE:
                        LogUtils.LbcDebug("data111111111111111111111111111");
                            if((boolean)msg.obj){
                                dnsImageView.setBackground(drawableSuccess);
                            } else {
                                dnsImageView.setBackground(drawableError);
                            }
                            dnsImageView.setVisibility(View.VISIBLE);
                        qmulDnsLoadingView.setVisibility(View.GONE);
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
     * */
    private boolean checkingHardState() {
        NetStateintegerData=NetUtils.getNetWrokState(this);
        if(-1==NetStateintegerData.get(0)){
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
     * */
    private void checkingPortalState(){
        if(1==NetStateintegerData.get(0)){
            sendRequest();
        } else if((NetStateintegerData.get(0)>1)&&(NetStateintegerData.get(0)<5)) {
            qmulWifiLoadingView.setVisibility(View.GONE);
            portalImageView.setBackground(drawableSuccess);
            portalImageView.setVisibility(View.VISIBLE);
        } else {
            qmulWifiLoadingView.setVisibility(View.GONE);
            portalImageView.setBackground(drawableError);
            portalImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * portal checking
     * */
    private void sendRequest() {
		/*需要新建子线程进行访问*/
        new Thread(){
            public void run(){
                HttpURLConnection httpURLConnection=null;
                try {
                    URL url=new URL("http://www.baidu.com");
                    httpURLConnection=(HttpURLConnection) url.openConnection();//获取到httpURLConnection的实例
                    httpURLConnection.setInstanceFollowRedirects(false);
                    httpURLConnection.setRequestMethod("POST");//设置HTTP请求所使用的方法，GET表示希望从服务器那里获取数据，而POST则表示希望提交数据给服务器
                    httpURLConnection.setReadTimeout(10000);//设置读取超时的毫秒数
                    List<String> BundleData=new ArrayList<>();
                    String httpStateNo =httpURLConnection.getHeaderField("X-Android-Response-Source");
                    BundleData.add(httpStateNo);
                    String urlAndUrl =httpURLConnection.getHeaderField("Location");
                    BundleData.add(urlAndUrl);
                    Message msg=new Message();
                    msg.what=SHOW_RESPONSE;//封装子线程编号
                    msg.obj = BundleData;
                    handler.sendMessage(msg);//发送信息
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    httpURLConnection.disconnect();//将HTTP连接关闭掉
                }
            }
        }.start();
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_back_net_detail:
                finish();
                break;
            case R.id.rl_checking_hard_state:
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                break;
            case R.id.rl_checking_portal_state:
                Bundle portalBundle = new Bundle();
                portalBundle.putString("PortalUrl",PortalUrl);
                IntentUtils.startActivity(this, PortalLogInActivity.class,portalBundle);
                break;
            case R.id.rl_checking_dns_state:
                break;
                default:
                break;
        }
    }

    /**
     * 检测DNS服务器状态
     * @param CheckUrl
     * @param CheckIp
     * @param PingUrl
     * @param PingIp */
    private  void DNSConnectState(String CheckUrl,String CheckIp,String PingUrl,String PingIp) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.LbcDebug("DNS Data1");
                    PingNetEntity checkUrlEntity =new PingNetEntity("www.baidu.com",3,70,new StringBuffer());
                    LogUtils.LbcDebug("DNS Data2");
                    PingNetEntity checkUrlEntityResult= NetUtils.ping(checkUrlEntity, (long) 1500);
                    LogUtils.LbcDebug("DNS Data3");
                    PingNetEntity checkIpEntity =new PingNetEntity("202.108.22.5",3,70,new StringBuffer());
                    LogUtils.LbcDebug("DNS Data4");
                    PingNetEntity checkIpEntityResult= NetUtils.ping(checkIpEntity, (long) 1500);
                    PingNetEntity pingUrlEntity =new PingNetEntity("www.aliyun.com",3,70,new StringBuffer());
                    LogUtils.LbcDebug("DNS Data4");
                    PingNetEntity pingUrlEntityResult= NetUtils.ping(pingUrlEntity, (long) 1500);
                    PingNetEntity pingIpEntity =new PingNetEntity("106.11.93.21",3,70,new StringBuffer());
                    PingNetEntity pingIpEntityResult= NetUtils.ping(pingIpEntity, (long) 1500);
                    LogUtils.LbcDebug("DNS Data4");
                    if((checkIpEntityResult.isResult()&&checkUrlEntityResult.isResult())||(pingIpEntityResult.isResult()&&pingUrlEntityResult.isResult())){
                        //结果数据显示
                        Message dnsState = new Message();
                        dnsState.what=SHOW_DNSCONNCTSTATE;
                        dnsState.obj=true;
                        handler.sendMessage(dnsState);
                    }else{
                        Message dnsState = new Message();
                        dnsState.what=SHOW_DNSCONNCTSTATE;
                        dnsState.obj=false;
                        handler.sendMessage(dnsState);
                    }
                }catch (Exception e) {
                    LogUtils.LbcDebug("catch");
                    Message dnsState = new Message();
                    dnsState.what=SHOW_DNSCONNCTSTATE;
                    dnsState.obj=false;
                    handler.sendMessage(dnsState);
                }
            }
        }).start();

    }
}
