/**
 * APICallback.java
 * classes : com.inspur.emmcloud.api.APICallback
 * V 1.0.0
 * Create at 2016年11月7日 下午4:12:58
 */
package com.inspur.emmcloud.api;

import android.content.Context;

import com.inspur.emmcloud.bean.AppException;
import com.inspur.emmcloud.util.AppExceptionCacheUtils;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.StringUtils;

import org.xutils.common.Callback.CommonCallback;
import org.xutils.ex.HttpException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;


/**
 * com.inspur.emmcloud.api.APICallback
 * create at 2016年11月7日 下午4:12:58
 */
public abstract class APICallback implements CommonCallback<String> {

	private Context context;
	private String url;

	public APICallback(Context context, String url) {
		this.context = context;
		this.url = url;
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
			if (arg0 instanceof TimeoutException || arg0 instanceof SocketTimeoutException || arg0 instanceof UnknownHostException) {
				errorLevel = 3;
				error = "time out";
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
				callbackTokenExpire();
			} else {
				callbackFail(error, responseCode);
				saveNetException(error, responseCode,errorLevel);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

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
	public void onSuccess(String arg0) {
		// TODO Auto-generated method stub
		LogUtils.debug("HttpUtil", "url=" + url);
		LogUtils.debug("HttpUtil", "result=" + arg0);
		//Callback回调到回调处，出异常，则可能既调onSuccess又调OnError，加try为了将异常在此处捕获防止异常被吞，无法查找
		try {
			callbackSuccess(arg0);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public abstract void callbackSuccess(String arg0);

	public abstract void callbackFail(String error, int responseCode);

	public abstract void callbackTokenExpire();

}
