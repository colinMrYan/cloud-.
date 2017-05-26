package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.bean.ReactNativeDownloadUrlBean;
import com.inspur.emmcloud.bean.ReactNativeInstallUriBean;
import com.inspur.emmcloud.util.DownLoaderUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.OauthCallBack;
import com.inspur.emmcloud.util.OauthUtils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

/**
 * Created by yufuchang on 2017/3/28.
 */

public class ReactNativeAPIService {
    private Context context;
    private APIInterface apiInterface;
    public ReactNativeAPIService(Context context){
        this.context = context;
    }
    public void setAPIInterface(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    /**
     * 获取ClientId
     * @param deviceId
     * @param deviceName
     */
    public void getClientId(final String deviceId, final String deviceName){
        final String completeUrl = APIUri.getClientId();
        LogUtils.YfcDebug("请求clietnid的地址："+completeUrl);
        LogUtils.YfcDebug("设备Id："+deviceId);
        LogUtils.YfcDebug("设备名称："+deviceName);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("deviceId",deviceId);
        params.addParameter("deviceName",deviceName);
        x.http().post(params, new APICallback(context,completeUrl) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnGetClientIdResultSuccess(new GetClientIdRsult(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetClientIdResultFail(error);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        getClientId(deviceId,deviceName);
                    }
                },context).refreshToken(completeUrl);
            }
        });
    }

    /**
     * 获取地址
     * @param uri
     */
    public void getReactNativeInstallUrl(final String uri){
        final String completeUrl = APIUri.getReactNativeInstallUrl();
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("uri",uri);
        x.http().post(params, new APICallback(context,completeUrl) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnGetReactNativeInstallUrlSuccess(new ReactNativeInstallUriBean(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetReactNativeInstallUrlFail(error);
            }

            @Override
            public void callbackTokenExpire() {

                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        getReactNativeInstallUrl(uri);
                    }
                },context).refreshToken(completeUrl);
            }
        });
    }

    /**
     * 写回版本变更情况
     * @param preVersion
     * @param currentVersion
     * @param clientId
     * @param command
     * @param appId
     */
    public void writeBackVersionChange(final String preVersion, final String currentVersion, final String clientId, final String command, final String appId){
        final String completeUrl = APIUri.getReactNativeWriteBackUrl(appId)+"?preVersion="+preVersion+"&currentVersion="+currentVersion+"&clientId="+clientId+
                "&command="+command;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        x.http().post(params, new APICallback(context,completeUrl) {
            @Override
            public void callbackSuccess(String arg0) {
                LogUtils.YfcDebug("写回成功，不需要后续处理");
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                LogUtils.YfcDebug("写回失败，不需要后续处理");
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        writeBackVersionChange(preVersion,currentVersion,clientId, command,appId);
                    }
                },context).refreshToken(completeUrl);
            }
        });
    }


    /**
     * 获取ReactNative应用的下地址
     * @param context
     * @param findDownloadUrl
     * @param clientId
     * @param currentVersion
     */
    public void getDownLoadUrl(final Context context, final String findDownloadUrl,
                               final String clientId, final String currentVersion){
        final String completeUrl = findDownloadUrl+"?version="+currentVersion+"&clientId="+clientId;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        x.http().get(params, new APICallback(context,completeUrl) {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnGetDownloadReactNativeUrlSuccess(new ReactNativeDownloadUrlBean(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnGetDownloadReactNativeUrlFail(error);
            }

            @Override
            public void callbackTokenExpire() {

                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        getDownLoadUrl(context,findDownloadUrl,clientId,currentVersion);
                    }
                },context).refreshToken(completeUrl);
            }
        });
    }

    /**
     * 下载reactnative更新包
     * @param fromUri
     * @param filePath
     * @param progressCallback
     */
    public void downloadReactNativeModuleZipPackage(String fromUri,String filePath,Callback.ProgressCallback<File> progressCallback){
        DownLoaderUtils downLoaderUtils = new DownLoaderUtils();
        downLoaderUtils.startDownLoad(fromUri,filePath,progressCallback);
    }

    /**
     * 写回闪屏日志
     * @param preVersion
     * @param currentVersion
     * @param clientId
     * @param command
     */
    public void writeBackSplashPageVersionChange(final String preVersion, final String currentVersion, final String clientId, final String command){
        final String completeUrl = APIUri.getUploadSplashPageWriteBackLogUrl()+"?preVersion="+preVersion+"&currentVersion="+currentVersion+"&clientId="+clientId+
                "&command="+command;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        x.http().post(params, new APICallback(context,completeUrl) {
            @Override
            public void callbackSuccess(String arg0) {
                LogUtils.YfcDebug("闪屏写回成功，不需要后续处理");
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                LogUtils.YfcDebug("闪屏写回失败，不需要后续处理"+error+responseCode);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        writeBackSplashPageVersionChange(preVersion,currentVersion,clientId, command);
                    }
                },context).refreshTocken(completeUrl);
            }
        });
    }


}
