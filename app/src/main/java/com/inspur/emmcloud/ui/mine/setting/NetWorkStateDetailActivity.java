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

   private ImageView hardImageView;
   private ImageView portalImageView;
   private ImageView dnsImageView;
   private QMUILoadingView qmulHardLoadingView ;
   private QMUILoadingView qmulWifiLoadingView ;
   private QMUILoadingView qmulDnsLoadingView;
   private Drawable drawableError;
   private Drawable drawableSuccess;
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
            checkingPortalState("http://www.baidu.com");
            //检测DNS服务
            DNSConnectState("www.baidu.com","202.108.22.5","www.aliyun.com","106.11.93.21");
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
                    case SHOW_PORTAL_CONNECT:
                        if(NetUtils.NETWORK_WIFI==NetStateintegerData.get(0)) {
                            List<String> bundleata =(List<String>)msg.obj;
                            String httpResNum = bundleata.get(0);
                            String content=bundleata.get(1);
                            if(-1!=httpResNum.indexOf("NETWORK 30")){
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
    private void checkingPortalState(final String StrUrl){
        if(NetUtils.NETWORK_WIFI==NetStateintegerData.get(0)){
            sendRequest(StrUrl);
        } else if((NetStateintegerData.get(0)>=NetUtils.NETWORK_2G)&&(NetStateintegerData.get(0)<=NetUtils.NETWORK_4G)) {
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
     * portal checking "http://www.baidu.com"
     * @param StrUrl   "http://www.baidu.com"
     * */
    private void sendRequest(final  String  StrUrl) {
		/*需要新建子线程进行访问*/
        new Thread(){
            public void run(){
                HttpURLConnection httpURLConnection=null;
                try {
                    URL url=new URL(StrUrl);
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
                    msg.what=SHOW_PORTAL_CONNECT;//封装子线程编号
                    msg.obj = BundleData;
                    handler.sendMessage(msg);//发送信息
                } catch (Exception e) {
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
            case R.id.rl_checking_hard_state:
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                break;
            case R.id.rl_checking_portal_state:
                String activityName = getResources().getString(R.string.net_network_authentication);
                UriUtils.openUrl(this,PortalUrl,"网络认证");
                break;
            case R.id.rl_checking_dns_state:
                break;
                default:
                break;
        }
    }

    /**
     * 检测DNS服务器状态
     * @param CheckUrl "www.baidu.com"
     * @param CheckIp   "202.108.22.5"
     * @param PingUrl   "www.aliyun.com"
     * @param PingIp    "106.11.93.21"*/
    private  void DNSConnectState(final String CheckUrl, final String CheckIp, final String PingUrl, final String PingIp) {
        final String checkUrl  = CheckUrl;
        final String checkIp   = CheckIp;
        final String pingUrl   = PingUrl;
        final String pingIp    = PingIp;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PingNetEntity checkUrlEntity =new PingNetEntity(checkUrl,1,1,new StringBuffer());
                    PingNetEntity checkUrlEntityResult= NetUtils.ping(checkUrlEntity, (long) 1000);
                    PingNetEntity checkIpEntity =new PingNetEntity(checkIp,1,1,new StringBuffer());
                    PingNetEntity checkIpEntityResult= NetUtils.ping(checkIpEntity, (long) 1000);
                    PingNetEntity pingUrlEntity =new PingNetEntity(pingUrl,1,1,new StringBuffer());
                    PingNetEntity pingUrlEntityResult= NetUtils.ping(pingUrlEntity, (long) 1000);
                    PingNetEntity pingIpEntity =new PingNetEntity(pingIp,1,1,new StringBuffer());
                    PingNetEntity pingIpEntityResult= NetUtils.ping(pingIpEntity, (long) 1000);
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
                    Message dnsState = new Message();
                    dnsState.what=SHOW_DNSCONNCTSTATE;
                    dnsState.obj=false;
                    handler.sendMessage(dnsState);
                }
            }
        }).start();

    }
}
