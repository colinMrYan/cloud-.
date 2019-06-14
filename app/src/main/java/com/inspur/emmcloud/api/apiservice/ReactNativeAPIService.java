package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.bean.appcenter.GetClientIdRsult;
import com.inspur.emmcloud.bean.appcenter.ReactNativeDownloadUrlBean;
import com.inspur.emmcloud.bean.appcenter.ReactNativeInstallUriBean;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.io.File;

/**
 * Created by yufuchang on 2017/3/28.
 */

public class ReactNativeAPIService {
    private Context context;
    private APIInterface apiInterface;

    public ReactNativeAPIService(Context context) {
        this.context = context;
    }

    public void setAPIInterface(APIInterface apiInterface) {
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
        final String completeUrl = APIUri.getClientId();
        RequestParams params = ((MyApplication) context.getApplicationContext())
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
     * 获取地址
     *
     * @param uri
     */
    public void getReactNativeInstallUrl(final String uri) {
        final String completeUrl = APIUri.getReactNativeInstallUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("uri", uri);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGetReactNativeInstallUrlSuccess(new ReactNativeInstallUriBean(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetReactNativeInstallUrlFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getReactNativeInstallUrl(uri);
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
        final String completeUrl = APIUri.getReactNativeWriteBackUrl(appId) + "?preVersion=" + preVersion + "&currentVersion=" + currentVersion + "&clientId=" + clientId +
                "&command=" + command;
        RequestParams params = ((MyApplication) context.getApplicationContext())
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


    /**
     * 获取ReactNative应用的下地址
     *
     * @param context
     * @param findDownloadUrl
     * @param clientId
     * @param currentVersion
     */
    public void getDownLoadUrl(final Context context, final String findDownloadUrl,
                               final String clientId, final String currentVersion) {
        final String completeUrl = findDownloadUrl + "?version=" + currentVersion + "&clientId=" + clientId;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.GET, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                apiInterface.returnGetDownloadReactNativeUrlSuccess(new ReactNativeDownloadUrlBean(new String(arg0)));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetDownloadReactNativeUrlFail(error, responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        getDownLoadUrl(context, findDownloadUrl, clientId, currentVersion);
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
     * 下载reactnative更新包
     *
     * @param fromUri
     * @param filePath
     * @param progressCallback
     */
    public void downloadReactNativeModuleZipPackage(String fromUri, String filePath, Callback.ProgressCallback<File> progressCallback) {
        DownLoaderUtils downLoaderUtils = new DownLoaderUtils();
        downLoaderUtils.startDownLoad(fromUri, filePath, progressCallback);
    }

    /**
     * 写回闪屏日志
     *
     * @param preVersion
     * @param currentVersion
     * @param clientId
     * @param command
     */
    public void writeBackSplashPageVersionChange(final String preVersion, final String currentVersion, final String clientId, final String command) {
        final String completeUrl = APIUri.getUploadSplashPageWriteBackLogUrl() + "?preVersion=" + preVersion + "&currentVersion=" + currentVersion + "&clientId=" + clientId +
                "&command=" + command;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                LogUtils.YfcDebug("闪屏写回成功，不需要后续处理");
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                LogUtils.YfcDebug("闪屏写回失败，不需要后续处理" + error + responseCode);
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                OauthCallBack oauthCallBack = new OauthCallBack() {
                    @Override
                    public void reExecute() {
                        writeBackSplashPageVersionChange(preVersion, currentVersion, clientId, command);
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
