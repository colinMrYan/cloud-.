package com.inspur.emmcloud.interf;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.service.SyncCommonAppService;
import com.inspur.emmcloud.ui.mine.setting.CreateGestureActivity;
import com.inspur.emmcloud.ui.mine.setting.FaceVerifyActivity;
import com.inspur.emmcloud.ui.mine.setting.GestureLoginActivity;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.systool.emmpermission.EmmPermissionActivity;
import com.inspur.emmcloud.util.common.systool.emmpermission.Permissions;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.util.privates.AppBadgeUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ClientIDUtils;
import com.inspur.emmcloud.util.privates.cache.DbCacheUtils;

/**
 * Created by chenmch on 2017/9/13.
 */

public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private int count = 0;
    private Activity currentActivity;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        MyApplication.getInstance().addActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
        //检查是否有必要权限，如果有则继续下面逻辑，如果没有则转到MainActivity
        if (isLackNecessaryPermission()) {
            count++;
            return;
        }
        if (count == 0) {
            MyApplication.getInstance().setIsActive(true);
            if (!isLackNecessaryPermission() && MyApplication.getInstance().isHaveLogin()) {
                long appBackgroundTime = PreferencesUtils.getLong(MyApplication.getInstance(), Constant.PREF_APP_BACKGROUND_TIME, 0L);
                //进入后台后重新进入应用需要间隔3分钟以上才弹出二次验证
                if (System.currentTimeMillis() - appBackgroundTime >= 180000) {
                    showSafeVerificationPage();
                }
                uploadMDMInfo(activity);
                new AppBadgeUtils(MyApplication.getInstance()).getAppBadgeCountFromServer();
            }
        }
        count++;

    }

    private boolean isLackNecessaryPermission() {
        //如果没有存储权限则跳转到MainActivity进行处理
        String[] necessaryPermissionArray = StringUtils.concatAll(Permissions.STORAGE, new String[]{Permissions.READ_PHONE_STATE});
        if (!PermissionRequestManagerUtils.getInstance().isHasPermission(MyApplication.getInstance(), necessaryPermissionArray)) {
            if (!(currentActivity instanceof MainActivity || currentActivity instanceof EmmPermissionActivity)) {
                Intent intent = new Intent(currentActivity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                currentActivity.startActivity(intent);
            }
            return true;
        }
        return false;
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
            PreferencesUtils.putLong(MyApplication.getInstance(), Constant.PREF_APP_BACKGROUND_TIME, System.currentTimeMillis());
            MyApplication.getInstance().setIsActive(false);
            WebSocketPush.getInstance().closeWebsocket();
            if (MyApplication.getInstance().isHaveLogin()) {
                startUploadPVCollectService(MyApplication.getInstance());
                startSyncCommonAppService(MyApplication.getInstance());
                new ClientIDUtils(MyApplication.getInstance()).upload();
            }

        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        MyApplication.getInstance().removeActivity(activity);
    }

    public int getCount() {
        return count;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    /**
     * 弹出进入app安全验证界面
     */
    private void showSafeVerificationPage() {

        if (FaceVerifyActivity.getFaceVerifyIsOpenByUser(MyApplication.getInstance())) {
            MyApplication.getInstance().setSafeLock(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(currentActivity, FaceVerifyActivity.class);
                    intent.putExtra("isFaceVerifyExperience", false);
                    currentActivity.startActivity(intent);
                }
            }, 200);


        } else if (getIsNeedGestureCode(MyApplication.getInstance())) {
            MyApplication.getInstance().setSafeLock(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(currentActivity, GestureLoginActivity.class);
                    intent.putExtra("gesture_code_change", "login");
                    currentActivity.startActivity(intent);
                }
            }, 200);
        }

    }

    /**
     * 判断收需要打开手势解锁
     *
     * @return
     */
    private boolean getIsNeedGestureCode(Context context) {
        return CreateGestureActivity.getGestureCodeIsOpenByUser(context);
    }

    /**
     * 上传MDM需要的设备信息
     */
    private void uploadMDMInfo(Context context) {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance(), false)) {
            AppAPIService appAPIService = new AppAPIService(context);
            appAPIService.uploadMDMInfo();
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
    private void startSyncCommonAppService(Context context) {
        // TODO Auto-generated method stub
        if (!AppUtils.isServiceWork(context, SyncCommonAppService.class.getName()) && (!DbCacheUtils.isDbNull())) {
            Intent intent = new Intent();
            intent.setClass(context, SyncCommonAppService.class);
            context.startService(intent);
        }
    }


}
