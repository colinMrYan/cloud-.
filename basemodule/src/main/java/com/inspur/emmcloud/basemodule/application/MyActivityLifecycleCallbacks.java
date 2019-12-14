package com.inspur.emmcloud.basemodule.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppExceptionManager;
import com.inspur.emmcloud.basemodule.util.AppPVManager;
import com.inspur.emmcloud.basemodule.util.ClientIDUtils;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.componentservice.appcenter.AppcenterService;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.setting.SettingService;

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
                //进入后台后重新进入应用需要间隔1分钟以上才弹出二次验证
                if (System.currentTimeMillis() - appBackgroundTime >= 60 * 1000) {
                    showFaceOrGestureLock();
                }
                uploadMDMInfo();
                Router router = Router.getInstance();
                if (router.getService(AppService.class) != null) {
                    AppService service = router.getService(AppService.class);
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
            if (BaseApplication.getInstance().isSafeLock()) {
                BaseApplication.getInstance().setSafeLock(false);
                Router router = Router.getInstance();
                if (router.getService(SettingService.class) != null) {
                    SettingService settingService = router.getService(SettingService.class);
                    boolean isSetFaceOrGestureLock = settingService.isSetFaceOrGestureLock();
                    if (isSetFaceOrGestureLock) {
                        settingService.closeOriginLockPage();
                    }

                }
            } else {
                PreferencesUtils.putLong(BaseApplication.getInstance(), Constant.PREF_APP_BACKGROUND_TIME, System.currentTimeMillis());
            }
            BaseApplication.getInstance().setIsActive(false);
            if (BaseApplication.getInstance().isHaveLogin()) {
                //进行app异常上传
                new AppExceptionManager().uploadException();
                new AppPVManager().uploadPV();
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
        if (router.getService(SettingService.class) != null) {
            final SettingService settingService = router.getService(SettingService.class);
            boolean isSetFaceOrGestureLock = settingService.isSetFaceOrGestureLock();
            if (isSetFaceOrGestureLock) {
                BaseApplication.getInstance().setSafeLock(true);
                settingService.closeOriginLockPage();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        settingService.showFaceOrGestureLock();
                    }
                }, 200);
            }

        }
    }

    /**
     * 上传MDM需要的设备信息
     */
    private void uploadMDMInfo() {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.uploadMDMInfo();
        }
    }


    /**


    /***
     * 打开同步常用应用Service;
     */
    private void startSyncCommonAppService() {
        // TODO Auto-generated method stub
        Router router = Router.getInstance();
        if (router.getService(AppcenterService.class) != null) {
            AppcenterService service = router.getService(AppcenterService.class);
            service.startSyncCommonAppService();
        }
    }


}
