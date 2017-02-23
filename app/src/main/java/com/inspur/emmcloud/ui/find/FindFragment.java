package com.inspur.emmcloud.ui.find;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.BuildConfig;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.inspur.reactnative.ReactNativeFlow;
import com.reactnativecomponent.swiperefreshlayout.RCTSwipeRefreshLayoutPackage;


/**
 * com.inspur.emmcloud.ui.FindFragment create at 2016年8月29日 下午3:27:26
 */
public class FindFragment extends Fragment implements DefaultHardwareBackBtnHandler {
    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceManager;
    private AppAPIService appAPIService;
//    public static boolean reactNativeViewNeedToRefresh = false;
    private String filePath;
    private RefreshReactNativeReceiver reactNativeReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        appAPIService = new AppAPIService(getActivity());
//        appAPIService.setAPIInterface(new WebService());
//        appAPIService.getReactNativeUpdate(0,0L);
        if (mReactRootView == null) {
            createReactNativeView(false);
        }
        return mReactRootView;
    }

    /**
     * 创建并启动ReactNativeView
     */
    private void createReactNativeView(boolean needToRefresh) {
        LogUtils.YfcDebug("创建ReactView");
        mReactRootView = new ReactRootView(getActivity());
        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getActivity().getApplication())
                .setCurrentActivity(getActivity())
                .setJSMainModuleName("index.android")
//                    .setBundleAssetName("index.android.bundle")
                .setJSBundleFile(filePath + "/current/default/" + "index.android.bundle")
//                    .setJSBundleFile(Environment.getExternalStorageDirectory()+"/IMP-Cloud/reactnative")
                .addPackage(new MainReactPackage())
                .addPackage(new RCTSwipeRefreshLayoutPackage())
                .addPackage(new AuthorizationManagerPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        mReactRootView.startReactApplication(mReactInstanceManager, "discover", null);
        if(needToRefresh){
            LogUtils.YfcDebug("发现执行重绘操作");
            mReactRootView.invalidate();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reactNativeReceiver = new RefreshReactNativeReceiver();
        filePath = getActivity().getFilesDir().getPath();
//                ZipUtils.upZipFile(Environment.getExternalStorageDirectory()+"/default.zip",filePath+"/unzipdefault");
//                ZipUtils.upZipFile(Environment.getExternalStorageDirectory()+"/IMP-Cloud/cache/cloud/default.zip",filePath + "/");
        if (!ReactNativeFlow.checkBundleFileIsExist(filePath + "/current/default/index.android.bundle")) {
            LogUtils.YfcDebug("在FindFragment里解压bundle");
            ReactNativeFlow.unZipFile(getActivity(), "default.zip", filePath + "/current", true);
        }
        registerMsgReceiver();
    }

    /**
     * 注册刷新广播
     */
    private void registerMsgReceiver() {
        if(reactNativeReceiver == null){
            reactNativeReceiver = new RefreshReactNativeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.inspur.react.refresh");
            getActivity().registerReceiver(reactNativeReceiver, filter);
        }
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        getActivity().onBackPressed();
    }

    class RefreshReactNativeReceiver extends BroadcastReceiver{
        private static final String ACTION_REFRESH = "com.inspur.react.refresh";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_REFRESH)){
                LogUtils.YfcDebug("FindFragment里刷新View");
                createReactNativeView(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(reactNativeReceiver != null){
            getActivity().unregisterReceiver(reactNativeReceiver);
            reactNativeReceiver=null;
        }
    }
}
