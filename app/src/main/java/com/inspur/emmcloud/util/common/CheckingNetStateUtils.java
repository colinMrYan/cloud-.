package com.inspur.emmcloud.util.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libaochao on 2019/2/18.
 */

public class CheckingNetStateUtils {

    public CheckingNetStateUtils(Context context){
        this.context=context;
    }

    private Context context;
    private Handler handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
              List<Object> pingIdAndData = (List<Object>)msg.obj;
              String actionTip = (String)pingIdAndData.get( 0 );
              pingIdAndData.remove(0);
              LogUtils.LbcDebug( "action:"+pingIdAndData.get(0)+"data"+pingIdAndData.get(1));
              EventBus.getDefault().post(new SimpleEventMessage(actionTip, pingIdAndData));
              super.handleMessage( msg );
        }
    };

    /**
     *Ping  网络通断状态检测（用于显示网络状态异常框）
     * */
    public   void CheckNetPingThreadStart(final  String[]  StrUrl, final int WaiteTime, final String eventBusAction) {
        for(int i=0;i<StrUrl.length;i++){
            final int finalI = i;
            new Thread( new Runnable() {
                @Override
                public void run() {
                    try {
                            PingNetEntity pingNetEntity=new PingNetEntity(StrUrl[finalI],1,WaiteTime,new StringBuffer());
                            pingNetEntity=NetUtils.ping(pingNetEntity, (long)WaiteTime);
                            final List<Object> pingIdAndData = new ArrayList<>();
                            pingIdAndData.add( eventBusAction );
                            pingIdAndData.add(StrUrl[finalI]);
                            pingIdAndData.add(pingNetEntity.isResult());
                            Message  message = new Message();
                            message.obj = pingIdAndData;
                            handler.sendMessage( message );
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public   void CheckNetHttpThreadStart(final  String[]  StrUrl) {
        for(int i=0;i<StrUrl.length;i++){
            AppAPIService apiService = new AppAPIService(context);
            apiService.setAPIInterface( new WebHttpService() );
            apiService.getCloudConnectStateUrl( StrUrl[i] );
        }
    }

    public  class WebHttpService extends APIInterfaceInstance {
        @Override
        public void returnCheckCloudPluseConnectionSuccess(byte[] arg0, final String url) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    LogUtils.LbcDebug( "http 返回成功"+url );
                    List<Object> pingIdAndData = new ArrayList<>();//PingThreadStart
                    pingIdAndData.add(url);
                    pingIdAndData.add(true);
                    EventBus.getDefault().post(new SimpleEventMessage( Constant.EVENTBUS_TAG__NET_HTTP_POST_CONNECTION, pingIdAndData));
                }
            });
        }

        @Override
        public void returnCheckCloudPluseConnectionError(String error, int responseCode,final String url) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    LogUtils.LbcDebug( "http 返回失败" );
                    List<Object> pingIdAndData = new ArrayList<>();//PingThreadStart
                    pingIdAndData.add(url);
                    pingIdAndData.add(false);
                    EventBus.getDefault().post(new SimpleEventMessage( Constant.EVENTBUS_TAG__NET_HTTP_POST_CONNECTION, pingIdAndData));
                }
            });
        }
    }

}
