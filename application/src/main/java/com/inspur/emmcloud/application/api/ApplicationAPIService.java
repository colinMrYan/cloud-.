/**
 * MyAppAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.MyAppAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:31:55
 */
package com.inspur.emmcloud.application.api;

import android.content.Context;

import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.application.bean.GetAddAppResult;
import com.inspur.emmcloud.application.bean.GetAllAppResult;
import com.inspur.emmcloud.application.bean.GetAppGroupResult;
import com.inspur.emmcloud.application.bean.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.application.bean.GetRemoveAppResult;
import com.inspur.emmcloud.application.bean.GetSearchAppResult;
import com.inspur.emmcloud.application.bean.GetWebAppRealUrlResult;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;

import org.xutils.http.RequestParams;


public class ApplicationAPIService {
    private Context context;
    private ApplicationAPIInterface apiInterface;

    public ApplicationAPIService(Context context) {
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
     * 添加应用
     *
     * @param appID
     */
    public void addApp(final String appID) {
        final String completeUrl = ApplicationAPIUri.addApp();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
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
        final String completeUrl = ApplicationAPIUri.removeApp();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
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
        final String completeUrl = ApplicationAPIUri.getAllApps();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
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
     * 根据应用id获取应用的详细信息
     *
     * @param appId
     */
    public void getAppInfo(final String appId) {
        final String completeUrl = ApplicationAPIUri.getAppInfo() + "?appID=" + appId;
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
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
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
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
        final String url = ApplicationAPIUri.saveAppConfigUrl("CommonFunctions");
        RequestParams params = ((BaseApplication) context.getApplicationContext()).getHttpRequestParams(url);
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
        final String completeUrl = ApplicationAPIUri.getMyAppWidgetsUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
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

    /**
     * 验证行政审批密码
     *
     * @param password
     */
    public void veriryApprovalPassword(String userName, final String password) {
        String completeUrl = ApplicationAPIUri.getVeriryApprovalPasswordUrl();
        RequestParams params = new RequestParams(completeUrl);
        params.addQueryStringParameter("userName", userName);
        params.addQueryStringParameter("userPass", password);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                if (arg0 != null && new String(arg0).equals("登录成功")) {
                    apiInterface.returnVeriryApprovalPasswordSuccess(password);
                } else {
                    apiInterface.returnVeriryApprovalPasswordFail("", -1);
                }

            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVeriryApprovalPasswordFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                apiInterface.returnVeriryApprovalPasswordFail("", -1);
            }
        });
    }

    /**
     * 获取所有应用
     */
    public void getNewAllApps() {
        final String completeUrl = ApplicationAPIUri.getNewAllApps();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
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
     * 获取用户所有apps
     */
    public void getUserApps(final String clientConfigMyAppVersion) {
        final String completeUrl = ApplicationAPIUri.getUserApps();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
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

}
