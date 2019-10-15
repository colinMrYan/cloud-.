package com.inspur.emmcloud.ui.chat.mvp.model.api;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;

import org.xutils.http.RequestParams;

import java.util.ArrayList;

/**
 * Created by libaochao on 2019/10/14.
 */

public class ApiServiceImpl implements ApiService.IGroupInfoActivity {

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
    public void setConversationStick(BaseModuleAPICallback apiCallback, boolean stickyState, String conversationId) {
        final String completeUrl = ApiUrl.getConversationSetStick(conversationId);
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addParameter("stick", stickyState);
        HttpUtils.request(CloudHttpMethod.PUT, params, apiCallback);
    }

    @Override
    public void setMuteNotification(BaseModuleAPICallback apiCallback, boolean muteNotificationState, String conversationId) {
        final String completeUrl = ApiUrl.getConversationSetDnd(conversationId);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.addParameter("dnd", muteNotificationState);
        HttpUtils.request(CloudHttpMethod.PUT, params, apiCallback);

    }

    @Override
    public void addGroupMembers(BaseModuleAPICallback apiCallback, ArrayList<String> uidList, String conversationId) {
        final String url = ApiUrl.getModifyGroupMemberUrl(conversationId);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        params.addParameter("members", uidList);
        params.setAsJsonContent(true);
        HttpUtils.request(CloudHttpMethod.POST, params, apiCallback);

    }

    @Override
    public void delGroupMembers(BaseModuleAPICallback apiCallback, ArrayList<String> uidList, String conversationId) {
        final String url = ApiUrl.getModifyGroupMemberUrl(conversationId);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(url);
        params.addParameter("members", JSONUtils.toJSONArray(uidList));
        params.setAsJsonContent(true);
        HttpUtils.request(CloudHttpMethod.DELETE, params, apiCallback);

    }

    @Override
    public void quitGroupChannel(BaseModuleAPICallback apiCallback, String conversationId) {
        String completeUrl = ApiUrl.getQuitChannelGroupUrl(conversationId);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        params.setAsJsonContent(true);
        HttpUtils.request(CloudHttpMethod.DELETE, params, apiCallback);
    }

    @Override
    public void delChannel(BaseModuleAPICallback apiCallback, String conversationId) {
        String completeUrl = APIUri.getDeleteChannelUrl(conversationId);
        RequestParams params = MyApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(CloudHttpMethod.DELETE, params, apiCallback);
    }

    public void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.refreshToken(oauthCallBack, requestTime);
        }
    }
}
