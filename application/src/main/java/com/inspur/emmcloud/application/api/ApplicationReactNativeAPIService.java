package com.inspur.emmcloud.application.api;

import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.application.GetClientIdRsult;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;

import org.xutils.http.RequestParams;

/**
 * Created by yufuchang on 2017/3/28.
 */

public class ApplicationReactNativeAPIService {
    private Context context;
    private ApplicationAPIInterface apiInterface;

    public ApplicationReactNativeAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(ApplicationAPIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    private void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.refreshToken(oauthCallBack, requestTime);
        }
    }

    /**
     * 获取ClientId
     *
     * @param deviceId
     * @param deviceName
     */
    public void getClientId(final String deviceId, final String deviceName) {
        final String completeUrl = ApplicationAPIUri.getClientId();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("deviceId", deviceId);
        params.addParameter("deviceName", deviceName);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGetClientIdResultSuccess(new GetClientIdRsult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetClientIdResultFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getClientId(deviceId, deviceName);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }



    /**
     * 写回版本变更情况
     *
     * @param preVersion
     * @param currentVersion
     * @param clientId
     * @param command
     * @param appId
     */
    public void writeBackVersionChange(final String preVersion, final String currentVersion, final String clientId, final String command, final String appId) {
        final String completeUrl = ApplicationAPIUri.getReactNativeWriteBackUrl(appId) + "?preVersion=" + preVersion + "&currentVersion=" + currentVersion + "&clientId=" + clientId +
                "&command=" + command;
        RequestParams params = ((BaseApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                LogUtils.YfcDebug("写回成功，不需要后续处理");
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                LogUtils.YfcDebug("写回失败，不需要后续处理");
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        writeBackVersionChange(preVersion, currentVersion, clientId, command, appId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }




}
