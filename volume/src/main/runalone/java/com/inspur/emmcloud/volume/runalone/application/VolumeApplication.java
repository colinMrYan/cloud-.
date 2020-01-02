package com.inspur.mvp_demo.runalone.application;


import com.inspur.emmcloud.basemodule.application.BaseApplication;

/**
 * Created by chenmch on 2019/5/7.
 */

public class VolumeApplication extends BaseApplication {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public String getIntentClassRouterAfterLogin() {
        return Constant.AROUTER_CLASS_VOLUME_HOME;
    }

}