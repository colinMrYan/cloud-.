package com.inspur.emmcloud.schedule.runalone.application;


import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;

/**
 * Created by chenmch on 2019/5/7.
 */

public class ScheduleApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public String getIntentClassRouterAfterLogin() {
        return Constant.AROUTER_CLASS_SCHEDLE_TEST;
    }
}
