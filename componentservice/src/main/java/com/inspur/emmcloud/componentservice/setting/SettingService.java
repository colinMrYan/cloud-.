package com.inspur.emmcloud.componentservice.setting;

import android.content.Context;

import com.inspur.emmcloud.componentservice.CoreService;

/**
 * Created by chenmch on 2019/5/31.
 */

public interface SettingService extends CoreService {

    Class getImpFragmentClass();

    boolean isSetFaceOrGestureLock();//是否开启刷脸和手势解锁

    void showFaceOrGestureLock();//展示刷脸或手势解锁页面

    void closeOriginLockPage();

    boolean getGestureCodeIsOpenByUser(Context context);

    boolean openWebRotate();// 打开Web旋转

    boolean closeWebRotate();// 关闭Web旋转

    boolean clearWebCache();// 清除web缓存

}
