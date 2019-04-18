package com.inspur.emmcloud.ui.appcenter;

import com.inspur.emmcloud.BaseActivity;

/**
 * Created by yufuchang on 2017/5/13.
 */

public class ShortCutFunctionActivity extends BaseActivity {

//    private LoadingDialog loadingDialog;
//    private Context context;
//    private String uri = "";
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_blank);
//        loadingDialog = new LoadingDialog(this);
//        context = ShortCutFunctionActivity.this;
//        LogUtils.YfcDebug("进入ShortCut");
//        if(getIntent().hasExtra("uri")){
//            uri = getIntent().getStringExtra("uri");
//        }
//        if(isRefreshTokenExist()){
////            forceRereshToken();
//            if(!StringUtils.isBlank(uri)){
//                openApp();
//            }else{
//                ToastUtils.show(context, context.getString(R.string.authorization_expired));
//                finish();
//            }
//        }else{
//            LogUtils.YfcDebug("不存在token导致失败");
//            ToastUtils.show(context, context.getString(R.string.authorization_expired));
//            finish();
//        }
//    }
//
//    /**
//     * 强制刷新token
//     */
//    private void forceRereshToken() {
//        if(NetUtils.isNetworkConnected(this)){
//            LoginAPIService apiService = new LoginAPIService(this);
//            apiService.setAPIInterface(new WebService());
//            if(loadingDialog != null && !loadingDialog.isShowing()){
//                loadingDialog.show();
//            }
//            apiService.refreshToken();
//        }
//    }
//
//    /**
//     * 检查RT是否存在,
//     * 如果已过期则当做不存在
//     * @return
//     */
//    private boolean isRefreshTokenExist() {
////        boolean flag = false;
//        String rereshToken = PreferencesUtils.getString(ShortCutFunctionActivity.this,"refreshToken","");
//        LogUtils.YfcDebug("refreshTOken："+rereshToken);
////        long betweenTime = System.currentTimeMillis() - PreferencesUtils.getLong(ShortCutFunctionActivity.this,"acccessTokenTime",0);
////        if(betweenTime<259000000){
////            flag = true;
////        }
//        return !StringUtils.isBlank(rereshToken);
//    }
//
//    private class WebService extends APIInterfaceInstance {
//        @Override
//        public void returnOauthSignInSuccess(GetLoginResult getLoginResult) {
//            // TODO Auto-generated method stub
//            if(loadingDialog != null && loadingDialog.isShowing()){
//                loadingDialog.dismiss();
//            }
//            saveTokenInfo(getLoginResult);
//            if(!StringUtils.isBlank(uri)){
//                openApp();
//            }else{
//                ToastUtils.show(ShortCutFunctionActivity.this,"您要打开的应用不存在，请删除现在快捷后重新添加");
//                finish();
//            }
//        }
//
//        @Override
//        public void returnOauthSignInFail(String error,int errorCode) {
//            // TODO Auto-generated method stub
//            LogUtils.YfcDebug("请求失败导致授权过期");
//            if(loadingDialog != null && loadingDialog.isShowing()){
//                loadingDialog.dismiss();
//            }
//            ToastUtils.show(context, context.getString(R.string.authorization_expired));
//            finish();
//        }
//
//    }
//
//    /**
//     * 存储token信息
//     * @param getLoginResult
//     */
//    private void saveTokenInfo(GetLoginResult getLoginResult) {
//        String accessToken = getLoginResult.getAccessToken();
//        String refreshToken = getLoginResult.getRefreshToken();
//        int keepAlive = getLoginResult.getKeepAlive();
//        String tokenType = getLoginResult.getTokenType();
//        int expiresIn = getLoginResult.getExpiresIn();
//        PreferencesUtils.putString(context, "accessToken", accessToken);
//        PreferencesUtils.putString(context, "refreshToken", refreshToken);
//        PreferencesUtils.putInt(context, "keepAlive", keepAlive);
//        PreferencesUtils.putString(context, "tokenType", tokenType);
//        PreferencesUtils.putInt(context, "expiresIn", expiresIn);
//        ((MyApplication)context.getApplicationContext()).setIsTokenRefreshing(false);
//        ((MyApplication)context.getApplicationContext()).setAccessToken(accessToken);
//        ((MyApplication)context.getApplicationContext()).setRefreshToken(refreshToken);
//    }
//
//    /**
//     * 打开对应的app
//     */
//    private void openApp() {
//        Intent sIntent = new Intent(Intent.ACTION_MAIN);
//        sIntent.addCategory(Intent.CATEGORY_LAUNCHER);// 加入action,和category之后，程序卸载的时候才会主动将该快捷方式也卸载
//        sIntent.setClass(ShortCutFunctionActivity.this, ImpActivity.class);//点击后进入的Activity
//        sIntent.putExtra("uri",uri);
//        startActivity(sIntent);
//        finish();
//    }
}
