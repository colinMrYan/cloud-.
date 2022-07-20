package com.inspur.emmcloud.setting.serviceimpl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.AppConfig;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppConfigCacheUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.componentservice.setting.SettingService;
import com.inspur.emmcloud.setting.api.SettingAPIInterfaceImpl;
import com.inspur.emmcloud.setting.api.SettingAPIService;
import com.inspur.emmcloud.setting.ui.MoreFragment;
import com.inspur.emmcloud.setting.ui.setting.CreateGestureActivity;
import com.inspur.emmcloud.setting.ui.setting.FaceVerifyActivity;
import com.inspur.emmcloud.setting.ui.setting.GestureLoginActivity;
import com.inspur.emmcloud.setting.ui.setting.SettingActivity;
import com.inspur.emmcloud.setting.widget.DataCleanManager;

/**
 * Created by libaochao on 2019/12/25.
 */

public class SettingServiceImpl extends SettingAPIInterfaceImpl implements SettingService {


    @Override
    public Class getImpFragmentClass() {
        return MoreFragment.class;
    }

    @Override
    public boolean getGestureCodeIsOpenByUser(Context context) {
        return CreateGestureActivity.getGestureCodeIsOpenByUser(context);
    }

    @Override
    public boolean openWebRotate() {
        return saveWebAutoRotateConfig(true);
    }

    @Override
    public boolean closeWebRotate() {
        return saveWebAutoRotateConfig(false);
    }

    @Override
    public boolean clearWebCache() {
        DataCleanManager.cleanWebViewCache(BaseApplication.getInstance());
        BaseApplication.getInstance().removeAllSessionCookie();
        return true;
    }

    @Override
    public boolean openNativeRotate(Context context) {
        ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        return PreferencesUtils.putBoolean(context, Constant.PREF_APP_OPEN_NATIVE_ROTATE_SWITCH, true);
    }

    @Override
    public boolean closeNativeRotate(Context context) {
        ((Activity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return PreferencesUtils.putBoolean(context, Constant.PREF_APP_OPEN_NATIVE_ROTATE_SWITCH, false);
    }

    @Override
    public boolean isSetFaceOrGestureLock() {
        return isSetFaceLock() || isSetGestureLock();
    }

    @Override
    public void showFaceOrGestureLock() {
        if (isSetFaceLock()) {
            Intent intent = new Intent(BaseApplication.getInstance(), FaceVerifyActivity.class);
            intent.putExtra("isFaceVerifyExperience", false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            BaseApplication.getInstance().startActivity(intent);
        } else if (isSetGestureLock()) {
            Intent intent = new Intent(BaseApplication.getInstance(), GestureLoginActivity.class);
            intent.putExtra(GestureLoginActivity.GESTURE_CODE_CHANGE, "login");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            BaseApplication.getInstance().startActivity(intent);
        }
    }

    @Override
    public void closeOriginLockPage() {
        if (isSetFaceLock()) {
            BaseApplication.getInstance().closeActivity(FaceVerifyActivity.class.getSimpleName());
        } else if (isSetGestureLock()) {
            BaseApplication.getInstance().closeActivity(GestureLoginActivity.class.getSimpleName());
        }
    }


    private boolean isSetFaceLock() {
        return FaceVerifyActivity.getFaceVerifyIsOpenByUser(BaseApplication.getInstance());
    }

    private boolean isSetGestureLock() {
        return CreateGestureActivity.getGestureCodeIsOpenByUser(BaseApplication.getInstance());
    }

    private boolean saveWebAutoRotateConfig(boolean enable){
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
            AppConfig appConfig = new AppConfig(Constant.CONCIG_WEB_AUTO_ROTATE, String.valueOf(enable));
            AppConfigCacheUtils.saveAppConfig(BaseApplication.getInstance(), appConfig);
            SettingAPIService apiService = new SettingAPIService(BaseApplication.getInstance());
            apiService.saveWebAutoRotateConfig(enable);
            return true;
        }
        return false;
    }

}
