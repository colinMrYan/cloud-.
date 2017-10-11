package com.inspur.emmcloud.ui.app;

import android.content.Intent;
import android.os.Bundle;

import com.beefe.picker.PickerViewPackage;
import com.facebook.react.BuildConfig;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIDownloadCallBack;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ReactNativeAPIService;
import com.inspur.emmcloud.bean.AndroidBundleBean;
import com.inspur.emmcloud.bean.Enterprise;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.bean.ReactNativeDownloadUrlBean;
import com.inspur.emmcloud.bean.ReactNativeInstallUriBean;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StateBarColor;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.dialogs.ECMCustomIOSDialog;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.inspur.reactnative.ReactNativeFlow;
import com.inspur.reactnative.ReactNativeInitInfoUtils;
import com.reactnativecomponent.swiperefreshlayout.RCTSwipeRefreshLayoutPackage;

import java.io.File;

/**
 * Created by yufuchang on 2017/3/15.
 */

public class ReactNativeAppActivity extends BaseActivity implements DefaultHardwareBackBtnHandler {
    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceManager;
    private ReactNativeAPIService reactNativeAPIService;
    private String reactNativeApp = "";
    private String reactAppFilePath;
    private ECMCustomIOSDialog loadingDialog;
    private String appModule;
    private String installUri = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StateBarColor.changeStateBarColor(this,R.color.white);
        init();
        checkSource();
        initReactNativeApp();
    }

    /**
     * 初始化RN应用Activity
     */
    private void init() {
        String token = ((MyApplication)getApplicationContext())
                .getToken();
        checkToken(token);
        loadingDialog = new ECMCustomIOSDialog(this, R.style.CustomDialog);
        reactNativeAPIService = new ReactNativeAPIService(ReactNativeAppActivity.this);
        reactNativeAPIService.setAPIInterface(new WebService());
    }

    /**
     * 检查token，如果token不存在则跳转到登录页面
     * @param token
     */
    private void checkToken(String token) {
        if(StringUtils.isBlank(token)){
            ToastUtils.show(ReactNativeAppActivity.this, ReactNativeAppActivity.this.getString(R.string.authorization_expired));
            Intent intent = new Intent();
            intent.setClass(ReactNativeAppActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * 检查应用来源，目前有两种来源
     * 1，来自网页消息等，scheme形式传来
     * 2，从其他Activity跳转而来以extras传来
     */
    private void checkSource() {
        String scheme = getIntent().getDataString();
        if(scheme != null){
            //从网页，消息，快捷方式等唤起应用时从这里获取协议和应用编号
            scheme = getIntent().getDataString();//'ecc-app-react-native: //1000'
            reactNativeApp = scheme;
        }else if(getIntent().hasExtra("ecc-app-react-native")){
            //从其他Activity启动时从这启动
            reactNativeApp = getIntent().getStringExtra("ecc-app-react-native");
        }else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mReactInstanceManager.onActivityResult(ReactNativeAppActivity.this,requestCode,resultCode,data);
    }

    /**
     * 初始化RN App
     */
    private void initReactNativeApp() {
        loadingDialog.show();
        boolean needCheckUpdate = true;//检查更新标志，如果需要从网络获取应用则不用再执行更新检查
        appModule = ReactNativeFlow.getAppModuleFromScheme(reactNativeApp);
        reactAppFilePath = MyAppConfig.getReactAppFilePath(ReactNativeAppActivity.this,
                ((MyApplication)getApplication()).getUid(),appModule);
        if(!StringUtils.isBlank(appModule) && ReactNativeFlow.checkBundleFileIsExist(reactAppFilePath + "/index.android.bundle")){
            //已安装应用
            createReactRootView(reactAppFilePath);
        }else if(!StringUtils.isBlank(appModule) && ReactNativeFlow.checkAssetsFileExits(ReactNativeAppActivity.this,appModule+".zip")){
            //预置应用
            ReactNativeFlow.unZipFile(ReactNativeAppActivity.this, appModule+".zip", reactAppFilePath, true);
            createReactRootView(reactAppFilePath);
        }else if(!StringUtils.isBlank(reactNativeApp)){
            //从网络获取应用
            getReactNativeAppFromNet();
            needCheckUpdate = false;
        }else {
            //都不符合则退出
            finish();
        }
        if(needCheckUpdate){
            //如果应用从网络获取而来则认为已经是最新版本，否则在完成显示之后需要检查更新
            checkReactNativeUpdate();
        }
    }

    /**
     * 检测更新，前面步骤保证了到这里时一定有bundle.json
     */
    private void checkReactNativeUpdate() {
        if(NetUtils.isNetworkConnected(ReactNativeAppActivity.this)){
            if(!loadingDialog.isShowing()){
                loadingDialog.show();
            }
            StringBuilder describeVersionAndTime = ReactNativeFlow.getBundleDotJsonFromFile(reactAppFilePath);
            AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
            String clientId = PreferencesByUserAndTanentUtils.getString(ReactNativeAppActivity.this,"react_native_clientid", "");
            reactNativeAPIService.getDownLoadUrl(ReactNativeAppActivity.this,androidBundleBean.getUpdate(),clientId,androidBundleBean.getVersion());
        }
    }

    /**
     * 从网络获取应用
     */
    private void getReactNativeAppFromNet() {
        if(NetUtils.isNetworkConnected(ReactNativeAppActivity.this)){
            if(!loadingDialog.isShowing()){
                loadingDialog.show();
            }
            reactNativeAPIService.getReactNativeInstallUrl(reactNativeApp);
        }
    }

    /**
     * 创建ReactRootView视图，并展示
     * @param reactAppFilePath
     */
    private void createReactRootView(String reactAppFilePath) {
        if(!ReactNativeFlow.checkBundleFileIsExist(reactAppFilePath + "/index.android.bundle")){
            ToastUtils.show(ReactNativeAppActivity.this,getString(R.string.react_native_app_open_failed));
            finish();
        }
        mReactRootView = new ReactRootView(this);
        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setCurrentActivity(ReactNativeAppActivity.this)
                .addPackage(new MainReactPackage())
                .addPackage(new RCTSwipeRefreshLayoutPackage())
                .addPackage(new PickerViewPackage())
                .addPackage(new AuthorizationManagerPackage())
                .setJSMainModuleName("index.android")
                .setJSBundleFile(reactAppFilePath + "/index.android.bundle")
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        StringBuilder describeVersionAndTime = ReactNativeFlow.getBundleDotJsonFromFile(reactAppFilePath);
        AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
        Bundle bundle = createInitBundle();
        mReactRootView.startReactApplication(mReactInstanceManager, androidBundleBean.getMainComponent(), bundle);
        setContentView(mReactRootView);
        if(loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }

    /**
     * 创建初始化参数
     * @return
     */
    private Bundle createInitBundle() {
        Bundle bundle = new Bundle();
        String myInfo = PreferencesUtils.getString(ReactNativeAppActivity.this,
                "myInfo", "");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        bundle.putString("id",getMyInfoResult.getID());
        bundle.putString("code",getMyInfoResult.getCode());
        bundle.putString("name",getMyInfoResult.getName());
        bundle.putString("mail",getMyInfoResult.getMail());
        bundle.putString("avatar",getMyInfoResult.getAvatar());
        Enterprise currentEnterprise = ((MyApplication)getApplicationContext()).getCurrentEnterprise();
        bundle.putString("enterpriseCode",currentEnterprise.getCode());
        bundle.putString("enterpriseName",currentEnterprise.getName());
        bundle.putString("enterpriseId",currentEnterprise.getId());

        //这里与IOS传值有所不同，建议是保留原来版本即上面的传值方式，下面是IOS传值方式
        //bundle.putString("profile",myInfo);
        bundle.putString("systemName", ReactNativeInitInfoUtils.SYSTEM);
        bundle.putString("systemVersion",ReactNativeInitInfoUtils.getSystemVersion(ReactNativeAppActivity.this));
        bundle.putString("locale",ReactNativeInitInfoUtils.getLocalLanguage(ReactNativeAppActivity.this));
        bundle.putString("reactNativeVersion",ReactNativeInitInfoUtils.getReactNativeVersion(reactAppFilePath));
        bundle.putSerializable("userProfile",getMyInfoResult.getUserProfile2ReactNativeWritableNativeMap());
        bundle.putString("accessToken",ReactNativeInitInfoUtils.getAppToken(ReactNativeAppActivity.this));
        bundle.putString("pushId",ReactNativeInitInfoUtils.getPushId(ReactNativeAppActivity.this));
        bundle.putString("pushType",ReactNativeInitInfoUtils.getPushType());
        bundle.putSerializable("currentEnterprise", ReactNativeInitInfoUtils.getCurrentEnterprise(ReactNativeAppActivity.this).enterPrise2ReactNativeWritableNativeMap());
        bundle.putString("appVersion",AppUtils.getVersion(ReactNativeAppActivity.this));
        return bundle;
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }



    /**
     * 根据命令更改
     * @param reactNativeDownloadUrlBean
     */
    private void changeReactNativeAppByOrder(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean) {
        int state = ReactNativeFlow.checkReactNativeOperation(reactNativeDownloadUrlBean.getCommand());
        String userId = ((MyApplication)getApplication()).getUid();
        String reactNatviveTempPath = MyAppConfig.getReactTempFilePath(ReactNativeAppActivity.this,userId);
        String preVersion = "",currentVersion = "";
        if (state == ReactNativeFlow.REACT_NATIVE_RESET) {
            //应用暂无RESET操作
        } else if (state == ReactNativeFlow.REACT_NATIVE_ROLLBACK) {
            File file = new File(reactNatviveTempPath);
            if(file.exists()){
                preVersion = getAppBundleBean().getVersion();
                ReactNativeFlow.moveFolder(reactNatviveTempPath+"/"+appModule, reactAppFilePath);
                currentVersion = getAppBundleBean().getVersion();
                createReactRootView(reactAppFilePath);
                writeBackVersion(preVersion,currentVersion,"ROLLBACK");
                ReactNativeFlow.deleteOldVersionFile(reactNatviveTempPath+"/"+appModule);
            }
        } else if (state == ReactNativeFlow.REACT_NATIVE_FORWORD) {
            downloadReactNativeZip(reactNativeDownloadUrlBean);
        } else if (state == ReactNativeFlow.REACT_NATIVE_UNKNOWN) {
        } else if (state == ReactNativeFlow.REACT_NATIVE_NO_UPDATE) {
            if(!ReactNativeFlow.checkBundleFileIsExist(reactAppFilePath + "/index.android.bundle")){
                ToastUtils.show(ReactNativeAppActivity.this,getString(R.string.react_native_app_open_failed));
                finish();
            }
        }

    }

    /**
     * 获取下载地址
     * @param reactNativeInstallUriBean
     */
    private void getDownlaodUrl(ReactNativeInstallUriBean reactNativeInstallUriBean) {
        if(NetUtils.isNetworkConnected(ReactNativeAppActivity.this)){
            String clientId = PreferencesByUserAndTanentUtils.getString(ReactNativeAppActivity.this,"react_native_clientid", "");
            if(!StringUtils.isBlank(clientId)){
                StringBuilder describeVersionAndTime = ReactNativeFlow.getBundleDotJsonFromFile(reactAppFilePath);
                AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
                reactNativeAPIService.getDownLoadUrl(ReactNativeAppActivity.this,reactNativeInstallUriBean.getInstallUri(),clientId,androidBundleBean.getVersion());
            }else {
                reactNativeAPIService.getClientId(AppUtils.getMyUUID(ReactNativeAppActivity.this), AppUtils.GetChangShang());
            }
        }
    }

    /**
     * 下载reactNative的zip包
     * @param reactNativeDownloadUrlBean
     */
    private void downloadReactNativeZip(final ReactNativeDownloadUrlBean reactNativeDownloadUrlBean) {
        final String userId = ((MyApplication)getApplication()).getUid();
        final String reactZipDownloadFromUri = APIUri.getZipUrl() + reactNativeDownloadUrlBean.getUri();
        final String reactZipFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH  + userId + "/" + reactNativeDownloadUrlBean.getUri() ;
        APIDownloadCallBack progressCallback = new APIDownloadCallBack(ReactNativeAppActivity.this,reactZipDownloadFromUri){
            @Override
            public void callbackStart() {

            }

            @Override
            public void callbackLoading(long total, long current, boolean isUploading) {

            }

            @Override
            public void callbackSuccess(File file) {
                if(loadingDialog != null && loadingDialog.isShowing()){
                    loadingDialog.dismiss();
                }
                String preVersion = getAppBundleBean().getVersion();
                String reactAppTempPath = MyAppConfig.getReactTempFilePath(ReactNativeAppActivity.this,userId);
                ReactNativeFlow.moveFolder(reactAppFilePath, reactAppTempPath+"/"+appModule);
                ReactNativeFlow.deleteOldVersionFile(reactAppFilePath);
                ReactNativeFlow.unZipFile(reactZipFilePath,reactAppFilePath);
                ReactNativeFlow.deleteReactNativeDownloadZipFile(reactZipFilePath);
                createReactRootView(reactAppFilePath);
                String currentVersion = getAppBundleBean().getVersion();
                writeBackVersion(preVersion,currentVersion,"FORWARD");
            }

            @Override
            public void callbackError(Throwable arg0, boolean arg1) {
                if(loadingDialog != null && loadingDialog.isShowing()){
                    loadingDialog.dismiss();
                }
                ToastUtils.show(ReactNativeAppActivity.this,getString(R.string.react_native_app_update_failed));
            }

            @Override
            public void callbackCanceled(CancelledException e) {
                if(loadingDialog != null && loadingDialog.isShowing()){
                    loadingDialog.dismiss();
                }
            }
        };
        reactNativeAPIService.downloadReactNativeModuleZipPackage(reactZipDownloadFromUri,reactZipFilePath,progressCallback);
    }

    /**
     * 向服务端写回目前版本
     */
    private void writeBackVersion(String preVersion,String currentVersion,String command) {
        String clientId = PreferencesByUserAndTanentUtils.getString(ReactNativeAppActivity.this,"react_native_clientid", "");
        reactNativeAPIService.writeBackVersionChange(preVersion,currentVersion,clientId,command,appModule);
    }

    /**
     * 获取目前显示着的AppVersion
     * @return
     */
    private AndroidBundleBean getAppBundleBean() {
        StringBuilder describeVersionAndTime = ReactNativeFlow.getBundleDotJsonFromFile(reactAppFilePath);
        AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
        return androidBundleBean;
    }


    /**
     * 获取到ClientId
     */
    private void installReactNativeApp() {
        if(NetUtils.isNetworkConnected(ReactNativeAppActivity.this)){
            StringBuilder describeVersionAndTime = ReactNativeFlow.getBundleDotJsonFromFile(reactAppFilePath);
            AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
            String clientId = PreferencesByUserAndTanentUtils.getString(ReactNativeAppActivity.this,"react_native_clientid", "");
            reactNativeAPIService.getDownLoadUrl(ReactNativeAppActivity.this,installUri,clientId,androidBundleBean.getVersion());
        }
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult) {
            if(loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            super.returnGetClientIdResultSuccess(getClientIdRsult);
            PreferencesByUserAndTanentUtils.putString(ReactNativeAppActivity.this,  "react_native_clientid", getClientIdRsult.getClientId());
            installReactNativeApp();
        }

        @Override
        public void returnGetClientIdResultFail(String error,int errorCode) {
            if(loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(ReactNativeAppActivity.this,
                    error,errorCode);
        }

        @Override
        public void returnGetReactNativeInstallUrlSuccess(ReactNativeInstallUriBean reactNativeInstallUriBean) {
            super.returnGetReactNativeInstallUrlSuccess(reactNativeInstallUriBean);
            if(loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            installUri = reactNativeInstallUriBean.getInstallUri();
            getDownlaodUrl(reactNativeInstallUriBean);
        }

        @Override
        public void returnGetReactNativeInstallUrlFail(String error,int errorCode) {
            if(loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(ReactNativeAppActivity.this,
                    error,errorCode);
        }

        @Override
        public void returnGetDownloadReactNativeUrlSuccess(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean) {
            super.returnGetDownloadReactNativeUrlSuccess(reactNativeDownloadUrlBean);
            if(loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            changeReactNativeAppByOrder(reactNativeDownloadUrlBean);
        }

        @Override
        public void returnGetDownloadReactNativeUrlFail(String error,int errorCode) {
            if(loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(ReactNativeAppActivity.this,
                    error,errorCode);
        }

    }
}