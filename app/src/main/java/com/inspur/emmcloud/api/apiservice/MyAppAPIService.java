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
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.AppRedirectResult;
import com.inspur.emmcloud.bean.GetAddAppResult;
import com.inspur.emmcloud.bean.GetAllAppResult;
import com.inspur.emmcloud.bean.GetAppBadgeResult;
import com.inspur.emmcloud.bean.GetAppGroupResult;
import com.inspur.emmcloud.bean.GetGroupNewsDetailResult;
import com.inspur.emmcloud.bean.GetMyAppResult;
import com.inspur.emmcloud.bean.GetNewsTitleResult;
import com.inspur.emmcloud.bean.GetRemoveAppResult;
import com.inspur.emmcloud.bean.GetSearchAppResult;
import com.inspur.emmcloud.bean.GetWebAppRealUrlResult;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileListResult;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileUploadSTSTokenResult;
import com.inspur.emmcloud.bean.Volume.GetVolumeListResult;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.callback.OauthCallBack;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.OauthUtils;
import com.inspur.emmcloud.util.UriUtils;

import org.xutils.http.HttpMethod;
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

        x.http().post(params, new APICallback(context, completeUrl) {

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
                    apiInterface.returnAllAppsFail(error, responseCode);
                } else if (type == TYPE_REFRESH) {
                    apiInterface.returnAllAppsFreshFail(error, responseCode);
                } else {
                    apiInterface.returnAllAppsMoreFail(error, responseCode);
                }
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAllApps(type, pageNumber);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
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
        x.http().post(params, new APICallback(context, completeUrl) {
            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        addApp(appID);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnAddAppSuccess(new GetAddAppResult(arg0,
                        appID));
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
        x.http().post(params, new APICallback(context, completeUrl) {
            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        removeApp(appID);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                apiInterface
                        .returnRemoveAppSuccess(new GetRemoveAppResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnRemoveAppFail(error, responseCode);
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
        x.http().post(params, new APICallback(context, completeUrl) {
            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getMyApp(jsonArray);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnMyAppSuccess(new GetMyAppResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnMyAppFail(error, responseCode);
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
        x.http().post(params, new APICallback(context, completeUrl) {
            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        searchApp(keyword);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnSearchAppSuccess(new GetSearchAppResult(
                        arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnSearchAppFail(error, responseCode);
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
        x.http().get(params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire() {
                // TODO Auto-generated method stub
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void reExecute() {
                        getNewsTitles();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
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
                LogUtils.YfcDebug("错误代码：" + responseCode);
                apiInterface.returnGroupNewsTitleFail(error, responseCode);
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

        final String completeUrl = UriUtils.getHttpApiUri("/api/v0/content/news/section/" + ncid + "/post?page=" + page + "&limit=20");
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);

        x.http().get(params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire() {
                // TODO Auto-generated method stub
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void reExecute() {
                        getGroupNewsDetail(ncid, page);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnGroupNewsDetailSuccess(new GetGroupNewsDetailResult(
                                arg0), page);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnGroupNewsDetailFail(error, responseCode, page);
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
        x.http().post(params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire() {
                // TODO Auto-generated method stub
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void reExecute() {
                        // TODO Auto-generated method stub
                        getUserApps();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnUserAppsSuccess(new GetAppGroupResult(arg0));
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

        x.http().post(params, new APICallback(context, completeUrl) {

            @Override
            public void callbackSuccess(String arg0) {
                // TODO Auto-generated method stub
                apiInterface.returnAllAppsSuccess(new GetAllAppResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                // TODO Auto-generated method stub
                apiInterface.returnAllAppsFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getNewAllApps();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
            }

        });

    }

    /**
     * 应用身份认证
     *
     * @param urlParams
     */
    public void getAuthCode(final String urlParams) {
        final String completeUrl = APIUri.getAppAuthCodeUri() + "?" + urlParams;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        x.http().get(params, new APICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnGetAppAuthCodeResultSuccess(new AppRedirectResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetAppAuthCodeResultFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAuthCode(urlParams);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
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
        x.http().get(params, new APICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnAppInfoSuccess(new App(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnAppInfoFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAppInfo(appId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
            }
        });
    }

    /**
     * 获取所有app的未处理消息
     */
    public void getAppBadgeNum() {
        final String completeUrl = APIUri.getAppBadgeNumUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(completeUrl);
        x.http().get(params, new APICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnGetAppBadgeResultSuccess(new GetAppBadgeResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetAppBadgeResultFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getAppBadgeNum();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
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
        x.http().get(params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnWebAppRealUrlSuccess(new GetWebAppRealUrlResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnWebAppRealUrlFail();
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getWebAppRealUrl(url);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(url);

            }
        });
    }

    /**
     * 向服务端同步常用应用数据
     * @param commonAppListJson
     */
    public void syncCommonApp(final String commonAppListJson){
        final String url = APIUri.saveAppConfigUrl("CommonFunctions");
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.setBodyContent(commonAppListJson);
        x.http().post(params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnSaveConfigSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnSaveConfigFail();
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void reExecute() {
                        syncCommonApp(commonAppListJson);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(url);
            }
        });
    }


    /**
     * 获取云盘列表
     */
    public void getVolumeList(){
        final String url = APIUri.getVolumeListUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        x.http().get(params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnVolumeListSuccess(new GetVolumeListResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeListFail(error,responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void reExecute() {
                        getVolumeList();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(url);
            }
        });
    }

    /**
     * 获取云盘文件列表
     * @param volumeId
     * @param absolutePath
     */
    public void getVolumeFileList(final String volumeId,final String absolutePath){
        final String url = APIUri.getVolumeFileOperationUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("path",absolutePath);
        x.http().get(params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnVolumeFileListSuccess(new GetVolumeFileListResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileListFail(error,responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void reExecute() {
                        getVolumeFileList(volumeId,absolutePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(url);
            }
        });
    }

    /**
     * 获取阿里云上传STS Token
     * @param volumeId
     * @param fileName
     * @param volumeFilePath
     */
    public void getVolumeFileUploadSTSToken(final String volumeId,final String fileName,final String volumeFilePath,final String localFilePath){
        final String url = APIUri.getVolumeFileUploadSTSTokenUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("name",fileName);
        params.addParameter("path",volumeFilePath);
        x.http().post(params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnVolumeFileUploadSTSTokenSuccess(new GetVolumeFileUploadSTSTokenResult(arg0),localFilePath);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileUploadSTSTokenFail(error,responseCode,localFilePath);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void reExecute() {
                        getVolumeFileUploadSTSToken(volumeId,fileName,volumeFilePath,localFilePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(url);
            }
        });
    }

    /**
     * 创建文件夹
     * @param volumeId
     * @param forderName
     * @param absolutePath
     */
    public void createForder(final String volumeId,final String forderName,final String absolutePath){
        final String url = APIUri.getCreateForderUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("path",absolutePath+forderName);
        x.http().post(params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnCreateForderSuccess(new VolumeFile(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnCreateForderFail(error,responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createForder(volumeId,forderName,absolutePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                },context).refreshToken(url);
            }
        });
    }

    /**
     * 删除文件
     * @param volumeId
     * @param fileName
     * @param absolutePath
     */
    public void deleteFile(final String volumeId,final VolumeFile volumeFile,final String absolutePath){
        final String url = APIUri.getVolumeFileOperationUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("path",absolutePath+volumeFile.getName());
        x.http().request(HttpMethod.DELETE, params, new APICallback(context,url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnDeleteFileSuccess(volumeFile);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnDeleteFileFail(error,responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        deleteFile(volumeId,volumeFile,absolutePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                },context).refreshToken(url);
            }
        });
    }
}
