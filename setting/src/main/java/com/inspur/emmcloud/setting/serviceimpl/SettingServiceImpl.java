package com.inspur.emmcloud.setting.serviceimpl;

import com.inspur.emmcloud.componentservice.setting.SettingService;
import com.inspur.emmcloud.setting.api.SettingAPIInterfaceImpl;

/**
 * Created by libaochao on 2019/12/25.
 */

public class SettingServiceImpl extends SettingAPIInterfaceImpl implements SettingService {
    @Override
    public boolean isSetFaceOrGestureLock() {
        return false;
    }

    @Override
    public void showFaceOrGestureLock() {

    }

    @Override
    public void closeOriginLockPage() {

    }
}
