package com.inspur.emmcloud.ui.mine.setting;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by libaochao on 2018/11/8.
 */

public class PortalLogInActivity extends BaseActivity {

    private WebView webview;
    public String acceptData;  //定义接受json数据信息的变量
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal_login);
//        webview = (WebView)findViewById(R.id.wv_show_login_detail);
//
//        webview.getSettings().setJavaScriptEnabled(true);
//        webview.loadUrl("http://www.baidu.com");
//        webview.setWebViewClient(new WebViewClient(){
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return super.shouldOverrideUrlLoading(view, url);
//            }
//        });
        //处理返回结果的函数，系统提供的类方法  //handler处理返回数据， 此方法，我写在onCreate()函数外。

        SendGetRequest("http://baidu.com","");

    }







    public void SendGetRequest(final String url,final String content) {
        new Thread(){
            @Override
            public void run() {
                String pathString = url + content;
                HttpURLConnection connection;
                try {
                    URL url = new URL(pathString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    //接受数据
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"utf-8"));
                        String line;
                        while((line=bufferedReader.readLine())!=null){ //不为空进行操作
                            acceptData+=line;
                        }
                        System.out.println("接受到的数据："+acceptData);
                        LogUtils.LbcDebug("接收的数据为：：：：：：：：：：：：：：："+acceptData);
                    }
                } catch (MalformedURLException e) {
                    LogUtils.LbcDebug("error((((((((((((((((((((((((((");
                    e.printStackTrace();
                } catch (IOException e) {
                    LogUtils.LbcDebug("error((((((((((((((((((((((((((");
                    e.printStackTrace();
                }
            }
        }.start();
    }







    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_back_portal_login:
                finish();
                break;
            default:
                break;
        }
    }
}
