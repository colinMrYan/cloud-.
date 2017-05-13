package com.inspur.emmcloud.ui.app;

import android.content.Context;
import android.os.Bundle;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.GetLoginResult;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

/**
 * Created by yufuchang on 2017/5/13.
 */

public class ShortCutFunctionActivity extends BaseActivity{

    private LoadingDialog loadingDialog;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(this);
        context = ShortCutFunctionActivity.this;
        if(isRefreshTokenExist()){
            forceRereshToken();
        }else{
            ToastUtils.show(this,"您的通行证已过期，请重新登录");
            finish();
        }
    }

    /**
     * 强制刷新token
     */
    private void forceRereshToken() {
        if(NetUtils.isNetworkConnected(this)){
            LoginAPIService apiService = new LoginAPIService(this);
            apiService.setAPIInterface(new WebService());
            if(loadingDialog != null && !loadingDialog.isShowing()){
                loadingDialog.show();
            }
            apiService.refreshToken();
        }
    }

    /**
     * 检查RT是否存在,
     * 如果已过期则当做不存在
     * @return
     */
    private boolean isRefreshTokenExist() {
        boolean flag = false;
        String rereshToken = PreferencesUtils.getString(ShortCutFunctionActivity.this,"refreshToken","");
        long betweenTime = System.currentTimeMillis() - PreferencesUtils.getLong(ShortCutFunctionActivity.this,"acccessTokenTime",0);
        if(betweenTime<259000000){
            flag = true;
        }
        return flag && StringUtils.isBlank(rereshToken);
    }

    private class WebService extends APIInterfaceInstance {
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
            PreferencesUtils.putLong(context,"acccessTokenTime",System.currentTimeMillis());
            ((MyApplication)context.getApplicationContext()).setIsTokenRefreshing(false);
//            ((MyApplication)context.getApplicationContext()).startWebSocket();
            ((MyApplication)context.getApplicationContext()).setAccessToken(accessToken);
        }

        @Override
        public void returnOauthSigninFail(String error) {
            // TODO Auto-generated method stub
            ToastUtils.show(ShortCutFunctionActivity.this,"您的通行证已过期，请重新登录");
            finish();
        }

    }
}
