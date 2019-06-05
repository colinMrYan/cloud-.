package com.inspur.emmcloud.servcieimpl;

import android.content.Intent;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.componentservice.appcenter.AppcenterService;
import com.inspur.emmcloud.service.SyncCommonAppService;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.cache.DbCacheUtils;

/**
 * Created by chenmch on 2019/5/31.
 */

public class AppcenterServiceImpl implements AppcenterService {
    @Override
    public void startSyncCommonAppService() {
        if (!AppUtils.isServiceWork(MyApplication.getInstance(), SyncCommonAppService.class.getName()) && (!DbCacheUtils.isDbNull())) {
            Intent intent = new Intent();
            intent.setClass(MyApplication.getInstance(), SyncCommonAppService.class);
            MyApplication.getInstance().startService(intent);
        }
    }
}
