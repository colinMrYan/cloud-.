/**
 * MyAppAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.MyAppAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:31:55
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.google.gson.JsonArray;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.AppRedirectResult;
import com.inspur.emmcloud.bean.GetAddAppResult;
import com.inspur.emmcloud.bean.GetAllAppResult;
import com.inspur.emmcloud.bean.GetAppGroupResult;
import com.inspur.emmcloud.bean.GetGroupNewsDetailResult;
import com.inspur.emmcloud.bean.GetMyAppResult;
import com.inspur.emmcloud.bean.GetNewsTitleResult;
import com.inspur.emmcloud.bean.GetRemoveAppResult;
import com.inspur.emmcloud.bean.GetSearchAppResult;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.OauthCallBack;
import com.inspur.emmcloud.util.OauthUtils;
import com.inspur.emmcloud.util.UriUtils;

import org.xutils.http.RequestParams;
import org.xutils.x;

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

    /**
     * 获取所有应用
     *
     * @param type
     * @param pageNumber
     */
    public void getAllApps(final int type, final int pageNumber) {
        final int TYPE_NORMAL = 3;
        final int TYPE_REFRESH = 4;
        final int TYPE_MORE = 5;
        final String completeUrl = APIUri.getAllApps();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("pageNumber", pageNumber + "");

        x.http().post(params, new APICallback(context,completeUrl) {

            @Override
            public void callbackSuccess(String arg0) {
                // TODO Auto-generated method stub
                if (type == TYPE_NORMAL) {
                    apiInterface
                            .returnAllAppsSuccess(new GetAllAppResult(arg0));
                } else if (type == TYPE_REFRESH) {
                    apiInterface.returnAllAppsFreshSuccess(new GetAllAppResult(
                            arg0));
                } else {
                    apiInterface.returnAllAppsMoreSuccess(new GetAllAppResult(
                            arg0));
                }
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                if (type == TYPE_NORMAL) {
                    apiInterface.returnAllAppsFail(error);
                } else if (type == TYPE_REFRESH) {
                    apiInterface.returnAllAppsFreshFail(error);
                } else {
                    apiInterface.returnAllAppsMoreFail(error);
                }
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        getAllApps(type, pageNumber);
                    }
                }, context).refreshTocken(completeUrl);
            }

        });

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
        x.http().post(params, new APICallback(context,completeUrl) {
            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        addApp(appID);
                    }
                }, context).refreshTocken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnAddAppSuccess(new GetAddAppResult(arg0,
                        appID));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnAddAppFail(error);
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
        x.http().post(params, new APICallback(context,completeUrl) {
            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        removeApp(appID);
                    }
                }, context).refreshTocken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                apiInterface
                        .returnRemoveAppSuccess(new GetRemoveAppResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnRemoveAppFail(error);
            }
        });
    }

    /**
     * 获取我的应用
     *
     * @param jsonArray
     */
    public void getMyApp(final JsonArray jsonArray) {
        final String completeUrl = APIUri.getMyApp();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        x.http().post(params, new APICallback(context,completeUrl) {
            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        getMyApp(jsonArray);
                    }
                }, context).refreshTocken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnMyAppSuccess(new GetMyAppResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnMyAppFail(error);
            }
        });

    }

    /**
     * 应用搜索
     *
     */
    public void searchApp(final String keyword, final int pageNumber) {
        final String completeUrl = APIUri.getAllApps();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("keyword", keyword);
        params.addParameter("clientType", 0);
        params.addParameter("pageNumber", pageNumber);
        x.http().post(params, new APICallback(context,completeUrl) {
            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        searchApp(keyword, pageNumber);
                    }
                }, context).refreshTocken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                if (pageNumber > 1) {
                    apiInterface.returnSearchAppSuccess(new GetSearchAppResult(
                            arg0));
                } else {
                    apiInterface.returnSearchAppSuccess(new GetSearchAppResult(
                            arg0));
                }
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                if (pageNumber > 1) {
                    apiInterface.returnSearchAppMoreFail(error);
                } else {
                    apiInterface.returnSearchAppFail(error);
                }
            }
        });

    }


    /***********************************集团新闻**************************************************************/
    /**
     * 获取集团新闻标题
     */
    public void getNewsTitles() {

        final String completeUrl = UriUtils.getHttpApiUri("api/v0/content/news/section");
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        x.http().get(params, new APICallback(context,completeUrl) {

            @Override
            public void callbackTokenExpire() {
                // TODO Auto-generated method stub
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void execute() {
                        getNewsTitles();
                    }
                }, context).refreshTocken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnGroupNewsTitleSuccess(new GetNewsTitleResult(
                                arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                LogUtils.YfcDebug("错误代码："+responseCode);
                apiInterface.returnGroupNewsTitleFail(error);
            }
        });
    }


    /**
     * 请求每个标题下的新闻列表
     *
     * @param ncid
     * @param page
     */
    public void getGroupNewsDetail(final String ncid, final int page) {

        final String completeUrl = UriUtils.getHttpApiUri("/api/v0/content/news/section/"+ncid+"/post?page="+page+"&limit=20");
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);

        x.http().get(params, new APICallback(context,completeUrl) {

            @Override
            public void callbackTokenExpire() {
                // TODO Auto-generated method stub
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void execute() {
                        getGroupNewsDetail(ncid, page);
                    }
                }, context).refreshTocken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnGroupNewsDetailSuccess(new GetGroupNewsDetailResult(
                                arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnGroupNewsDetailFail(error);
            }
        });

    }

    /**
     * 获取用户所有apps
     */
    public void getUserApps() {
        final String completeUrl = APIUri.getUserApps();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        x.http().post(params, new APICallback(context,completeUrl) {

            @Override
            public void callbackTokenExpire() {
                // TODO Auto-generated method stub
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void execute() {
                        // TODO Auto-generated method stub
                        getUserApps();
                    }
                }, context).refreshTocken(completeUrl);
                ;
            }

            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnUserAppsSuccess(new GetAppGroupResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUserAppsFail(error);
            }
        });
    }


    /**
     * 获取所有应用
     *
     */
    public void getNewAllApps() {
        final String completeUrl = APIUri.getNewAllApps();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);

        x.http().post(params, new APICallback(context,completeUrl) {

            @Override
            public void callbackSuccess(String arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAllAppsSuccess(new GetAllAppResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAllAppsFail(error);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        getNewAllApps();
                    }
                }, context).refreshTocken(completeUrl);
            }

        });

    }

    /**
     * 应用身份认证
     * @param urlParams
     */
    public void getAuthCode(final String urlParams) {
        final String completeUrl = APIUri.getAppAuthCodeUri() + "?" + urlParams;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        x.http().get(params, new APICallback(context,completeUrl) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnGetAppAuthCodeResultSuccess(new AppRedirectResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetAppAuthCodeResultFail(error);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        getAuthCode(urlParams);
                    }
                }, context).refreshTocken(completeUrl);
            }
        });
    }

    /**
     * 获取真实的url地址
     * @param url
     */
    public void getReallyUrl(final String url){
//        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
//        x.http().get(params, new Callback.CommonCallback<String>() {
//            @Override
//            public void onSuccess(String s) {
//                String reallyUrl = JSONUtils.getString(s,"uri","");
//                openWebApp(activity,reallyUrl,app);
//            }
//
//            @Override
//            public void onError(Throwable throwable, boolean b) {
//                ToastUtils.show(activity, R.string.react_native_app_open_failed);
//            }
//
//            @Override
//            public void onCancelled(CancelledException e) {
//
//            }
//
//            @Override
//            public void onFinished() {
//                loadingDialog.dismiss();
//            }
//        });
    }

}
