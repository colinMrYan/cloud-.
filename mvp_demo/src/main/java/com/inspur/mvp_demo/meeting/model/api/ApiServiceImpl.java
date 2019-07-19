package com.inspur.mvp_demo.meeting.model.api;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;

import org.xutils.http.RequestParams;

public class ApiServiceImpl implements ApiService.IMeetingHistory {
    private static ApiServiceImpl instance;

    public static ApiServiceImpl getInstance() {
        if (instance == null) {
            synchronized (ApiServiceImpl.class) {
                if (instance == null) {
                    instance = new ApiServiceImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void getMeetingList(BaseModuleAPICallback apiCallback, int pageNum) {
        final String completeUrl = ApiUrl.getMeetingHistoryUrl(1);
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(CloudHttpMethod.GET, params, apiCallback);
    }

    public void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.refreshToken(oauthCallBack, requestTime);
        }
    }
}
