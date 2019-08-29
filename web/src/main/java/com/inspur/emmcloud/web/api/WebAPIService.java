package com.inspur.emmcloud.web.api;

import android.content.Context;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;
import com.inspur.emmcloud.web.bean.AppRedirectResult;

import org.xutils.http.RequestParams;
import org.xutils.http.app.RedirectHandler;
import org.xutils.http.request.UriRequest;

/**
 * Created by chenmch on 2019/6/14.
 */

public class WebAPIService {
    private Context context;
    private WebAPIInterface apiInterface;

    public WebAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(WebAPIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    /**
     * 应用身份认证
     *
     * @param urlParams
     */
    public void getAuthCode(final String requestUrl, final String urlParams) {
        final String completeUrl = requestUrl + "/oauth2.0/quick_authz_code" + "?" + urlParams;
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGetAppAuthCodeResultSuccess(new AppRedirectResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetAppAuthCodeResultFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAuthCode(requestUrl, urlParams);
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

    private void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class) != null) {
            LoginService service = router.getService(LoginService.class);
            service.refreshToken(oauthCallBack, requestTime);
        }
    }


    public void webHttpGet(final String requestUrl) {
        RequestParams params = new RequestParams(requestUrl);
        params.setRedirectHandler(new RedirectHandler() {
            @Override
            public RequestParams getRedirectParams(UriRequest uriRequest) throws Throwable {
                String locationUrl = uriRequest.getResponseHeader("Location");
                RequestParams params = BaseApplication.getInstance().getHttpRequestParams(locationUrl);
                return params;
            }
        });
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, requestUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnWebHttpGetSuccess((new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnWebHttpGetFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        webHttpGet(requestUrl);
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
