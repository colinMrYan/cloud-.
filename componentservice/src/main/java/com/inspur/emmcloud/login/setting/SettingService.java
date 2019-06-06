package com.inspur.emmcloud.login.setting;

import com.inspur.emmcloud.login.CoreService;

/**
 * Created by chenmch on 2019/5/31.
 */

public interface SettingService extends CoreService {
    boolean isSetFaceOrGestureLock();//是否开启刷脸和手势解锁

    void showFaceOrGestureLock();//展示刷脸或手势解锁页面

    void uploadMDMInfo();//上传MDM信息
}
