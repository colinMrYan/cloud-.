package com.inspur.emmcloud.servcieimpl;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.componentservice.appcenter.AppcenterService;
import com.inspur.emmcloud.componentservice.appcenter.ApplicationService;

/**
 * Created by chenmch on 2019/5/31.
 */

public class AppcenterServiceImpl implements AppcenterService {
    @Override
    public void startSyncCommonAppService() {
        Router router = Router.getInstance();
        if (router.getService(ApplicationService.class) != null) {
            ApplicationService service = router.getService(ApplicationService.class);
            service.syncCommonApp();
        }

    }
}
