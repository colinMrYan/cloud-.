package com.inspur.emmcloud.ui.mine.setting;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;

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

    public static final int SHOW_RESPONSE=1;
    /*TextView是在主线程定义，所以修改操作也必须在主线程中，而获取内容是在子线程，所以当子线程获取内容后需要给主线程发送信息，主线程再对TextView的文本内容进行修改*/
    private Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg){
            switch (msg.what) {
                case SHOW_RESPONSE://根据子线程编号判断是哪个子线程发来的信息
                    String content=(String) msg.obj;
                    LogUtils.LbcDebug(content);
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
        checkingHardState();
    }

    /**
     * 检测硬件连接问题
     * */
    private void checkingHardState() {
        List<Integer> integerData=NetUtils.getNetWrokState(this);
        List<String> ConnectedNetNames = NetUtils.getNetStateName(integerData);
        if(-1==integerData.get(0)){
            Resources resources=getBaseContext().getResources();
            Drawable drawable=resources.getDrawable(R.drawable.ic_net_checking_error);
            hardImageView.setBackground(drawable);
            LogUtils.LbcDebug("当前网络不通，硬件断开");
        }else {
            Resources resources=getBaseContext().getResources();
            Drawable drawable=resources.getDrawable(R.drawable.ic_net_checking_succeed);
            hardImageView.setBackground(drawable);
        }
    }

//   /**
//    * 检查是否登录小助手
//    * */
//    private void checkingPortalLogin() {
//         sendRequest(null);
//    }
//    private void sendRequest(String urll) {
//                    String currentUrl = "http://www.baidu.com";
//                    if(urll!=null||urll!=""){
//                        currentUrl=urll;
//                    }
//		/*需要新建子线程进行访问*/
//                    final String finalCurrentUrl = currentUrl;
//                    new Thread(){
//                        public void run(){
//                            HttpURLConnection httpURLConnection=null;
//                            try {
//                                URL url=new URL(finalCurrentUrl);
//                                httpURLConnection=(HttpURLConnection) url.openConnection();//获取到httpURLConnection的实例
//                                httpURLConnection.setInstanceFollowRedirects(false);
//                                httpURLConnection.setRequestMethod("POST");//设置HTTP请求所使用的方法，GET表示希望从服务器那里获取数据，而POST则表示希望提交数据给服务器
//                                httpURLConnection.setReadTimeout(20000);//设置读取超时的毫秒数
//                                String urlAndUrl =httpURLConnection.getHeaderField("Location");
//                                InputStream is=httpURLConnection.getInputStream();//获取到服务器返回的输入流
//                                BufferedReader br=new BufferedReader(new InputStreamReader(is));//对获取到的输入流进行读取
//                                String s;
//                                StringBuilder sb=new StringBuilder();
//                                while((s=br.readLine())!=null){
//                                    sb.append(s);
//                                }
//                    Message msg=new Message();
//                    msg.what=SHOW_RESPONSE;//封装子线程编号
//
//                    msg.obj = urlAndUrl;
//                    handler.sendMessage(msg);//发送信息
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }finally{
//                    httpURLConnection.disconnect();//将HTTP连接关闭掉
//                }
//            }
//        }.start();
//    }


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
//                    LogUtils.jasonDebug("httpURLConnection.getResponseCode()="+httpURLConnection.getResponseCode());
//                    Map<String,List<String>> data22= httpURLConnection.getHeaderFields();
//                    String data3 =data22.values().toString();
//                    String data4 = data22.toString();
//                    String data5 ="shujudata3::"+data3+"::::::::"+data4;
////                    LogUtils.jasonDebug("data5="+data5);
//                    for (Map.Entry<String, List<String>> entry : data22.entrySet()) {
//                        String key = entry.getKey();
//                        for (String value : entry.getValue()) {
//                            LogUtils.jasonDebug(key + ":" + value);
//                        }
//                    }
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
                    // msg.obj=sb.toString();//封装获取到的内容
                    msg.obj = urlAndUrl;
                    LogUtils.LbcDebug(urlAndUrl);
                    handler.sendMessage(msg);//发送信息
                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    httpURLConnection.disconnect();//将HTTP连接关闭掉
                }
            };
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
                //IntentUtils.startActivity(this, PortalLogInActivity.class);
                sendRequest();
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
        LogUtils.LbcDebug("2222222222222222222222222222222222222222222222222222");
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
            //e.printStackTrace();
            LogUtils.LbcDebug("33333333333333333333333333333333333333333333");
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            LogUtils.LbcDebug("444444444444444444444444444444444444");
            return false;
        }
    }
    /**
     * 检测DNS服务器状态*/
    private  boolean DNSConnectState() {
        return  true;
    }
}
