package com.inspur.emmcloud.application.api;

import android.content.Context;

import com.inspur.emmcloud.application.bean.GetClientIdRsult;
import com.inspur.emmcloud.application.bean.ReactNativeDownloadUrlBean;
import com.inspur.emmcloud.application.bean.ReactNativeInstallUriBean;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.componentservice.login.LoginService;
import com.inspur.emmcloud.componentservice.login.OauthCallBack;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.io.File;

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
     * 获取地址
     *
     * @param uri
     */
    public void getReactNativeInstallUrl(final String uri) {
        final String completeUrl = ApplicationAPIUri.getReactNativeInstallUrl();
        RequestParams params = ((BaseApplication) context.getApplicationContext())
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
        RequestParams params = ((BaseApplication) context.getApplicationContext())
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

//    /**
//     * 写回闪屏日志
//     *
//     * @param preVersion
//     * @param currentVersion
//     * @param clientId
//     * @param command
//     */
//    public void writeBackSplashPageVersionChange(final String preVersion, final String currentVersion, final String clientId, final String command) {
//        final String completeUrl = APIUri.getUploadSplashPageWriteBackLogUrl() + "?preVersion=" + preVersion + "&currentVersion=" + currentVersion + "&clientId=" + clientId +
//                "&command=" + command;
//        RequestParams params = ((MyApplication) context.getApplicationContext())
//                .getHttpRequestParams(completeUrl);
//        HttpUtils.request(context, CloudHttpMethod.POST, params, new BaseModuleAPICallback(context, completeUrl) {
//            @Override
//            public void callbackSuccess(byte[] arg0) {
//                LogUtils.YfcDebug("闪屏写回成功，不需要后续处理");
//            }
//
//            @Override
//            public void callbackFail(String error, int responseCode) {
//                LogUtils.YfcDebug("闪屏写回失败，不需要后续处理" + error + responseCode);
//            }
//
//            @Override
//            public void callbackTokenExpire(long requestTime) {
//                OauthCallBack oauthCallBack = new OauthCallBack() {
//                    @Override
//                    public void reExecute() {
//                        writeBackSplashPageVersionChange(preVersion, currentVersion, clientId, command);
//                    }
//
//                    @Override
//                    public void executeFailCallback() {
//                        callbackFail("", -1);
//                    }
//                };
//                refreshToken(
//                        oauthCallBack, requestTime);
//            }
//
//        });
//    }


}
