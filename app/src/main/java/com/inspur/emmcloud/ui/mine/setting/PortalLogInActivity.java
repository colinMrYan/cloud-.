package com.inspur.emmcloud.ui.mine.setting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by libaochao on 2018/11/8.
 */

public class PortalLogInActivity extends BaseActivity {

    private WebView webview;
    public static final int SHOW_RESPONSE=1;
    /*TextView是在主线程定义，所以修改操作也必须在主线程中，而获取内容是在子线程，所以当子线程获取内容后需要给主线程发送信息，主线程再对TextView的文本内容进行修改*/
    private Handler handler=new Handler(){
        public void handleMessage(android.os.Message msg){
            switch (msg.what) {
                case SHOW_RESPONSE://根据子线程编号判断是哪个子线程发来的信息
                    String content=(String) msg.obj;

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
        setContentView(R.layout.activity_portal_login);
        //1、网络检测,GPRS、Wifi、VPN(硬件连接检测) （涉及的状态有 连接GPRS 2 3 4/G,wifi或者VPN,提示对应的状态）或者提示无链接
        //2、小助手检测 （需要链接小助手提示，或者无效，）
        //3、DNS检测    （DNS检测或者无效，或者DNS链接问题）

        webview = (WebView)findViewById(R.id.wv_show_login_detail);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("http://www.baidu.com");
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        //处理返回结果的函数，系统提供的类方法  //handler处理返回数据， 此方法，我写在onCreate()函数外。
      //  SendGetRequest("http://baidu.com","");

    }

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
            case R.id.rl_back_portal_login:
                finish();
                break;
            default:
                break;
        }
    }
}
