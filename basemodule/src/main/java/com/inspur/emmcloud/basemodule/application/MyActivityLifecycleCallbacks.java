package com.inspur.emmcloud.basemodule.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.ClientIDUtils;
import com.inspur.emmcloud.login.appcenter.AppcenterService;
import com.luojilab.component.componentlib.router.Router;

/**
 * Created by chenmch on 2017/9/13.
 */

public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private int count = 0;
    private Activity currentActivity;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        BaseApplication.getInstance().addActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
        if (count == 0) {
            BaseApplication.getInstance().setIsActive(true);
//            if (BaseApplication.getInstance().isHaveLogin()) {
//                long appBackgroundTime = PreferencesUtils.getLong(BaseApplication.getInstance(), Constant.PREF_APP_BACKGROUND_TIME, 0L);
//                //进入后台后重新进入应用需要间隔3分钟以上才弹出二次验证
//                if (System.currentTimeMillis() - appBackgroundTime >= 180000) {
//                    showFaceOrGestureLock();
//                }
//                uploadMDMInfo();
//                Router router = Router.getInstance();
//                if (router.getService(AppService.class.getSimpleName()) != null) {
//                    AppService service = (AppService) router.getService(AppService.class.getSimpleName());
//                    service.getAppBadgeCountFromServer();
//                }
//            }
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
        if (count == 0) { // app 进入后台
            LogUtils.jasonDebug("进入后台====================");
            PreferencesUtils.putLong(BaseApplication.getInstance(), Constant.PREF_APP_BACKGROUND_TIME, System.currentTimeMillis());
            BaseApplication.getInstance().setIsActive(false);
            if (BaseApplication.getInstance().isHaveLogin()) {
//                startUploadPVCollectService(BaseApplication.getInstance());
                startSyncCommonAppService();
                new ClientIDUtils(BaseApplication.getInstance()).upload();
            }

        }
    }


    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        BaseApplication.getInstance().removeActivity(activity);
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }




    /***
     * 打开同步常用应用Service;
     */
    private void startSyncCommonAppService() {
        // TODO Auto-generated method stub
        Router router = Router.getInstance();
        if (router.getService(AppcenterService.class.getSimpleName()) != null) {
            AppcenterService service = (AppcenterService) router.getService(AppcenterService.class.getSimpleName());
            service.startSyncCommonAppService();
        }
    }


}
