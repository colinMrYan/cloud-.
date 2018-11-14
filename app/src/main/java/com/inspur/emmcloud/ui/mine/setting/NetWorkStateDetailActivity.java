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
    QMUILoadingView qmulWifiLoadingView ;
    QMUILoadingView qmulDnsLoadingView;
    public static final int SHOW_DNSCONNCTSTATE=2;
    public static final int SHOW_RESPONSE=1;
    private Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    if(1==NetStateintegerData.get(0)) {
                        List<String> bundleata =(List<String>)msg.obj;
                        String httpResNum = bundleata.get(0);
                        String content=bundleata.get(1);
                        if((-1!=content.indexOf("&firsturl"))&&(-1!=httpResNum.indexOf("NETWORK 302"))){
                            PortalUrl = content.substring(0,content.indexOf("&firsturl"));
                            portalImageView.setBackground(drawableError);
                        }else {
                            portalImageView.setBackground(drawableSuccess);
                        }
                        qmulWifiLoadingView.setVisibility(View.GONE);
                        portalImageView.setVisibility(View.VISIBLE);
                    } else {
                        qmulWifiLoadingView.setVisibility(View.GONE);
                    }
                    break;
                case SHOW_DNSCONNCTSTATE:
                     if(netHardConnectState){
                         if((boolean)msg.obj){
                            dnsImageView.setBackground(drawableSuccess);
                         } else {
                             dnsImageView.setBackground(drawableError);
                         }
                         qmulDnsLoadingView.setVisibility(View.GONE);
                         dnsImageView.setVisibility(View.VISIBLE);
                     } else {
                         qmulDnsLoadingView.setVisibility(View.GONE);
                     }
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping_network_state_detail);
        hardImageView = (ImageView)findViewById(R.id.iv_hard_checking);
        portalImageView= (ImageView)findViewById(R.id.iv_wifi_checking);
        dnsImageView   = (ImageView)findViewById(R.id.iv_dns_checking);
        drawableError=getBaseContext().getResources().getDrawable(R.drawable.app_delete);
        drawableSuccess=getBaseContext().getResources().getDrawable(R.drawable.icon_other_selected);
        qmulWifiLoadingView =(QMUILoadingView)findViewById(R.id.qlv_wifi_checkloading);
        qmulDnsLoadingView =(QMUILoadingView)findViewById(R.id.qlv_dns_checkloading);
        //检测网络通断
        netHardConnectState= checkingHardState();
        //检测端口
        if(netHardConnectState){
            //检测小助手
            checkingPortalState();
            //检测DNS服务
            DNSConnectState("","","","");
        }else {
            qmulDnsLoadingView.setVisibility(View.GONE);
            qmulWifiLoadingView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 检测硬件连接问题
     * */
    private boolean checkingHardState() {
        QMUILoadingView qmulHardLoadingView =(QMUILoadingView)findViewById(R.id.qlv_hard_checkloading);
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
        }
    }

    /**
     * Ping 网络
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
                    httpURLConnection.setReadTimeout(20000);//设置读取超时的毫秒数
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
            case R.id.rl_check_hard_device:
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                break;
            case R.id.rl_check_wifi_state:
                Bundle portalBundle = new Bundle();
                portalBundle.putString("PortalUrl",PortalUrl);
                IntentUtils.startActivity(this, PortalLogInActivity.class,portalBundle);
                break;
            case R.id.rl_check_dns_state:
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
        final String[] checkUrl = {CheckUrl};
        final String[] checkIp = {CheckIp};
        final String[] pingUrl = {PingUrl};
        final String[] pingIp = {PingIp};

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(""== checkUrl[0] ||""== checkIp[0]) {
                    checkUrl[0] ="www.baidu.com";
                    checkIp[0] ="202.108.22.5";
                }
                if(""== pingUrl[0] ||""== pingIp[0]) {
                    pingIp[0] ="www.aliyun.com";
                    pingUrl[0] ="106.11.93.21";
                }
                try {
                    PingNetEntity checkUrlEntity =new PingNetEntity(checkUrl[0],3,5,new StringBuffer());
                    PingNetEntity checkUrlEntityResult= NetUtils.ping(checkUrlEntity);
                    PingNetEntity checkIpEntity =new PingNetEntity(checkIp[0],3,5,new StringBuffer());
                    PingNetEntity checkIpEntityResult= NetUtils.ping(checkIpEntity);
                    PingNetEntity pingUrlEntity =new PingNetEntity(pingUrl[0],3,5,new StringBuffer());
                    PingNetEntity pingUrlEntityResult= NetUtils.ping(pingUrlEntity);
                    PingNetEntity pingIpEntity =new PingNetEntity(pingIp[0],3,5,new StringBuffer());
                    PingNetEntity pingIpEntityResult= NetUtils.ping(pingIpEntity);
                    if(checkIpEntityResult.isResult()&&checkUrlEntityResult.isResult()&&pingIpEntityResult.isResult()&&pingUrlEntityResult.isResult()){
                        //结果数据显示
                        Message dnsState = new Message();
                        dnsState.what=SHOW_DNSCONNCTSTATE;
                        dnsState.obj=true;
                        handler.sendMessage(dnsState);
                    }
                    Message dnsState = new Message();
                    dnsState.what=SHOW_DNSCONNCTSTATE;
                    dnsState.obj=false;
                    handler.sendMessage(dnsState);
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
