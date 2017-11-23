package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.ui.find.ScanResultActivity;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.api.ImpActivity;

import java.util.List;

/**
 * Created by yufuchang on 2017/11/22.
 */

public class AppId2AppAndOpenAppUtils {
    private static AppId2AppAndOpenAppUtils appId2AppAndOpenAppUtils = null;
    private Activity activity;
    private String uri = "";
    private LoadingDialog loadingDialog;
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
        loadingDialog = new LoadingDialog(activity);
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
            loadingDialogDismiss();
            handleAppAction(app);
            finishActivity();
        }

        @Override
        public void returnAppInfoFail(String error, int errorCode) {
            WebServiceMiddleUtils.hand(activity, error, errorCode);
            finishActivity();
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
                Intent intent = new Intent();
                intent.setClass(activity, ImpActivity.class);
                intent.putExtra("uri",app.getUri());
                intent.putExtra("appName","");
                intent.putExtra("help_url",app.getHelpUrl());
                intent.putExtra("is_zoomable",app.getIsZoomable());
                activity.startActivity(intent);
            }else{
                UriUtils.openApp(activity,app);
            }
        } else{
            showUnKnownMsg(uri);
        }
    }

    /**
     * 取消dialog
     */
    private void loadingDialogDismiss() {
        if(loadingDialog != null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
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
