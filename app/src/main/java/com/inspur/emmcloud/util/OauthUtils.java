package com.inspur.emmcloud.util;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.GetLoginResult;
import com.inspur.emmcloud.ui.login.LoginActivity;

public class OauthUtils {
	private OauthCallBack callBack;
	private Context context;
	private String oldToken ="";
	public OauthUtils(OauthCallBack callBack,Context context){
		this.callBack = callBack;
		this.context = context;
	}
	public void refreshTocken(String url){
		Log.d("jason", "refreshTocken----------");
		LoginAPIService apiService = new LoginAPIService(context);
		oldToken = PreferencesUtils.getString(context, "accessToken", "");
		((MyApplication)context.getApplicationContext()).addCallBack(callBack);
		if (!((MyApplication)context.getApplicationContext()).getIsTokenRefreshing()) {
			((MyApplication)context.getApplicationContext()).setIsTokenRefreshing(true);
			apiService.uploadAuthorizationInfo(1,url,null); 
			apiService.setAPIInterface(new WebService());
			apiService.refreshToken();
		}
		
	}
	
	private class WebService extends APIInterfaceInstance{
		@Override
		public void returnOauthSigninSuccess(GetLoginResult getLoginResult) {
			// TODO Auto-generated method stub
			Log.d("jason", "refresh---token---success");
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
			LoginAPIService apiService = new LoginAPIService(context);
			apiService.uploadAuthorizationInfo(2,null,oldToken); 
			List<OauthCallBack> callBackList = ((MyApplication)context.getApplicationContext()).getCallBackList();
			for (int i = 0; i < callBackList.size(); i++) {
				callBackList.get(i).execute();
			}
			((MyApplication)context.getApplicationContext()).clearCallBackList();
		}

		@Override
		public void returnOauthSigninFail(String error) {
			// TODO Auto-generated method stub
			Log.d("jason", "refresh---token---fail");
			((MyApplication)context.getApplicationContext()).setIsTokenRefreshing(false);
			((MyApplication)context.getApplicationContext()).clearCallBackList();
			if (((MyApplication)context.getApplicationContext()).getWebSocketPush() != null) {
				((MyApplication)context.getApplicationContext()).getWebSocketPush().connectWebSocket();
			}
			LoginAPIService apiService = new LoginAPIService(context);
			apiService.uploadAuthorizationInfo(3,null,null); 
			ToastUtils.show(context, context.getString(R.string.authorization_expired));
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClass(context, LoginActivity.class);
			context.startActivity(intent);
			if (context != null && context instanceof Activity) {
				((Activity) context).finish();
			}
		}

	}
}
