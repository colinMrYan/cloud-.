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
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.service.SyncCommonAppService;
import com.inspur.emmcloud.ui.SchemeHandleActivity;
import com.inspur.emmcloud.ui.mine.setting.CreateGestureActivity;
import com.inspur.emmcloud.ui.mine.setting.FaceVerifyActivity;
import com.inspur.emmcloud.ui.mine.setting.GestureLoginActivity;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.systool.permission.PermissionManagerUtils;
import com.inspur.emmcloud.util.common.systool.permission.Permissions;
import com.inspur.emmcloud.util.privates.AppBadgeUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ClientIDUtils;
import com.inspur.emmcloud.util.privates.cache.DbCacheUtils;
import com.yanzhenjie.permission.Permission;

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
        getStoragePermission();
        MyApplication.getInstance().setEnterSystemUI(false);
        currentActivity = activity;
        //此处不能用（count == 0）判断，由于Activity跳转生命周期因素导致，已登录账号进入应用不会打开手势解锁
        if (!MyApplication.getInstance().getIsActive() && MyApplication.getInstance()
                .isIndexActivityRunning()) {
            MyApplication.getInstance().setIsActive(true);
            //当用通知打开特定Activity或者第一个打开的是SchemeActivity时，此处不作处理，交由SchemeActivity处理
            if (!MyApplication.getInstance().getOPenNotification() && !(activity instanceof SchemeHandleActivity)) {
                showSafeVerificationPage();
            }
            uploadMDMInfo(activity);
            new AppBadgeUtils(MyApplication.getInstance()).getAppBadgeCountFromServer();
        }
        count++;
    }

    private void getStoragePermission() {
        //如果没有存储权限则跳转到MainActivity进行处理
        if(!PermissionManagerUtils.getInstance().isHasPermission(MyApplication.getInstance(), Permissions.STORAGE)){
            IntentUtils.startActivity(currentActivity, MainActivity.class);
        }else {
            getPhonePermissions();
        }
    }

    private void getPhonePermissions() {
        //如果没有电话权限，则跳转到MainActivity进行处理
        if(!PermissionManagerUtils.getInstance().isHasPermission(MyApplication.getInstance(), Permission.READ_PHONE_STATE)){
            IntentUtils.startActivity(currentActivity, MainActivity.class);
        }
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
        if (count == 0 && !MyApplication.getInstance().isEnterSystemUI()) { // app 进入后台
            MyApplication.getInstance().setIsActive(false);
            startUploadPVCollectService(MyApplication.getInstance());
            startSyncCommonAppService(MyApplication.getInstance());
            new ClientIDUtils(MyApplication.getInstance()).upload();
            WebSocketPush.getInstance().closeWebsocket();
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

    public Activity getCurrentActivity(){
        return currentActivity;
    }

    /**
     * 弹出进入app安全验证界面
     *
     */
    private void showSafeVerificationPage() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FaceVerifyActivity.getFaceVerifyIsOpenByUser(MyApplication.getInstance())) {
                    Intent intent = new Intent(currentActivity, FaceVerifyActivity.class);
                    intent.putExtra("isFaceVerifyExperience",false);
                    currentActivity.startActivity(intent);
                } else if (getIsNeedGestureCode(MyApplication.getInstance())) {
                    Intent intent = new Intent(currentActivity, GestureLoginActivity.class);
                    intent.putExtra("gesture_code_change", "login");
                    currentActivity.startActivity(intent);
                }
            }
        },200);
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
        if (NetUtils.isNetworkConnected(context, false)) {
            new AppAPIService(context).uploadMDMInfo();
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
