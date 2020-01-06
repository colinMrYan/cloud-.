package com.inspur.emmcloud.setting.serviceimpl;

import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.setting.SettingService;
import com.inspur.emmcloud.setting.api.SettingAPIInterfaceImpl;
import com.inspur.emmcloud.setting.ui.MoreFragment;
import com.inspur.emmcloud.setting.ui.setting.CreateGestureActivity;
import com.inspur.emmcloud.setting.ui.setting.FaceVerifyActivity;
import com.inspur.emmcloud.setting.ui.setting.GestureLoginActivity;

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


}
