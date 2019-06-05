package com.inspur.emmcloud.servcieimpl;

import android.content.Intent;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.componentservice.setting.SettingService;
import com.inspur.emmcloud.service.PVCollectService;
import com.inspur.emmcloud.ui.mine.setting.CreateGestureActivity;
import com.inspur.emmcloud.ui.mine.setting.FaceVerifyActivity;
import com.inspur.emmcloud.ui.mine.setting.GestureLoginActivity;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.cache.DbCacheUtils;

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
            MyApplication.getInstance().startActivity(intent);
        } else if (isSetGestureLock()) {
            Intent intent = new Intent(MyApplication.getInstance(), GestureLoginActivity.class);
            intent.putExtra("gesture_code_change", "login");
            MyApplication.getInstance().startActivity(intent);
        }
    }

    @Override
    public void uploadMDMInfo() {
        if (!AppUtils.isServiceWork(MyApplication.getInstance(), PVCollectService.class.getName()) && (!DbCacheUtils.isDbNull())) {
            Intent intent = new Intent();
            intent.setClass(MyApplication.getInstance(), PVCollectService.class);
            MyApplication.getInstance().startService(intent);
        }
    }


    private boolean isSetFaceLock() {
        return FaceVerifyActivity.getFaceVerifyIsOpenByUser(MyApplication.getInstance());
    }

    private boolean isSetGestureLock() {
        return CreateGestureActivity.getGestureCodeIsOpenByUser(MyApplication.getInstance());
    }
}
