package com.inspur.emmcloud.basemodule.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.service.PVCollectService;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ClientIDUtils;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
import com.inspur.emmcloud.login.app.AppService;
import com.inspur.emmcloud.login.appcenter.AppcenterService;
import com.inspur.emmcloud.login.setting.SettingService;
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
            if (BaseApplication.getInstance().isHaveLogin()) {
                long appBackgroundTime = PreferencesUtils.getLong(BaseApplication.getInstance(), Constant.PREF_APP_BACKGROUND_TIME, 0L);
                //进入后台后重新进入应用需要间隔3分钟以上才弹出二次验证
                if (System.currentTimeMillis() - appBackgroundTime >= 180000) {
                    showFaceOrGestureLock();
                }
                uploadMDMInfo();
                Router router = Router.getInstance();
                if (router.getService(AppService.class.getSimpleName()) != null) {
                    AppService service = (AppService) router.getService(AppService.class.getSimpleName());
                    service.getAppBadgeCountFromServer();
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
        if (count == 0) { // app 进入后台
            PreferencesUtils.putLong(BaseApplication.getInstance(), Constant.PREF_APP_BACKGROUND_TIME, System.currentTimeMillis());
            BaseApplication.getInstance().setIsActive(false);
            if (BaseApplication.getInstance().isHaveLogin()) {
                startUploadPVCollectService(BaseApplication.getInstance());
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

    /**
     * 弹出进入app安全验证界面
     */
    private void showFaceOrGestureLock() {
        Router router = Router.getInstance();
        if (router.getService(SettingService.class.getSimpleName()) != null) {
            SettingService settingService = (SettingService) router.getService(SettingService.class.getSimpleName());
            boolean isSetFaceOrGestureLock = settingService.isSetFaceOrGestureLock();
            if (isSetFaceOrGestureLock) {
                BaseApplication.getInstance().setSafeLock(true);
                new Handler().postDelayed(() -> {
                    settingService.showFaceOrGestureLock();
                }, 200);
            }

        }
    }

    /**
     * 上传MDM需要的设备信息
     */
    private void uploadMDMInfo() {
        Router router = Router.getInstance();
        if (router.getService(SettingService.class.getSimpleName()) != null) {
            SettingService settingService = (SettingService) router.getService(SettingService.class.getSimpleName());
            settingService.uploadMDMInfo();
        }
    }

    /***
     * 打开app应用行为分析上传的Service;
     */
    private void startUploadPVCollectService(Context context) {
        // TODO Auto-generated method stub
        if (!AppUtils.isServiceWork(context, PVCollectService.class.getName()) && (!DbCacheUtils.isDbNull())) {
            Intent intent = new Intent();
            intent.setClass(context, PVCollectService.class);
            context.startService(intent);
        }
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
