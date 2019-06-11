/**
 * MyAppAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.MyAppAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:31:55
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.appcenter.AppRedirectResult;
import com.inspur.emmcloud.bean.appcenter.GetAddAppResult;
import com.inspur.emmcloud.bean.appcenter.GetAllAppResult;
import com.inspur.emmcloud.bean.appcenter.GetAppGroupResult;
import com.inspur.emmcloud.bean.appcenter.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.bean.appcenter.GetRemoveAppResult;
import com.inspur.emmcloud.bean.appcenter.GetSearchAppResult;
import com.inspur.emmcloud.bean.appcenter.GetWebAppRealUrlResult;
import com.inspur.emmcloud.login.login.LoginService;
import com.inspur.emmcloud.login.login.OauthCallBack;
import com.luojilab.component.componentlib.router.Router;

import org.xutils.http.RequestParams;


/**
 * com.inspur.emmcloud.api.apiservice.MyAppAPIService create at 2016年11月8日
 * 下午2:31:55
 */
public class MyAppAPIService {
    private Context context;
    private APIInterface apiInterface;


    public MyAppAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    private void refreshToken(OauthCallBack oauthCallBack, long requestTime) {
        Router router = Router.getInstance();
        if (router.getService(LoginService.class.getSimpleName()) != null) {
            LoginService service = (LoginService) router.getService(LoginService.class.getSimpleName());
            service.refreshToken(oauthCallBack, requestTime);
        }
    }

    /**
     * 添加应用
     *
     * @param appID
     */
    public void addApp(final String appID) {
        final String completeUrl = APIUri.addApp();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("appID", appID);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addApp(appID);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }


            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnAddAppSuccess(new GetAddAppResult(new String(new String(arg0)), appID));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnAddAppFail(error, responseCode);
            }
        });

    }

    /**
     * 删除应用
     *
     * @param appID
     */
    public void removeApp(final String appID) {
        final String completeUrl = APIUri.removeApp();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("appID", appID);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        removeApp(appID);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface
                        .returnRemoveAppSuccess(new GetRemoveAppResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnRemoveAppFail(error, responseCode);
            }
        });
    }


    /**
     * 应用搜索
     */
    public void searchApp(final String keyword) {
        final String completeUrl = APIUri.getAllApps();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("keyword", keyword);
        params.addParameter("clientType", 0);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        searchApp(keyword);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnSearchAppSuccess(new GetSearchAppResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnSearchAppFail(error, responseCode);
            }
        });

    }





    /**
     * 获取用户所有apps
     */
    public void getUserApps(final String clientConfigMyAppVersion) {
        final String completeUrl = APIUri.getUserApps();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getUserApps(clientConfigMyAppVersion);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUserAppsSuccess(new GetAppGroupResult(new String(arg0)), clientConfigMyAppVersion);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUserAppsFail(error, responseCode);
            }
        });
    }


    /**
     * 获取所有应用
     */
    public void getNewAllApps() {
        final String completeUrl = APIUri.getNewAllApps();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAllAppsSuccess(new GetAllAppResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAllAppsFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getNewAllApps();
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
     * 应用身份认证
     *
     * @param urlParams
     */
    public void getAuthCode(final String requestUrl, final String urlParams) {
//        final String completeUrl = APIUri.getAppAuthCodeUri() + "?" + urlParams;
        final String completeUrl = requestUrl + "/oauth2.0/quick_authz_code" + "?" + urlParams;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
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

    /**
     * 根据应用id获取应用的详细信息
     *
     * @param appId
     */
    public void getAppInfo(final String appId) {
        final String completeUrl = APIUri.getAppInfo() + "?appID=" + appId;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnAppInfoSuccess(new App(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnAppInfoFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAppInfo(appId);
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
     * 获取真实的url地址
     *
     * @param url
     */
    public void getWebAppRealUrl(final String url) {
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnWebAppRealUrlSuccess(new GetWebAppRealUrlResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnWebAppRealUrlFail();
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getWebAppRealUrl(url);
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
     * 向服务端同步常用应用数据
     *
     * @param commonAppListJson
     */
    public void syncCommonApp(final String commonAppListJson) {
        final String url = APIUri.saveAppConfigUrl("CommonFunctions");
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.setBodyContent(commonAppListJson);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnSaveConfigSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnSaveConfigFail();
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        syncCommonApp(commonAppListJson);
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
     * 获取推荐应用小部件
     */
    public void getRecommendAppWidgetList() {
        final String completeUrl = APIUri.getMyAppWidgetsUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getRecommendAppWidgetList();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnRecommendAppWidgetListSuccess(new GetRecommendAppWidgetListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnRecommendAppWidgetListFail(error, responseCode);
            }
        });
    }



}
