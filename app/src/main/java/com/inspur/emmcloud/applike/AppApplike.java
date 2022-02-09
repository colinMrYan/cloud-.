package com.inspur.emmcloud.applike;

import com.inspur.emmcloud.baselib.applicationlike.IApplicationLike;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.componentservice.contact.ContactService;
import com.inspur.emmcloud.componentservice.web.WebMediaService;
import com.inspur.emmcloud.servcieimpl.AppServiceImpl;
import com.inspur.emmcloud.servcieimpl.CommunicationServiceImpl;
import com.inspur.emmcloud.servcieimpl.ContactServiceImpl;
import com.inspur.emmcloud.servcieimpl.MediaServiceImpl;

/**
 * Created by chenmch on 2019/5/31.
 */

public class AppApplike implements IApplicationLike {
    Router router = Router.getInstance();

    @Override
    public void onCreate() {
        router.addService(AppService.class, new AppServiceImpl());
        router.addService(CommunicationService.class, new CommunicationServiceImpl());
        router.addService(ContactService.class, new ContactServiceImpl());
        router.addService(WebMediaService.class, new MediaServiceImpl());
    }

    @Override
    public void onStop() {
        router.removeService(AppService.class);
        router.removeService(CommunicationService.class);
        router.removeService(ContactService.class);
        router.removeService(WebMediaService.class);
    }
}
