package com.inspur.emmcloud.ui.mine.setting;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.LogUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by libaochao on 2018/11/8.
 * try to show the netWork state and details such as net delay/connect State  and so on by PING
 */

public class NetWorkStateDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping_network_state_detail);
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
                // Boolean data1 = wifiRouteState();
                //LogUtils.LbcDebug("检测wifi路由器::::::::"+data1);
                IntentUtils.startActivity(this, PortalLogInActivity.class);
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
