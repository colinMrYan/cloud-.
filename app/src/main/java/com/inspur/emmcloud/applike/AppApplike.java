package com.inspur.emmcloud.applike;

import com.inspur.emmcloud.baselib.applicationlike.IApplicationLike;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.componentservice.appcenter.AppcenterService;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.setting.SettingService;
import com.inspur.emmcloud.servcieimpl.AppServiceImpl;
import com.inspur.emmcloud.servcieimpl.AppcenterServiceImpl;
import com.inspur.emmcloud.servcieimpl.CommunicationServiceImpl;
import com.inspur.emmcloud.servcieimpl.ContactServiceImpl;
import com.inspur.emmcloud.servcieimpl.SettingServiceImpl;

/**
 * Created by chenmch on 2019/5/31.
 */

public class AppApplike implements IApplicationLike {
    Router router = Router.getInstance();

    @Override
    public void onCreate() {
        router.addService(AppcenterService.class, new AppcenterServiceImpl());
        router.addService(SettingService.class, new SettingServiceImpl());
        router.addService(AppService.class, new AppServiceImpl());
        router.addService(CommunicationService.class, new CommunicationServiceImpl());
        router.addService(ContactService.class, new ContactServiceImpl());
    }

    @Override
    public void onStop() {
        router.removeService(AppcenterService.class);
        router.removeService(SettingService.class);
        router.removeService(AppService.class);
        router.removeService(CommunicationService.class);
        router.removeService(ContactService.class);
        router.removeService(MailService.class);
    }
}
