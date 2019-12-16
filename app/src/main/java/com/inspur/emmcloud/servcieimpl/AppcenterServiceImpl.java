package com.inspur.emmcloud.servcieimpl;

import android.content.Intent;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.DbCacheUtils;
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
            Class syncCommonAppService = service.getSyncCommonAppService();
            if (!AppUtils.isServiceWork(MyApplication.getInstance(), syncCommonAppService.getName()) && (!DbCacheUtils.isDbNull())) {
                Intent intent = new Intent();
                intent.setClass(MyApplication.getInstance(), syncCommonAppService);
                MyApplication.getInstance().startService(intent);
            }
        }

    }
}
