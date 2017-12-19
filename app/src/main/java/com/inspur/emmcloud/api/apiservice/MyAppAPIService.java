/**
 * MyAppAPIService.java
 * classes : com.inspur.emmcloud.api.apiservice.MyAppAPIService
 * V 1.0.0
 * Create at 2016年11月8日 下午2:31:55
 */
package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

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
import com.inspur.emmcloud.bean.GetNewsTitleResult;
import com.inspur.emmcloud.bean.GetRecommendAppWidgetListResult;
import com.inspur.emmcloud.bean.GetRemoveAppResult;
import com.inspur.emmcloud.bean.GetSearchAppResult;
import com.inspur.emmcloud.bean.GetWebAppRealUrlResult;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileListResult;
import com.inspur.emmcloud.bean.Volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.Volume.GetVolumeListResult;
import com.inspur.emmcloud.bean.Volume.VolumeFile;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.util.OauthUtils;

import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

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

        final String completeUrl = APIUri.getHttpApiUrl("api/v0/content/news/section");
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

        final String completeUrl = APIUri.getHttpApiUrl("/api/v0/content/news/section/" + ncid + "/post?page=" + page + "&limit=20");
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
     *
     * @param commonAppListJson
     */
    public void syncCommonApp(final String commonAppListJson) {
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
     * 获取推荐应用小部件
     */
    public void getRecommendAppWidgetList() {
        final String completeUrl = APIUri.getMyAppWidgetsUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        x.http().get(params, new APICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire() {
                // TODO Auto-generated method stub
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void reExecute() {
                        // TODO Auto-generated method stub
                        getRecommendAppWidgetList();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(completeUrl);
            }

            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnRecommendAppWidgetListSuccess(new GetRecommendAppWidgetListResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnRecommendAppWidgetListFail(error, responseCode);
            }
        });
    }


/*****************************************************云盘**********************************************************/
    /**
     * 获取云盘列表
     */
    public void getVolumeList() {
        final String url = APIUri.getVolumeListUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        x.http().get(params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnVolumeListSuccess(new GetVolumeListResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeListFail(error, responseCode);
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
     *
     * @param volumeId
     * @param currentDirAbsolutePath
     */
    public void getVolumeFileList(final String volumeId, final String currentDirAbsolutePath) {
        final String url = APIUri.getVolumeFileOperationUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("path", currentDirAbsolutePath);
        x.http().get(params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnVolumeFileListSuccess(new GetVolumeFileListResult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileListFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void reExecute() {
                        getVolumeFileList(volumeId, currentDirAbsolutePath);
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
     * 获取文件上传Token
     *
     * @param volumeId
     * @param fileName
     * @param volumeFilePath
     */
    public void getVolumeFileUploadToken(final String fileName, final String volumeFilePath, final String localFilePath, final VolumeFile mockVolumeFile) {
        final String url = APIUri.getVolumeFileUploadSTSTokenUrl(mockVolumeFile.getVolume());
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("name", fileName);
        params.addParameter("path", volumeFilePath + fileName);
        x.http().post(params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnVolumeFileUploadTokenSuccess(new GetVolumeFileUploadTokenResult(arg0), localFilePath, mockVolumeFile);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileUploadTokenFail(mockVolumeFile, error, responseCode, localFilePath);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void reExecute() {
                        getVolumeFileUploadToken(fileName, volumeFilePath, localFilePath, mockVolumeFile);
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
     * 移动云盘文件
     *
     * @param volumeId
     * @param currentDirAbsolutePath
     */
    public void moveVolumeFile(final String volumeId, final String currentDirAbsolutePath, final List<VolumeFile> moveVolumeFileList, final String toPath) {
        final String url = APIUri.getMoveVolumeFileUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("to", toPath);
        params.addQueryStringParameter("from", currentDirAbsolutePath + moveVolumeFileList.get(0).getName());
        x.http().request(HttpMethod.PUT, params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnMoveFileSuccess(moveVolumeFileList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnMoveFileFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {

                    @Override
                    public void reExecute() {
                        moveVolumeFile(volumeId, currentDirAbsolutePath, moveVolumeFileList, toPath);
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
     *
     * @param volumeId
     * @param forderName
     * @param currentDirAbsolutePath
     */
    public void createForder(final String volumeId, final String forderName, final String currentDirAbsolutePath) {
        final String url = APIUri.getCreateForderUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("path", currentDirAbsolutePath + forderName);
        x.http().post(params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnCreateForderSuccess(new VolumeFile(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnCreateForderFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createForder(volumeId, forderName, currentDirAbsolutePath);
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
     * 删除文件
     *
     * @param volumeId
     * @param fileName
     * @param currentDirAbsolutePath
     */
    public void volumeFileDelete(final String volumeId, final VolumeFile volumeFile, final String currentDirAbsolutePath) {
        final String url = APIUri.getVolumeFileOperationUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("path", currentDirAbsolutePath + volumeFile.getName());
        x.http().request(HttpMethod.DELETE, params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnVolumeFileDeleteSuccess(volumeFile);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileDeleteFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        volumeFileDelete(volumeId, volumeFile, currentDirAbsolutePath);
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
     * 文件重命名
     *
     * @param volumeId
     * @param volumeFile
     * @param currentDirAbsolutePath
     */
    public void volumeFileRename(final String volumeId, final VolumeFile volumeFile, final String currentDirAbsolutePath, final String fileNewName) {
        final String url = APIUri.getVolumeFileRenameUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("path", currentDirAbsolutePath + volumeFile.getName());
        params.addQueryStringParameter("name", fileNewName);
        x.http().request(HttpMethod.PUT, params, new APICallback(context, url) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnVolumeFileRenameSuccess(volumeFile, fileNewName);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileRenameFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        volumeFileRename(volumeId, volumeFile, currentDirAbsolutePath, fileNewName);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                }, context).refreshToken(url);
            }
        });
    }
}
