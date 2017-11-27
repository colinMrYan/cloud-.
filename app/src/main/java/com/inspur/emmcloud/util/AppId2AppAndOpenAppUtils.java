package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.ui.find.ScanResultActivity;

/**
 * Created by yufuchang on 2017/11/22.
 */

public class AppId2AppAndOpenAppUtils {
    private static AppId2AppAndOpenAppUtils appId2AppAndOpenAppUtils = null;
    private Activity activity;
    private String uri = "";
    private OnFinishActivityListener onFinishActivityListener;
    public static AppId2AppAndOpenAppUtils getInstance(Activity activity){
        if(appId2AppAndOpenAppUtils == null){
            synchronized (AppId2AppAndOpenAppUtils.class){
                if(appId2AppAndOpenAppUtils == null){
                    appId2AppAndOpenAppUtils = new AppId2AppAndOpenAppUtils(activity);
                }
            }
        }
        return appId2AppAndOpenAppUtils;
    }

    private AppId2AppAndOpenAppUtils(Activity activity){
        this.activity = activity;
    }

    /**
     * 根据id获取app详情
     */
    public  void getAppInfoById(Uri uri) {
        String appId = uri.getHost();
        this.uri = (uri == null) ? "":uri.toString();
        if (NetUtils.isNetworkConnected(activity) && !StringUtils.isBlank(appId)) {
            MyAppAPIService apiService = new MyAppAPIService(activity);
            apiService.setAPIInterface(new WebService());
            apiService.getAppInfo(appId);
        }else{
            onFinishActivityListener.onFinishActivity();
        }
    }

    /**
     * 设置回调函数
     * @param l
     */
    public void setOnFinishActivityListener(OnFinishActivityListener l){
        this.onFinishActivityListener = l;
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnAppInfoSuccess(App app) {
            handleAppAction(app);
            onFinishActivityListener.onFinishActivity();
        }

        @Override
        public void returnAppInfoFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(activity, error, errorCode);
            onFinishActivityListener.onFinishActivity();
        }
    }

    /**
     * 根据app打开app
     * @param app
     */
    private void handleAppAction(App app) {
        if(!StringUtils.isBlank(app.getAppID())){
            //特殊处理，不走UriUtils.openApp的逻辑，直接打开appUri，appUri是最终打开地址。
            if(app.getUri().startsWith("https://emm.inspur.com/ssohandler/gs/")){
                UriUtils.openWebApp(activity,app.getUri(),app);
            }else{
                UriUtils.openApp(activity,app);
            }
        } else{
            showUnKnownMsg(uri);
        }
    }

    /**
     * 展示扫描到的信息
     *
     * @param msg
     */
    private void showUnKnownMsg(String msg) {
        Intent intent = new Intent();
        intent.putExtra("result", msg);
        intent.setClass(activity, ScanResultActivity.class);
        activity.startActivity(intent);
    }

    public interface OnFinishActivityListener{
        void onFinishActivity();
    }
}
