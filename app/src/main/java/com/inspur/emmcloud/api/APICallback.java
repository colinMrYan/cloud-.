/**
 * 
 * APICallback.java
 * classes : com.inspur.emmcloud.api.APICallback
 * V 1.0.0
 * Create at 2016年11月7日 下午4:12:58
 */
package com.inspur.emmcloud.api;

import java.net.HttpCookie;
import java.util.List;

import org.xutils.common.Callback.CommonCallback;
import org.xutils.ex.HttpException;
import org.xutils.http.cookie.DbCookieStore;

import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.StringUtils;


/**
 * com.inspur.emmcloud.api.APICallback
 * create at 2016年11月7日 下午4:12:58
 */
public abstract class APICallback implements CommonCallback<String>{

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
		String error = "";
		int responseCode = -1;
		if (arg0 instanceof HttpException) {
			HttpException httpEx = (HttpException) arg0;
			error = httpEx.getResult();
			responseCode = httpEx.getCode();
		}
		
		if (StringUtils.isBlank(error)) {
			LogUtils.debug("HttpUtil","result=未知错误");
		}else {
			LogUtils.debug("HttpUtil","result="+arg0);
		}
		 try {
			 if (responseCode ==  401) {
					callbackTokenExpire();
				}else {
					callbackFail(error, responseCode);
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
