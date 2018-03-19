package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.login.GetLoginResult;
import com.inspur.emmcloud.interf.OauthCallBack;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ToastUtils;

import java.util.List;


public class OauthUtils {
	private OauthCallBack callBack;
	private Context context;
	public OauthUtils(OauthCallBack callBack,Context context){
		this.callBack = callBack;
		this.context = context;
	}
	public void refreshToken(String url){
		((MyApplication)context.getApplicationContext()).addCallBack(callBack);
		if (!((MyApplication)context.getApplicationContext()).getIsTokenRefreshing()) {
			((MyApplication)context.getApplicationContext()).setIsTokenRefreshing(true);
			LoginAPIService apiService = new LoginAPIService(context);
			apiService.setAPIInterface(new WebService());
			apiService.refreshToken();
		}
		
	}
	
	private class WebService extends APIInterfaceInstance{
		@Override
		public void returnOauthSigninSuccess(GetLoginResult getLoginResult) {
			// TODO Auto-generated method stub
			String accessToken = getLoginResult.getAccessToken();
			String refreshToken = getLoginResult.getRefreshToken();
			int keepAlive = getLoginResult.getKeepAlive();
			String tokenType = getLoginResult.getTokenType();
			int expiresIn = getLoginResult.getExpiresIn();
			PreferencesUtils.putString(context, "accessToken", accessToken);
			PreferencesUtils.putString(context, "refreshToken", refreshToken);
			PreferencesUtils.putInt(context, "keepAlive", keepAlive);
			PreferencesUtils.putString(context, "tokenType", tokenType);
			PreferencesUtils.putInt(context, "expiresIn", expiresIn);
			((MyApplication)context.getApplicationContext()).setIsTokenRefreshing(false);
			((MyApplication)context.getApplicationContext()).startWebSocket();
			((MyApplication)context.getApplicationContext()).setAccessToken(accessToken);
			((MyApplication)context.getApplicationContext()).setRefreshToken(refreshToken);
			List<OauthCallBack> callBackList = ((MyApplication)context.getApplicationContext()).getCallBackList();
			for (int i = 0; i < callBackList.size(); i++) {
				callBackList.get(i).reExecute();
			}
			((MyApplication)context.getApplicationContext()).clearCallBackList();
		}

		@Override
		public void returnOauthSigninFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			((MyApplication)context.getApplicationContext()).setIsTokenRefreshing(false);
			//当errorCode为400时代表refreshToken也失效，需要重新登录
			if (errorCode == 400){
				((MyApplication)context.getApplicationContext()).clearCallBackList();
				if (((MyApplication)context.getApplicationContext()).getWebSocketPush() != null) {
					((MyApplication)context.getApplicationContext()).getWebSocketPush().closeSocket();
				}
				ToastUtils.show(context, context.getString(R.string.authorization_expired));
				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setClass(context, LoginActivity.class);
				context.startActivity(intent);
				if (context != null && context instanceof Activity) {
					((Activity) context).finish();
				}
			}else{
				List<OauthCallBack> callBackList = ((MyApplication)context.getApplicationContext()).getCallBackList();
				for (int i = 0; i < callBackList.size(); i++) {
					callBackList.get(i).executeFailCallback();
				}
				((MyApplication)context.getApplicationContext()).clearCallBackList();
			}

		}

	}
}
