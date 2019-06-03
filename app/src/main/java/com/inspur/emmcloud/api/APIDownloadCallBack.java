package com.inspur.emmcloud.api;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.util.privates.cache.AppExceptionCacheUtils;

import org.xutils.common.Callback;
import org.xutils.ex.HttpException;

import java.io.File;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;


/**
 * Created by yufuchang on 2017/10/9.
 * 下载过程中出现问题统一封装，记录下载失败状态
 */

public abstract class APIDownloadCallBack implements Callback.ProgressCallback<File> {

    private String url = "";
    private Context context;

    public APIDownloadCallBack(Context context, String url) {
        this.context = context;
        this.url = url;
    }

    public APIDownloadCallBack(String url) {
        this.url = url;
    }

    @Override
    public void onWaiting() {

    }

    @Override
    public void onStarted() {
        callbackStart();
    }

    @Override
    public void onLoading(long l, long l1, boolean b) {
        callbackLoading(l, l1, b);
    }

    @Override
    public void onSuccess(File file) {
        LogUtils.debug("HttpUtil", "result=");
        //Callback回调到回调处，出异常，则可能既调onSuccess又调OnError，加try为了将异常在此处捕获防止异常被吞，无法查找
        try {
            callbackSuccess(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onError(Throwable arg0, boolean arg1) {
        try {
            String error = "";
            int responseCode = -1;
            int errorLevel = 2;
            //connect timed out
            if (arg0 instanceof TimeoutException || arg0 instanceof SocketTimeoutException || arg0 instanceof UnknownHostException) {
                errorLevel = 3;
                error = "download time out";
            } else if (arg0 instanceof HttpException) {
                HttpException httpEx = (HttpException) arg0;
                error = httpEx.getResult();
                responseCode = httpEx.getCode();
            } else {
                error = arg0.toString();
            }
            if (StringUtils.isBlank(error)) {
                error = "未知错误";
            }
            LogUtils.debug("HttpUtil", "result=" + error);
            saveNetException(error, responseCode, errorLevel);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        callbackError(arg0, arg1);
    }

    @Override
    public void onCancelled(CancelledException e) {
        saveNetException("download cancel", 0, 3);
        callbackCanceled(e);
    }

    @Override
    public void onFinished() {

    }

    /**
     * 处理异常网络请求
     *
     * @param error
     * @param responseCode
     */
    private void saveNetException(String error, int responseCode, int errorLevel) {
        AppExceptionCacheUtils.saveAppException(MyApplication.getInstance(), errorLevel, url, error, responseCode);
    }

    public void callbackStart() {
    }

    public void callbackLoading(long total, long current, boolean isUploading) {
    }


    public abstract void callbackSuccess(File file);

    public abstract void callbackError(Throwable arg0, boolean arg1);

    public void callbackCanceled(CancelledException e) {
    }

}
