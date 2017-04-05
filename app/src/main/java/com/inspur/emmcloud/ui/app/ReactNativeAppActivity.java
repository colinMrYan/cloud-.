package com.inspur.emmcloud.ui.app;

import android.app.Activity;
import android.os.Bundle;

import com.beefe.picker.PickerViewPackage;
import com.facebook.react.BuildConfig;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ReactNativeAPIService;
import com.inspur.emmcloud.bean.AndroidBundleBean;
import com.inspur.emmcloud.bean.GetClientIdRsult;
import com.inspur.emmcloud.bean.ReactNativeDownloadUrlBean;
import com.inspur.emmcloud.bean.ReactNativeInstallUriBean;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.FileUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesByUserUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.inspur.reactnative.ReactNativeFlow;

import org.xutils.common.Callback;

import java.io.File;

/**
 * Created by yufuchang on 2017/3/15.
 */

public class ReactNativeAppActivity extends Activity implements DefaultHardwareBackBtnHandler {
    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceManager;
    private ReactNativeAPIService reactNativeAPIService;
    private String reactNativeApp = "";
    private String reactAppFilePath;
    private LoadingDialog loadingDialog;
    private String appModule;
    private String installUri = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(ReactNativeAppActivity.this);
        reactNativeAPIService = new ReactNativeAPIService(ReactNativeAppActivity.this);
        reactNativeAPIService.setAPIInterface(new WebService());
        String scheme = getIntent().getDataString();
        if(scheme != null){
            scheme = getIntent().getDataString();//'ecc-app-react-native: //1000'
            reactNativeApp = scheme;
        }else if(getIntent().hasExtra("ecc-app-react-native")){
            reactNativeApp = getIntent().getStringExtra("ecc-app-react-native");
        }else {
            LogUtils.YfcDebug("未知来源");
        }
        initReactNativeApp();
    }

    /**
     * 初始化RN App
     */
    private void initReactNativeApp() {
        boolean needCheckUpdate = true;
        appModule = reactNativeApp.split("//")[1];
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
        }else if(!StringUtils.isBlank(appModule) && NetUtils.isNetworkConnected(ReactNativeAppActivity.this)){
            LogUtils.YfcDebug("从网络获取ReactApp");
            getReactNativeAppFromNet();
            needCheckUpdate = false;
        }else {
            LogUtils.YfcDebug("不符合所有条件");
        }
        if(needCheckUpdate){
            checkReactNativeUpdate();
        }
    }

    /**
     * 检测更新，前面步骤保证了到这里时一定有bundle.json
     */
    private void checkReactNativeUpdate() {
        if(NetUtils.isNetworkConnected(ReactNativeAppActivity.this)){
            loadingDialog.show();
            StringBuilder describeVersionAndTime = FileUtils.readFile(reactAppFilePath +"/bundle.json", "UTF-8");
            AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
            String clientId = PreferencesByUserUtils.getString(ReactNativeAppActivity.this,"react_native_clientid", "");
            reactNativeAPIService.getDownLoadUrl(ReactNativeAppActivity.this,androidBundleBean.getUpdate(),clientId,androidBundleBean.getVersion());
        }
    }

    /**
     * 从网络获取应用
     */
    private void getReactNativeAppFromNet() {
        if(NetUtils.isNetworkConnected(ReactNativeAppActivity.this)){
            loadingDialog.show();
            reactNativeAPIService.getReactNativeInstallUrl(reactNativeApp);
        }
    }

    /**
     * 创建ReactRootView视图，并展示
     * @param reactAppFilePath
     * @param appModule
     */
    private void createReactRootView(String reactAppFilePath,String appModule) {
        mReactRootView = new ReactRootView(this);
        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setJSMainModuleName("index.android")
                .setJSBundleFile(reactAppFilePath + "/index.android.bundle")
                .setCurrentActivity(ReactNativeAppActivity.this)
                .addPackage(new MainReactPackage())
                .addPackage(new AuthorizationManagerPackage())
                .addPackage(new PickerViewPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        StringBuilder describeVersionAndTime = FileUtils.readFile(reactAppFilePath +"/bundle.json", "UTF-8");
        AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
        // 注意这里的HelloWorld必须对应“index.android.js”中的
        // “AppRegistry.registerComponent()”的第一个参数
        mReactRootView.startReactApplication(mReactInstanceManager, androidBundleBean.getMainComponent(), null);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }

    class WebService extends APIInterfaceInstance{
        @Override
        public void returnGetClientIdResultSuccess(GetClientIdRsult getClientIdRsult) {
            super.returnGetClientIdResultSuccess(getClientIdRsult);
            PreferencesByUserUtils.putString(ReactNativeAppActivity.this,  "react_native_clientid", getClientIdRsult.getClientId());
            installReactNativeApp();
        }

        @Override
        public void returnGetClientIdResultFail(String error) {
            if(loadingDialog != null && loadingDialog.isShowing()){
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(ReactNativeAppActivity.this,
                    error);
            super.returnGetClientIdResultFail(error);
        }

        @Override
        public void returnGetReactNativeInstallUrlSuccess(ReactNativeInstallUriBean reactNativeInstallUriBean) {
            super.returnGetReactNativeInstallUrlSuccess(reactNativeInstallUriBean);
            installUri = reactNativeInstallUriBean.getInstallUri();
            getDownlaodUrl(reactNativeInstallUriBean);
        }

        @Override
        public void returnGetReactNativeInstallUrlFail(String error) {
            super.returnGetReactNativeInstallUrlFail(error);
        }

        @Override
        public void returnGetDownloadReactNativeUrlSuccess(ReactNativeDownloadUrlBean reactNativeDownloadUrlBean) {
            super.returnGetDownloadReactNativeUrlSuccess(reactNativeDownloadUrlBean);
            changeReactNativeAppByOrder(reactNativeDownloadUrlBean);
        }

        @Override
        public void returnGetDownloadReactNativeUrlFail(String error) {
            super.returnGetDownloadReactNativeUrlFail(error);
        }

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
        if (state == ReactNativeFlow.REACT_NATIVE_ROLLBACK) {
            File file = new File(reactNatviveTempPath);
            if(file.exists()){
                LogUtils.YfcDebug("收到回滚操作");
                preVersion = getAppBundleBean().getVersion();
                ReactNativeFlow.moveFolder(reactNatviveTempPath+"/"+appModule, reactAppFilePath);
                currentVersion = getAppBundleBean().getVersion();
                createReactRootView(reactAppFilePath,appModule);
                setContentView(mReactRootView);
                writeBackVersion(preVersion,currentVersion,"ROLLBACK");
                FileUtils.deleteFile(reactNatviveTempPath+"/"+appModule);
            }else {
                LogUtils.YfcDebug("收到回滚操作但是没有缓存文件当做是StandBy指令，不做任何操作");
//                ReactNativeFlow.initReactNative(ReactNativeAppActivity.this,userId);
            }
        } else if (state == ReactNativeFlow.REACT_NATIVE_FORWORD) {
            LogUtils.YfcDebug("收到前进的指令");
            downloadReactNativeZip(reactNativeDownloadUrlBean);
        } else if (state == ReactNativeFlow.REACT_NATIVE_UNKNOWN) {
        } else if (state == ReactNativeFlow.REACT_NATIVE_NO_UPDATE) {
            LogUtils.YfcDebug("收到StandBy指令");
        }

    }

    /**
     * 获取下载地址
     * @param reactNativeInstallUriBean
     */
    private void getDownlaodUrl(ReactNativeInstallUriBean reactNativeInstallUriBean) {
        if(NetUtils.isNetworkConnected(ReactNativeAppActivity.this)){
            String clientId = PreferencesByUserUtils.getString(ReactNativeAppActivity.this,"react_native_clientid", "");
            if(!StringUtils.isBlank(clientId)){
                StringBuilder describeVersionAndTime = FileUtils.readFile(reactAppFilePath +"/bundle.json", "UTF-8");
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
        String reactZipDownloadFromUri = APIUri.getZipUrl() + reactNativeDownloadUrlBean.getUri();
        final String reactZipFilePath = MyAppConfig.LOCAL_DOWNLOAD_PATH  + userId + "/" + reactNativeDownloadUrlBean.getUri() ;
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

            }

            @Override
            public void onError(Throwable throwable, boolean b) {

            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {
                if(loadingDialog != null && loadingDialog.isShowing()){
                    loadingDialog.dismiss();
                }
                String preVersion = getAppBundleBean().getVersion();
                String reactAppTempPath = MyAppConfig.getReactTempFilePath(ReactNativeAppActivity.this,userId);
                ReactNativeFlow.moveFolder(reactAppFilePath, reactAppTempPath+"/"+appModule);
                ReactNativeFlow.deleteZipFile(reactAppFilePath);
                ReactNativeFlow.unZipFile(reactZipFilePath,reactAppFilePath);
                FileUtils.deleteFile(reactZipFilePath);
                String currentVersion = getAppBundleBean().getVersion();
                createReactRootView(reactAppFilePath,appModule);
                setContentView(mReactRootView);
                writeBackVersion(preVersion,currentVersion,"FORWARD");
            }
        };
        reactNativeAPIService.downloadReactNativeModuleZipPackage(reactZipDownloadFromUri,reactZipFilePath,progressCallback);
    }

    /**
     * 向服务端写回目前版本
     */
    private void writeBackVersion(String preVersion,String currentVersion,String command) {
        String clientId = PreferencesByUserUtils.getString(ReactNativeAppActivity.this,"react_native_clientid", "");
        reactNativeAPIService.writeBackVersionChange(preVersion,currentVersion,clientId,command,appModule);
    }

    /**
     * 获取目前显示着的AppVersion
     * @return
     */
    private AndroidBundleBean getAppBundleBean() {
        StringBuilder describeVersionAndTime = FileUtils.readFile(reactAppFilePath +"/bundle.json", "UTF-8");
        AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
        return androidBundleBean;
    }


    /**
     * 获取到ClientId
     */
    private void installReactNativeApp() {
        if(NetUtils.isNetworkConnected(ReactNativeAppActivity.this)){
            StringBuilder describeVersionAndTime = FileUtils.readFile(reactAppFilePath +"/bundle.json", "UTF-8");
            AndroidBundleBean androidBundleBean = new AndroidBundleBean(describeVersionAndTime.toString());
            String clientId = PreferencesByUserUtils.getString(ReactNativeAppActivity.this,"react_native_clientid", androidBundleBean.getVersion());
            reactNativeAPIService.getDownLoadUrl(ReactNativeAppActivity.this,installUri,clientId,androidBundleBean.getVersion());
        }
    }
}