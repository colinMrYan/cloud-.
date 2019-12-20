package com.inspur.emmcloud.servcieimpl;

import android.app.Activity;
import android.os.Bundle;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.componentservice.appcenter.AppcenterService;
import com.inspur.emmcloud.componentservice.appcenter.ApplicationService;
import com.inspur.emmcloud.ui.appcenter.ReactNativeAppActivity;

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

    @Override
    public void startReactNativeApp(Activity activity, Bundle bundle) {
        IntentUtils.startActivity(activity, ReactNativeAppActivity.class, bundle);
    }
}
