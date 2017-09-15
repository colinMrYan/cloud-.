package com.inspur.emmcloud.callback;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.ui.mine.setting.CreateGestureActivity;
import com.inspur.emmcloud.ui.mine.setting.GestureLoginActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.DbCacheUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;

/**
 * Created by chenmch on 2017/9/13.
 */

public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    public int count = 0;
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {



        if (!((MyApplication) activity.getApplicationContext()).getIsActive()) {
            if (count == 0) {
                if (((MyApplication) activity.getApplicationContext())
                        .isIndexActivityRunning()) {
                    ((MyApplication) activity.getApplicationContext()).clearNotification();
                    uploadMDMInfo(activity.getApplicationContext());
                    ((MyApplication) activity.getApplicationContext()).sendActivedWSMsg();
                    //这里两处登录均不走这个方法
                    if(getIsNeedGestureCode(activity.getApplicationContext())){
                        showGestureVerification(activity.getApplicationContext());
                    }
                }
            }
        }

        count++;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        count--;
        if (count == 0) {
            ((MyApplication) activity.getApplicationContext()).setIsActive(false);
            ((MyApplication) activity.getApplicationContext()).sendFrozenWSMsg();
            startUploadPVCollectService(activity.getApplicationContext());
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    /**
     * 弹出手势验证码
     * @param context
     */
    private void showGestureVerification(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                    Intent intent = new Intent(context,GestureLoginActivity.class);
                    intent.putExtra("gesture_code_change","login");
                    context.startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).start();
    }

    /**
     * 判断收需要打开手势解锁
     * @return
     */
    private boolean getIsNeedGestureCode(Context context) {
        String gestureCode = CreateGestureActivity.getGestureCodeByUser(context);
        boolean gestureCodeOpen = CreateGestureActivity.getGestureCodeIsOpenByUser(context);
        return !StringUtils.isBlank(gestureCode) && gestureCodeOpen;
    }

    /**
     * 上传MDM需要的设备信息
     */
    private void uploadMDMInfo(Context context) {
        if (NetUtils.isNetworkConnected(context, false)) {
            new AppAPIService(context).uploadMDMInfo();
        }

    }

    /***
     * 打开app应用行为分析上传的Service;
     */
    private void startUploadPVCollectService(Context context) {
        // TODO Auto-generated method stub
        if (!AppUtils.isServiceWork(context, "com.inspur.emmcloud.service.CollectService") && (!DbCacheUtils.isDbNull())) {
            Intent intent = new Intent();
            intent.setClass(context, PVCollectService.class);
            context.startService(intent);
        }
    }
}
