package com.inspur.emmcloud.servcieimpl;

import android.content.Intent;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.setting.SettingService;
import com.inspur.emmcloud.ui.mine.setting.CreateGestureActivity;
import com.inspur.emmcloud.ui.mine.setting.FaceVerifyActivity;
import com.inspur.emmcloud.ui.mine.setting.GestureLoginActivity;

/**
 * Created by chenmch on 2019/5/31.
 */

public class SettingServiceImpl implements SettingService {
    @Override
    public boolean isSetFaceOrGestureLock() {
        return isSetFaceLock() || isSetGestureLock();
    }

    @Override
    public void showFaceOrGestureLock() {
        if (isSetFaceLock()) {
            Intent intent = new Intent(MyApplication.getInstance(), FaceVerifyActivity.class);
            intent.putExtra("isFaceVerifyExperience", false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApplication.getInstance().startActivity(intent);
        } else if (isSetGestureLock()) {
            Intent intent = new Intent(MyApplication.getInstance(), GestureLoginActivity.class);
            intent.putExtra(GestureLoginActivity.GESTURE_CODE_CHANGE, "login");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MyApplication.getInstance().startActivity(intent);
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
        return FaceVerifyActivity.getFaceVerifyIsOpenByUser(MyApplication.getInstance());
    }

    private boolean isSetGestureLock() {
        return CreateGestureActivity.getGestureCodeIsOpenByUser(MyApplication.getInstance());
    }
}
