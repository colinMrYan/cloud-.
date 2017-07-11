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
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ReactNativeAPIService;
import com.inspur.emmcloud.bean.AndroidBundleBean;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.bean.ReactNativeDownloadUrlBean;
import com.inspur.emmcloud.bean.ReactNativeInstallUriBean;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
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
import com.reactnativecomponent.swiperefreshlayout.RCTSwipeRefreshLayoutPackage;

import org.xutils.common.Callback;

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
    private String userId;
    private AuthorizationManagerPackage authorizationManagerPackage;
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
        userId = ((MyApplication)getApplication()).getUid();
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
            LogUtils.YfcDebug("未知来源,为保证应用稳定性未知来源时直接退出");
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
        if(NetUtils.isNetworkConnected(ReactNativeAppActivity.this)){
            loadingDialog.show();
        }
        boolean needCheckUpdate = true;
        appModule = ReactNativeFlow.getAppModuleFromScheme(reactNativeApp);
        reactAppFilePath = MyAppConfig.getReactAppFilePath(ReactNativeAppActivity.this,
                ((MyApplication)getApplication()).getUid(),appModule);
        if(!StringUtils.isBlank(appModule) && ReactNativeFlow.checkBundleFileIsExist(reactAppFilePath + "/index.android.bundle")){
            LogUtils.YfcDebug("符合已安装应用");
            createReactRootView(reactAppFilePath,appModule);
            setContentView(mReactRootView);
        }else if(!StringUtils.isBlank(appModule) && ReactNativeFlow.checkAssetsFileExits(ReactNativeAppActivity.this,appModule+".zip")){
            LogUtils.YfcDebug("从预置添加App");
            ReactNativeFlow.unZipFile(ReactNativeAppActivity.this, appModule+".zip", reactAppFilePath, true);
            createReactRootView(reactAppFilePath,appModule);
            setContentView(mReactRootView);
        }else if(!StringUtils.isBlank(reactNativeApp)){
            LogUtils.YfcDebug("从网络获取ReactApp");
            getReactNativeAppFromNet();
            needCheckUpdate = false;
        }else {
            LogUtils.YfcDebug("不符合所有条件，直接退出Activity");
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
            loadingDialog.show();
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
     * @param appModule
     */
    private void createReactRootView(String reactAppFilePath,String appModule) {
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
//        StringBuilder describeVersionAndTime = FileUtils.readFile(reactAppFilePath +"/bundle.json", "UTF-8");
        StringBuilder describeVersionAndTime = ReactNativeFlow.getBundleDotJsonFromFile(reactAppFilePath);
        AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
        Bundle bundle = createInitBundle();
        mReactRootView.startReactApplication(mReactInstanceManager, androidBundleBean.getMainComponent(), bundle);
    }

    /**
     * 创建初始化参数
     * @return
     */
    private Bundle createInitBundle() {
        Bundle bundle = new Bundle();
        String myInfo = PreferencesUtils.getString(ReactNativeAppActivity.this,
                "myInfo", "");
        LogUtils.YfcDebug("用户信息："+myInfo);
        LogUtils.YfcDebug("换成传profile");
//        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
//        bundle.putString("id",getMyInfoResult.getID());
//        bundle.putString("code",getMyInfoResult.getCode());
//        bundle.putString("name",getMyInfoResult.getName());
//        bundle.putString("mail",getMyInfoResult.getMail());
//        bundle.putString("avatar",getMyInfoResult.getAvatar());
//        Enterprise currentEnterprise = ((MyApplication)getApplicationContext()).getCurrentEnterprise();
//        bundle.putString("enterpriseCode",currentEnterprise.getCode());
//        bundle.putString("enterpriseName",currentEnterprise.getName());
//        bundle.putString("enterpriseId",currentEnterprise.getId());
        bundle.putString("profile",myInfo);


        return bundle;
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
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
            LogUtils.YfcDebug("返回正确的安装地址："+reactNativeInstallUriBean.getInstallUri());
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
            LogUtils.YfcDebug("获取download地址命令"+reactNativeDownloadUrlBean.getCommand());
            LogUtils.YfcDebug("获取downlaod地址："+reactNativeDownloadUrlBean.getUri());
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

    /**
     * 根据命令更改
     * @param reactNativeDownloadUrlBean
     */
    private void changeReactNativeAppByOrder(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean) {
        int state = ReactNativeFlow.checkReactNativeOperation(reactNativeDownloadUrlBean.getCommand());
//        String userId = ((MyApplication)getApplication()).getUid();
        String reactNatviveTempPath = MyAppConfig.getReactTempFilePath(ReactNativeAppActivity.this,userId);
        String preVersion = "",currentVersion = "";
        if (state == ReactNativeFlow.REACT_NATIVE_RESET) {
            //应用暂无RESET操作
        } else if (state == ReactNativeFlow.REACT_NATIVE_ROLLBACK) {
            File file = new File(reactNatviveTempPath);
            if(file.exists()){
                LogUtils.YfcDebug("收到回滚指令");
                preVersion = getAppBundleBean().getVersion();
                ReactNativeFlow.moveFolder(reactNatviveTempPath+"/"+appModule, reactAppFilePath);
                currentVersion = getAppBundleBean().getVersion();
                createReactRootView(reactAppFilePath,appModule);
                setContentView(mReactRootView);
                writeBackVersion(preVersion,currentVersion,"ROLLBACK");
                ReactNativeFlow.deleteOldVersionFile(reactNatviveTempPath+"/"+appModule);
//                FileUtils.deleteFile(reactNatviveTempPath+"/"+appModule);
            }else {
                LogUtils.YfcDebug("收到回滚操作但是没有缓存文件当做是StandBy指令，不做任何操作");
            }
        } else if (state == ReactNativeFlow.REACT_NATIVE_FORWORD) {
            LogUtils.YfcDebug("收到更新的指令");
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

//                StringBuilder describeVersionAndTime = FileUtils.readFile(reactAppFilePath +"/bundle.json", "UTF-8");
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
//        final String userId = ((MyApplication)getApplication()).getUid();
        final String reactZipDownloadFromUri = APIUri.getZipUrl() + reactNativeDownloadUrlBean.getUri();
        final String reactZipFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH  + userId + "/" + reactNativeDownloadUrlBean.getUri() ;
        LogUtils.YfcDebug("reactZipFilePath:"+reactZipFilePath);
        Callback.ProgressCallback<File> progressCallback = new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                LogUtils.YfcDebug("下载开始");
            }

            @Override
            public void onLoading(long l, long l1, boolean b) {
            }

            @Override
            public void onSuccess(File file) {
                if(loadingDialog != null && loadingDialog.isShowing()){
                    loadingDialog.dismiss();
                }
                String preVersion = getAppBundleBean().getVersion();
                String reactAppTempPath = MyAppConfig.getReactTempFilePath(ReactNativeAppActivity.this,userId);
                ReactNativeFlow.moveFolder(reactAppFilePath, reactAppTempPath+"/"+appModule);
                ReactNativeFlow.deleteOldVersionFile(reactAppFilePath);
                ReactNativeFlow.unZipFile(reactZipFilePath,reactAppFilePath);
                ReactNativeFlow.deleteReactNativeDownloadZipFile(reactZipFilePath);
//                FileUtils.deleteFile(reactZipFilePath);
                createReactRootView(reactAppFilePath,appModule);
                setContentView(mReactRootView);
                String currentVersion = getAppBundleBean().getVersion();
                writeBackVersion(preVersion,currentVersion,"FORWARD");
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                if(loadingDialog != null && loadingDialog.isShowing()){
                    loadingDialog.dismiss();
                }
                ToastUtils.show(ReactNativeAppActivity.this,getString(R.string.react_native_app_update_failed));
            }

            @Override
            public void onCancelled(CancelledException e) {
                if(loadingDialog != null && loadingDialog.isShowing()){
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFinished() {

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
//        StringBuilder describeVersionAndTime = FileUtils.readFile(reactAppFilePath +"/bundle.json", "UTF-8");
        StringBuilder describeVersionAndTime = ReactNativeFlow.getBundleDotJsonFromFile(reactAppFilePath);
        AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
        return androidBundleBean;
    }


    /**
     * 获取到ClientId
     */
    private void installReactNativeApp() {
        if(NetUtils.isNetworkConnected(ReactNativeAppActivity.this)){
//            StringBuilder describeVersionAndTime = FileUtils.readFile(reactAppFilePath +"/bundle.json", "UTF-8");
            StringBuilder describeVersionAndTime = ReactNativeFlow.getBundleDotJsonFromFile(reactAppFilePath);
            AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
            String clientId = PreferencesByUserAndTanentUtils.getString(ReactNativeAppActivity.this,"react_native_clientid", "");
            reactNativeAPIService.getDownLoadUrl(ReactNativeAppActivity.this,installUri,clientId,androidBundleBean.getVersion());
        }
    }
}