package com.inspur.emmcloud.ui.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.GetLoginResult;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ShortCutUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.imp.api.ImpActivity;

/**
 * Created by yufuchang on 2017/5/13.
 */

public class ShortCutFunctionActivity extends BaseActivity{

    private LoadingDialog loadingDialog;
    private Context context;
    private String uri = "";
    private String appShortCutName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dialog_custom_ios_layout);
        loadingDialog = new LoadingDialog(this);
        context = ShortCutFunctionActivity.this;
        LogUtils.YfcDebug("进入ShortCut");
        if(getIntent().hasExtra("uri")){
            uri = getIntent().getStringExtra("uri");
        }
        if(getIntent().hasExtra("appShortCutName")){
            appShortCutName = getIntent().getStringExtra("appShortCutName");
        }
        if(isRefreshTokenExist()){
            forceRereshToken();
        }else{
            LogUtils.YfcDebug("不存在token导致失败");
            ToastUtils.show(context, context.getString(R.string.authorization_expired));
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
//        boolean flag = false;
        String rereshToken = PreferencesUtils.getString(ShortCutFunctionActivity.this,"refreshToken","");
        LogUtils.YfcDebug("refreshTOken："+rereshToken);
//        long betweenTime = System.currentTimeMillis() - PreferencesUtils.getLong(ShortCutFunctionActivity.this,"acccessTokenTime",0);
//        if(betweenTime<259000000){
//            flag = true;
//        }
        return !StringUtils.isBlank(rereshToken);
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnOauthSigninSuccess(GetLoginResult getLoginResult) {
            // TODO Auto-generated method stub
            if(loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            saveTokenInfo(getLoginResult);
            if(StringUtils.isBlank(uri)){
                openApp();
            }else{
                ToastUtils.show(ShortCutFunctionActivity.this,"您要打开的应用不存在，请重新登录后重新添加快捷方式");
                ShortCutUtils.deleteShortcut(ShortCutFunctionActivity.this,appShortCutName);
                finish();
            }
        }

        @Override
        public void returnOauthSigninFail(String error) {
            // TODO Auto-generated method stub
            LogUtils.YfcDebug("请求失败导致授权过期");
            if(loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            ToastUtils.show(context, context.getString(R.string.authorization_expired));
            finish();
        }

    }

    /**
     * 存储token信息
     * @param getLoginResult
     */
    private void saveTokenInfo(GetLoginResult getLoginResult) {
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
        ((MyApplication)context.getApplicationContext()).setAccessToken(accessToken);
    }

    /**
     * 打开对应的app
     */
    private void openApp() {
        Intent sIntent = new Intent(Intent.ACTION_MAIN);
        sIntent.addCategory(Intent.CATEGORY_LAUNCHER);// 加入action,和category之后，程序卸载的时候才会主动将该快捷方式也卸载
        sIntent.setClass(ShortCutFunctionActivity.this, ImpActivity.class);//点击后进入的Activity
        sIntent.putExtra("uri",uri);
        startActivity(sIntent);
        finish();
    }
}
