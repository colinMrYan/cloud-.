package com.inspur.emmcloud.api.apiservice;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APICallback;
import com.inspur.emmcloud.api.APIInterface;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.bean.ReactNativeClientIdErrorBean;
import com.inspur.emmcloud.bean.ReactNativeUpdateBean;
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
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        params.addParameter("deviceId",deviceId);
        params.addParameter("deviceName",deviceName);
        x.http().post(params, new APICallback() {
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
                },context).refreshTocken(completeUrl);
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
    public void getDownLoadUrl(final Context context, final String findDownloadUrl, final String clientId, final String currentVersion){
        final String completeUrl = findDownloadUrl+"?currentVersion="+currentVersion+"&clientId="+clientId;
        LogUtils.YfcDebug("获取ReactNative应用下载地址："+completeUrl);
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        x.http().get(params, new APICallback() {
            @Override
            public void callbackSuccess(String arg0) {
                LogUtils.YfcDebug("获取React下载地址成功："+arg0);
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                LogUtils.YfcDebug("获取react下载地址返回失败："+error);
            }

            @Override
            public void callbackTokenExpire() {

                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        getDownLoadUrl(context,findDownloadUrl,clientId,currentVersion);
                    }
                },context).refreshTocken(completeUrl);
            }
        });
    }

    /**
     * 获取ReactNative更新版本
     * @param version
     * @param lastCreationDate
     */
    public void getReactNativeUpdate (final String version, final long lastCreationDate, final String clientId){
        final String completeUrl = APIUri.getReactNativeUpdate()+"version="+version+"&lastCreationDate=" + lastCreationDate
                +"&clientId="+clientId;
        RequestParams params = ((MyApplication) context.getApplicationContext())
                .getHttpRequestParams(completeUrl);
        x.http().get(params, new APICallback() {
            @Override
            public void callbackSuccess(String arg0) {
                apiInterface.returnReactNativeUpdateSuccess(new ReactNativeUpdateBean(arg0));
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                apiInterface.returnReactNativeUpdateFail(new ReactNativeClientIdErrorBean(error));
                LogUtils.YfcDebug("clientId失效："+error);
            }

            @Override
            public void callbackTokenExpire() {
                new OauthUtils(new OauthCallBack() {
                    @Override
                    public void execute() {
                        getReactNativeUpdate(version,lastCreationDate,clientId);
                    }
                },context).refreshTocken(completeUrl);
            }
        });
    }

    /**
     * 检查ReactNative模块是否需要更新
     * @param scheme
     */
    public void checkReactNativeModuleUpdate(String scheme){

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



}
