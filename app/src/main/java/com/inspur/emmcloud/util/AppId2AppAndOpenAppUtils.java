package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.ui.SchemeHandleActivity;
import com.inspur.emmcloud.ui.find.ScanResultActivity;

import java.util.List;

/**
 * Created by yufuchang on 2017/11/22.
 */

public class AppId2AppAndOpenAppUtils {
    private static AppId2AppAndOpenAppUtils appId2AppUtils = null;
    private Activity activity;
    private String uri = "";
    public static AppId2AppAndOpenAppUtils getInstance(Activity activity){
        if(appId2AppUtils == null){
            synchronized (AppId2AppAndOpenAppUtils.class){
                if(appId2AppUtils == null){
                    appId2AppUtils = new AppId2AppAndOpenAppUtils(activity);
                }
            }
        }
        return appId2AppUtils;
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
            finishActivity();
        }
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnAppInfoSuccess(App app) {
            if(!StringUtils.isBlank(app.getAppID())){
                UriUtils.openApp(activity,app);
            } else{
                showUnKnownMsg(uri);
            }
            if(activity instanceof SchemeHandleActivity){
                finishActivity();
            }
        }

        @Override
        public void returnAppInfoFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(activity, error, errorCode);
            if(activity instanceof SchemeHandleActivity){
                finishActivity();
            }
        }
    }

    /**
     * 结束Activity
     */
    private void finishActivity() {
        List<Activity> activityList = ((MyApplication)activity.getApplication()).getActivitieList();
        for (int i = 0; i < activityList.size(); i++){
            if(activityList.get(i).getLocalClassName().contains("SchemeHandleActivity")){
                activityList.get(i).finish();
            }
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
}
