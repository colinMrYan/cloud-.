/**
 * 
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
import java.util.List;
import java.util.concurrent.TimeoutException;


/**
 * com.inspur.emmcloud.api.APICallback
 * create at 2016年11月7日 下午4:12:58
 */
public abstract class APICallback implements CommonCallback<String>{

	private Context context;
	private String url;

	public APICallback(Context context,String url){
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
			 //connect timed out
			 if (arg0 instanceof HttpException) {
				 HttpException httpEx = (HttpException) arg0;
				 error = httpEx.getResult();
				 responseCode = httpEx.getCode();
			 }
			 if (StringUtils.isBlank(error)) {
				 LogUtils.debug("HttpUtil","result=未知错误");
			 }else {
				 LogUtils.debug("HttpUtil","result="+arg0.toString());
			 }
			 if (responseCode ==  401) {
					callbackTokenExpire();
				}else {
					callbackFail(error, responseCode);
				 saveNetException(arg0,responseCode);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		
	}

	/**
	 * 处理异常网络请求
	 * @param arg0
	 */
	private void saveNetException(Throwable arg0,int responseCode){
		if (!AppUtils.isApkDebugable(context)){
			String error = "";
			int errorLevel = 2;
			if (arg0 instanceof TimeoutException || arg0 instanceof SocketTimeoutException){
				errorLevel = 3;
				error = "time out";
			}else if (arg0 instanceof HttpException) {
				HttpException httpEx = (HttpException) arg0;
				error = httpEx.getResult();
			}else {
				error = arg0.toString();
			}
			if (StringUtils.isBlank(error)) {
				error = "未知错误";
			}
			AppException appException = new AppException(System.currentTimeMillis(), AppUtils.getVersion(context),errorLevel,url,error,responseCode);
			AppExceptionCacheUtils.saveAppException(context,appException);
			List<AppException> appExceptionList = AppExceptionCacheUtils.getAppExceptionList(context);
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
	    LogUtils.debug("HttpUtil","result="+arg0);
	    try {
	    	callbackSuccess(arg0);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public abstract void callbackSuccess(String arg0);
	public abstract void callbackFail(String error,int responseCode);
	public abstract void callbackTokenExpire();

}
