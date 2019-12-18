package com.inspur.emmcloud.servcieimpl;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MyAppAPIService;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.appcenter.AppCommonlyUse;
import com.inspur.emmcloud.componentservice.appcenter.AppcenterService;
import com.inspur.emmcloud.util.privates.cache.AppCacheUtils;

import java.util.List;

/**
 * Created by chenmch on 2019/5/31.
 */

public class AppcenterServiceImpl extends APIInterfaceInstance implements AppcenterService {
    @Override
    public void startSyncCommonAppService() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance()) && BaseApplication.getInstance().isHaveLogin()) {
            List<AppCommonlyUse> commonAppList = AppCacheUtils.getUploadCommonlyUseAppList(MyApplication.getInstance());
            if (commonAppList.size() > 0) {
                String commonAppListJson = JSONUtils.toJSONString(commonAppList);
                MyAppAPIService apiService = new MyAppAPIService(BaseApplication.getInstance());
                apiService.setAPIInterface(this);
                apiService.syncCommonApp(commonAppListJson);
            }
        }
    }


}
