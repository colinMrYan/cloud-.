package com.inspur.emmcloud.util;

import android.content.Context;
import android.os.Handler;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.ErrorBean;

/**
 * 网络请求结果中间件，处理异常
 * 
 * @author Administrator
 *
 */
public class WebServiceMiddleUtils {

	private static final String TAG = "WebServiceMiddleUtils";
	
	/**
	 * 最常用的弹出toast
	 * @param context
	 * @param response
	 */
	public static void hand(Context context,String response){
		hand(context, response, null, null);
	}
	
	/**
	 * 处理错误
	 * @param context
	 * @param error
	 * @param errorCode
	 */
	public static void hand(Context context, String error,
			 int errorCode) {
		hand(context, error, null, errorCode,0);
	}
	
	/**
	 * 处理登录
	 * @param context
	 * @param response
	 * @param handler
	 * @param error
	 */
	public static void hand(Context context, String response,
			Handler handler, Integer error) {
		hand(context, response, handler,-1, error);
	}
	
	/**
	 * 最终调用的构造方法
	 * @param context
	 * @param response
	 * @param handler
	 * @param errorCode
	 * @param error
	 */
	public static void hand(Context context, String response,
			Handler handler,int errorCode, Integer error) {
		if(errorCode == 400){
			ErrorBean errorBean = new ErrorBean(response);
			ToastUtils.show(context, ErrorCodeUtils.getAlertByCode(context, errorBean.getErrorCode()));
		}else {
			ToastUtils.show(context, R.string.net_request_failed);
		}
		if (handler != null && error != null) {
			handler.sendEmptyMessage(error);
		}
	}

}
