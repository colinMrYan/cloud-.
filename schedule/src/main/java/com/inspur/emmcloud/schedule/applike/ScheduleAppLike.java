package com.inspur.emmcloud.schedule.applike;

import com.inspur.emmcloud.baselib.applicationlike.IApplicationLike;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.mail.MailService;
import com.inspur.emmcloud.schedule.serviceimpl.ScheduleServiceImpl;


/**
 * Created by libaochao on 2019/7/22.
 */

public class ScheduleAppLike implements IApplicationLike {

    Router router = Router.getInstance();

    @Override
    public void onCreate() {
        router.addService(MailService.class, new ScheduleServiceImpl());
    }

    @Override
    public void onStop() {
        router.removeService(MailService.class);
    }
}
