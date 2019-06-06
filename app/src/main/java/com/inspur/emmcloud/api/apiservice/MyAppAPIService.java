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
import com.inspur.emmcloud.bean.appcenter.news.GetGroupNewsDetailResult;
import com.inspur.emmcloud.bean.appcenter.news.GetNewsTitleResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileListResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeFileUploadTokenResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeGroupPermissionResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeGroupResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeListResult;
import com.inspur.emmcloud.bean.appcenter.volume.GetVolumeResultWithPermissionResult;
import com.inspur.emmcloud.bean.appcenter.volume.Volume;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeDetail;
import com.inspur.emmcloud.bean.appcenter.volume.VolumeFile;
import com.inspur.emmcloud.login.login.OauthCallBack;
import com.inspur.emmcloud.util.privates.OauthUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

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
                OauthUtils.getInstance().refreshToken(
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
                OauthUtils.getInstance().refreshToken(
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
                OauthUtils.getInstance().refreshToken(
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


    /***********************************集团新闻**************************************************************/
    /**
     * 获取集团新闻标题
     */
    public void getNewsTitles() {

        final String completeUrl = APIUri.getGroupNewsUrl("/content/news/section");
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getNewsTitles();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnGroupNewsTitleSuccess(new GetNewsTitleResult(new String(arg0)));
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

        final String completeUrl = APIUri.getGroupNewsUrl("/content/news/section/" + ncid + "/post?page=" + page + "&limit=20");
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getGroupNewsDetail(ncid, page);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

            @Override
            public void callbackSuccess(byte[] arg0) {
                // TODO Auto-generated method stub
                apiInterface
                        .returnGroupNewsDetailSuccess(new GetGroupNewsDetailResult(new String(arg0)), page);
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
                OauthUtils.getInstance().refreshToken(
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
                OauthUtils.getInstance().refreshToken(
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
                OauthUtils.getInstance().refreshToken(
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
                OauthUtils.getInstance().refreshToken(
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
                OauthUtils.getInstance().refreshToken(
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
                OauthUtils.getInstance().refreshToken(
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
                OauthUtils.getInstance().refreshToken(
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


/*****************************************************云盘**********************************************************/
    /**
     * 获取云盘列表
     */
    public void getVolumeList() {
        final String url = APIUri.getVolumeListUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeListSuccess(new GetVolumeListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeListFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeList();
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
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
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeFileListSuccess(new GetVolumeFileListResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileListFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeFileList(volumeId, currentDirAbsolutePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
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
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeFileUploadTokenSuccess(new GetVolumeFileUploadTokenResult(new String(arg0)), localFilePath, mockVolumeFile);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileUploadTokenFail(mockVolumeFile, error, responseCode, localFilePath);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeFileUploadToken(fileName, volumeFilePath, localFilePath, mockVolumeFile);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
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
        JSONArray array = new JSONArray();
        try {
            for (int i = 0; i < moveVolumeFileList.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("from", currentDirAbsolutePath + moveVolumeFileList.get(i).getName());
                object.put("to", toPath);
                array.put(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(array.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnMoveFileSuccess(moveVolumeFileList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnMoveFileFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        moveVolumeFile(volumeId, currentDirAbsolutePath, moveVolumeFileList, toPath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 复制云盘文件
     *
     * @param volumeId
     * @param currentDirAbsolutePath
     */
    public void copyVolumeFile(final String volumeId, final String currentDirAbsolutePath, final List<VolumeFile> copyVolumeFileList, final String toPath) {
        final String url = APIUri.getCopyVolumeFileUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        JSONArray array = new JSONArray();
        try {
            for (int i = 0; i < copyVolumeFileList.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("from", currentDirAbsolutePath + copyVolumeFileList.get(i).getName());
                object.put("to", toPath);
                array.put(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(array.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnCopyFileSuccess();
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnCopyFileFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        copyVolumeFile(volumeId, currentDirAbsolutePath, copyVolumeFileList, toPath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
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
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnCreateForderSuccess(new VolumeFile(new String(new String(arg0))));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnCreateForderFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createForder(volumeId, forderName, currentDirAbsolutePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
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
    public void volumeFileDelete(final String volumeId, final List<VolumeFile> deleteVolumeFileList, final String currentDirAbsolutePath) {
        final String url = APIUri.getVolumeFileOperationUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        JSONArray array = new JSONArray();
        try {
            for (int i = 0; i < deleteVolumeFileList.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("path", currentDirAbsolutePath + deleteVolumeFileList.get(i).getName());
                array.put(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(array.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeFileDeleteSuccess(deleteVolumeFileList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileDeleteFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        volumeFileDelete(volumeId, deleteVolumeFileList, currentDirAbsolutePath);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
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
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeFileRenameSuccess(volumeFile, fileNewName);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeFileRenameFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        volumeFileRename(volumeId, volumeFile, currentDirAbsolutePath, fileNewName);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 创建共享网盘
     *
     * @param memberArray
     * @param volumeName
     */
    public void createShareVolume(final String myUid, final String volumeName) {
        final String url = APIUri.getVolumeListUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        JSONObject object = new JSONObject();
        try {
            object.put("name", volumeName);
            JSONArray array = new JSONArray();
            array.put(myUid);
            object.put("members", array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.setBodyContent(object.toString());
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnCreateShareVolumeSuccess(new Volume(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnCreateShareVolumeFail("", -1);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        createShareVolume(myUid, volumeName);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });

    }

    /**
     * 修改网盘名称
     *
     * @param volume
     * @param name
     */
    public void updateShareVolumeName(final Volume volume, final String name) {
        final String url = APIUri.getUpdateVolumeInfoUrl(volume.getId());
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("name", name);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUpdateShareVolumeNameSuccess(volume, name);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUpdateShareVolumeNameFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateShareVolumeName(volume, name);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });

    }


    /**
     * 修改网盘名称
     *
     * @param volume
     * @param name
     */
    public void removeShareVolumeName(final Volume volume) {
        final String url = APIUri.getUpdateVolumeInfoUrl(volume.getId());
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("id", volume.getId());
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.retrunRemoveShareVolumeSuccess(volume);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnRemoveShareVolumeFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        removeShareVolumeName(volume);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });

    }

    /**
     * 获取共享网盘详细信息
     *
     * @param volumeId
     */
    public void getVolumeInfo(final String volumeId) {
        final String url = APIUri.getUpdateVolumeInfoUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("id", volumeId);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeDetailSuccess(new VolumeDetail(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeDetailFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeInfo(volumeId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 增加云盘成员
     *
     * @param volumeId
     * @param uidList
     */
    public void volumeMemAdd(final String volumeId, final List<String> uidList) {
        final String url = APIUri.getVolumeMemUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("members", uidList);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeMemAddSuccess(uidList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeMemAddFail(error, responseCode);
            }


            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        volumeMemAdd(volumeId, uidList);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 删除云盘成员
     *
     * @param volumeId
     * @param uidList
     */
    public void volumeMemDel(final String volumeId, final List<String> uidList) {
        final String url = APIUri.getVolumeMemUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("members", uidList);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeMemDelSuccess(uidList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeMemDelFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        volumeMemDel(volumeId, uidList);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 更改组名称
     *
     * @param groupId
     * @param groupName
     */
    public void updateGroupName(final String groupId, final String groupName) {
        final String url = APIUri.getGroupBaseUrl(groupId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addQueryStringParameter("name", groupName);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnUpdateGroupNameSuccess(groupName);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUpdateGroupNameFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateGroupName(groupId, groupName);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }


    /**
     * 增加组成员
     *
     * @param volumeId
     * @param uidList
     */
    public void groupMemAdd(final String groupId, final List<String> uidList) {
        final String url = APIUri.getGroupMemBaseUrl(groupId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("members", uidList);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGroupMemAddSuccess(uidList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGroupMemAddFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        groupMemAdd(groupId, uidList);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 删除组成员
     *
     * @param volumeId
     * @param uidList
     */
    public void groupMemDel(final String groupId, final List<String> uidList) {
        final String url = APIUri.getGroupMemBaseUrl(groupId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("members", uidList);
        params.setAsJsonContent(true);
        HttpUtils.request(context, CloudHttpMethod.DELETE, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGroupMemDelSuccess(uidList);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGroupMemDelFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        groupMemDel(groupId, uidList);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });
    }

    /**
     * 获取网盘下包含自己的组
     *
     * @param volumeId
     */
    public void getVolumeGroupContainMe(final String volumeId) {
        final String url = APIUri.getVolumeGroupUrl(volumeId);
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        params.addParameter("isMember", true);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeGroupContainMeSuccess(new GetVolumeGroupResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeGroupContainMeFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeGroupContainMe(volumeId);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }

        });

    }

    /**
     * 根据volumeId获取文件夹的权限组
     *
     * @param volumeId
     */
    public void getVolumeFileGroup(final String volumeId, final String path) {
        final String url = APIUri.getVolumeFileGroupUrl(volumeId) + "?path=" + path;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnVolumeGroupSuccess(new GetVolumeResultWithPermissionResult(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnVolumeGroupFail(error, responseCode);
            }


            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getVolumeFileGroup(volumeId, path);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        });

    }

    /**
     * 修改文件夹组权限
     *
     * @param volumeId
     * @param path
     * @param group
     * @param privilege
     * @param recurse
     */
    public void updateVolumeFileGroupPermission(final String volumeId, final String path, final String group, final int privilege, final boolean recurse) {
        final String url = APIUri.getVolumeFileGroupUrl(volumeId) + "?path=" + path + "&group=" + group + "&privilege=" + privilege + "&recurse=" + recurse;
        RequestParams params = ((MyApplication) context.getApplicationContext()).getHttpRequestParams(url);
        HttpUtils.request(context, CloudHttpMethod.PUT, params, new BaseModuleAPICallback(context, url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                GetVolumeGroupPermissionResult getVolumeGroupPermissionResult = new GetVolumeGroupPermissionResult("");
                getVolumeGroupPermissionResult.setPrivilege(privilege);
                apiInterface.returnUpdateVolumeGroupPermissionSuccess(getVolumeGroupPermissionResult);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnUpdateVolumeGroupPermissionFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        updateVolumeFileGroupPermission(volumeId, path, group, privilege, recurse);
                    }

                    @Override
                    public void executeFailCallback() {
                        callbackFail("", -1);
                    }
                };
                OauthUtils.getInstance().refreshToken(
                        oauthCallBack, requestTime);
            }
        });
    }
}
