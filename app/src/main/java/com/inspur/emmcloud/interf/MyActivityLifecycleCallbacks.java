package com.inspur.emmcloud.interf;

import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.service.SyncCommonAppService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.ui.SchemeHandleActivity;
import com.inspur.emmcloud.ui.mine.setting.CreateGestureActivity;
import com.inspur.emmcloud.ui.mine.setting.FaceVerifyActivity;
import com.inspur.emmcloud.ui.mine.setting.GestureLoginActivity;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.systool.emmpermission.EmmPermissionActivity;
import com.inspur.emmcloud.util.common.systool.emmpermission.Permissions;
import com.inspur.emmcloud.util.common.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.util.privates.AppBadgeUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.ClientIDUtils;
import com.inspur.emmcloud.util.privates.cache.DbCacheUtils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by chenmch on 2017/9/13.
 */

public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private int count = 0;
    private Activity currentActivity;
    private WebService webService = new WebService();

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        MyApplication.getInstance().addActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        MyApplication.getInstance().setEnterSystemUI(false);
        currentActivity = activity;
        count++;
        //检查是否有必要权限，如果有则继续下面逻辑，如果没有则转到MainActivity
        // if (isLackNecessaryPermission()) {
        // return;
        // }
        //此处不能用（count == 0）判断，由于Activity跳转生命周期因素导致，已登录账号进入应用不会打开手势解锁
        if (!MyApplication.getInstance().getIsActive() && MyApplication.getInstance().isIndexActivityRunning()) {
            MyApplication.getInstance().setIsActive(true);
            //当用通知打开特定Activity或者第一个打开的是SchemeActivity时，此处不作处理，交由SchemeActivity处理
            if (!MyApplication.getInstance().getOPenNotification() && !(activity instanceof SchemeHandleActivity)) {
                showSafeVerificationPage();
            }
            uploadMDMInfo(activity);
            new AppBadgeUtils(MyApplication.getInstance()).getAppBadgeCountFromServer();
        }
        //防止应用防止时间很久，Application被销毁后isActive变量被置为默认false
        if (activity instanceof IndexActivity) {
            MyApplication.getInstance().setIsActive(true);
        }
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

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    /**
     * 弹出进入app安全验证界面
     */
    private void showSafeVerificationPage() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FaceVerifyActivity.getFaceVerifyIsOpenByUser(MyApplication.getInstance())) {
                    Intent intent = new Intent(currentActivity, FaceVerifyActivity.class);
                    intent.putExtra("isFaceVerifyExperience", false);
                    currentActivity.startActivity(intent);
                } else if (getIsNeedGestureCode(MyApplication.getInstance())) {
                    Intent intent = new Intent(currentActivity, GestureLoginActivity.class);
                    intent.putExtra("gesture_code_change", "login");
                    currentActivity.startActivity(intent);
                }
            }
        }, 200);
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
            appAPIService.setAPIInterface(webService);
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


    private class WebService extends APIInterfaceInstance {

//        @Override
//        public void returnUploadMDMInfoSuccess(UploadMDMInfoResult uploadMDMInfoResult) {
//            PreferencesByUserAndTanentUtils.putInt(MyApplication.getInstance(), Constant.PREF_MNM_DOUBLE_VALIADATION, uploadMDMInfoResult.getDoubleValidation());
//        }
    }
}
