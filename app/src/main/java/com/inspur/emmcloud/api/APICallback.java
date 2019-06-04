/**
 * APICallback.java
 * classes : com.inspur.emmcloud.api.APICallback
 * V 1.0.0
 * Create at 2016年11月7日 下午4:12:58
 */
package com.inspur.emmcloud.api;

import android.content.Context;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.util.AppExceptionCacheUtils;

import org.xutils.common.Callback.CommonCallback;
import org.xutils.ex.HttpException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;


/**
 * com.inspur.emmcloud.api.APICallback
 * create at 2016年11月7日 下午4:12:58
 */
public abstract class APICallback implements CommonCallback<byte[]> {

    private Context context;
    private String url;
    private long requestTime;

    public APICallback(Context context, String url) {
        this.context = context;
        this.url = url;
        requestTime = System.currentTimeMillis();
    }

    /* (non-Javadoc)
     * @see org.xutils.common.Callback.CommonCallback#onCancelled(org.xutils.common.Callback.CancelledException)
     */
    @Override
    public void onCancelled(CancelledException arg0) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.xutils.common.Callback.CommonCallback#onError(java.lang.Throwable, boolean)
     */
    @Override
    public void onError(Throwable arg0, boolean arg1) {
        // TODO Auto-generated method stub
        try {
            String error = "";
            int responseCode = -1;
            int errorLevel = 2;
            //connect timed out
            if (arg0 instanceof TimeoutException || arg0 instanceof SocketTimeoutException) {
                errorLevel = 3;
                error = "time out";
                responseCode = 1001;
            } else if (arg0 instanceof UnknownHostException) {
                errorLevel = 3;
                error = "time out";
                responseCode = 1003;
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
            LogUtils.debug("HttpUtil", "url=" + url);
            LogUtils.debug("HttpUtil", "result=" + error);

            if (responseCode == 401) {
                callbackTokenExpire(requestTime);
            } else {
                callbackFail(error, responseCode);
                AppExceptionCacheUtils.saveAppException(MyApplication.getInstance(), errorLevel, url, error, responseCode);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    /* (non-Javadoc)
     * @see org.xutils.common.Callback.CommonCallback#onFinished()
     */
    @Override
    public void onFinished() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.xutils.common.Callback.CommonCallback#onSuccess(java.lang.Object)
     */
    @Override
    public void onSuccess(byte[] arg0) {
        // TODO Auto-generated method stub
        LogUtils.debug("HttpUtil", "url=" + url);
        if (arg0 == null) {
            arg0 = "".getBytes();
        }
        LogUtils.debug("HttpUtil", "result=" + new String(arg0));
        //Callback回调到回调处，出异常，则可能既调onSuccess又调OnError，加try为了将异常在此处捕获防止异常被吞，无法查找
        try {
            callbackSuccess(arg0);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public abstract void callbackSuccess(byte[] arg0);

    public abstract void callbackFail(String error, int responseCode);

    public abstract void callbackTokenExpire(long requestTime);

}
