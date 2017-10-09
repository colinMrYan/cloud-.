package com.inspur.emmcloud.api;

import android.content.Context;

import com.inspur.emmcloud.bean.AppException;
import com.inspur.emmcloud.util.AppExceptionCacheUtils;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.StringUtils;

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

public class APIDownloadCallBack implements Callback.ProgressCallback<File>{

    private String url = "";
    private Context context;
    public APIDownloadCallBack(Context context,String url){
        this.context = context;
        this.url = url;
    }
    @Override
    public void onWaiting() {

    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onLoading(long l, long l1, boolean b) {

    }

    @Override
    public void onSuccess(File file) {

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
                error = "download error";
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
            saveNetException(error, responseCode,errorLevel);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    @Override
    public void onCancelled(CancelledException e) {
        saveNetException("download cancel",0,3);
    }

    @Override
    public void onFinished() {

    }

    /**
     * 处理异常网络请求
     * @param error
     * @param responseCode
     */
    private void saveNetException(String error, int responseCode,int errorLevel) {
        if (!AppUtils.isApkDebugable(context)) {
            AppException appException = new AppException(System.currentTimeMillis(), AppUtils.getVersion(context), errorLevel, url, error, responseCode);
            AppExceptionCacheUtils.saveAppException(context, appException);
        }
    }

    /**
     * 记录文件下载后验证异常
     * @param context
     * @param url
     * @param error
     * @param errorLevel
     */
    public static void saveFileCheckException(Context context,String url,String error,int errorLevel) {
        if (!AppUtils.isApkDebugable(context)) {
            AppException appException = new AppException(System.currentTimeMillis(), AppUtils.getVersion(context), errorLevel, url, error, 0);
            AppExceptionCacheUtils.saveAppException(context, appException);
        }
    }
}
