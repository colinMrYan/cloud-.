package com.inspur.emmcloud.ui.mine.setting;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
    /*TextView是在主线程定义，所以修改操作也必须在主线程中，而获取内容是在子线程，所以当子线程获取内容后需要给主线程发送信息，主线程再对TextView的文本内容进行修改*/
    private Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE://根据子线程编号判断是哪个子线程发来的信息
                    if(1==NetStateintegerData.get(0)) {
                        String content=(String) msg.obj;
                        if(-1!=content.indexOf("&firsturl")){
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
        }else{
            LogUtils.LbcDebug("设置灰色且禁止点击");
        }
    }

    private void sendRequest() {
		/*需要新建子线程进行访问*/
        new Thread(){
            public void run(){
                HttpURLConnection httpURLConnection=null;
                try {
                    LogUtils.LbcDebug("new Thread");
                    URL url=new URL("http://www.baidu.com");
                    httpURLConnection=(HttpURLConnection) url.openConnection();//获取到httpURLConnection的实例
                    httpURLConnection.setInstanceFollowRedirects(false);
                    httpURLConnection.setRequestMethod("POST");//设置HTTP请求所使用的方法，GET表示希望从服务器那里获取数据，而POST则表示希望提交数据给服务器
                    httpURLConnection.setReadTimeout(20000);//设置读取超时的毫秒数
                    String urlAndUrl =httpURLConnection.getHeaderField("Location");
                    InputStream is=httpURLConnection.getInputStream();//获取到服务器返回的输入流
                    BufferedReader br=new BufferedReader(new InputStreamReader(is));//对获取到的输入流进行读取
                    String s;
                    StringBuilder sb=new StringBuilder();
                    while((s=br.readLine())!=null){
                        sb.append(s);
                    }
                    Message msg=new Message();
                    msg.what=SHOW_RESPONSE;//封装子线程编号
                    msg.obj = urlAndUrl;
                    LogUtils.LbcDebug("new thread :::::"+urlAndUrl);
                    handler.sendMessage(msg);//发送信息
                } catch (Exception e) {
                    LogUtils.LbcDebug("Url :::::::::::catch");
                    e.printStackTrace();
                }finally{
                    LogUtils.LbcDebug("URL::::::::::finally");
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
                LogUtils.LbcDebug("检测硬件");
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                break;
            case R.id.rl_check_wifi_state:
                Bundle portalBundle = new Bundle();
                portalBundle.putString("PortalUrl",PortalUrl);
                IntentUtils.startActivity(this, PortalLogInActivity.class,portalBundle);
                break;
            case R.id.rl_check_dns_state:
                LogUtils.LbcDebug("检测DNS服务器");
                break;
                default:
                break;
        }
    }

   /**
    * 判断有无网络链接
    * @param */
    private boolean  wifiOrGPRSConnect() {
        //通过Ping方式判断网络状态
        ConnectivityManager cm;
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConnected=cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ? true : false ;
        boolean isGprsConnected=cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ? true : false ;
        return  isWifiConnected&&isGprsConnected;
    }

   /**
    * 检测路由状况*/
    private boolean wifiRouteState() {
        //仅判断有无小助手需求
        boolean data2 = isWifiSetPortal();
        LogUtils.LbcDebug("1111111111111111111111111111111111111111111111111");
        return  data2 ;
    }


    private boolean isWifiSetPortalall() {
        final String mWalledGardenUrl = "http://connect.rom.miui.com/generate_204";
        final int WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000;

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(mWalledGardenUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            return urlConnection.getResponseCode() != 204;
        } catch (IOException e) {
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }


    private boolean isWifiSetPortal() {
        final String mWalledGardenUrl = "http://connect.rom.miui.com/generate_204";
        final int WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(mWalledGardenUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            return urlConnection.getResponseCode() != 204;
        } catch (IOException e) {
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            return false;
        }
    }

    /**
     * 检测DNS服务器状态*/
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
                    LogUtils.LbcDebug("DNS 检测有误");
                    Message dnsState = new Message();
                    dnsState.what=SHOW_DNSCONNCTSTATE;
                    dnsState.obj=false;
                    handler.sendMessage(dnsState);
                }
            }
        }).start();

    }
}
