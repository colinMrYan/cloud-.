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
    private String filePath;
    private RefreshReactNativeReceiver reactNativeReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                .setJSBundleFile(filePath + "/current/index.android.bundle")
                .addPackage(new MainReactPackage())
                .addPackage(new RCTSwipeRefreshLayoutPackage())
                .addPackage(new AuthorizationManagerPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        LogUtils.YfcDebug("Fragment指向的bundle路径："+filePath+"/current/index.android.bundle");
        mReactRootView.startReactApplication(mReactInstanceManager, "discover", null);
        if(needToRefresh){
            mReactRootView.invalidate();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reactNativeReceiver = new RefreshReactNativeReceiver();
        filePath = getActivity().getFilesDir().getPath();
        if (!ReactNativeFlow.checkBundleFileIsExist(filePath + "/current/index.android.bundle")) {
            LogUtils.YfcDebug("在FindFragment里解压bundle");
            ReactNativeFlow.unZipFile(getActivity(), "bundle-v0.1.0.android.zip", filePath + "/current", true);
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
            filter.addAction("com.inspur.react.success");
            getActivity().registerReceiver(reactNativeReceiver, filter);
        }
        LogUtils.YfcDebug("FindFragment创建");
        reactNativeReceiver = new RefreshReactNativeReceiver();
        filePath = getActivity().getFilesDir().getPath();
        if (!ReactNativeFlow.checkBundleFileIsExist(filePath + "/current/index.android.bundle")) {
            LogUtils.YfcDebug("在FindFragment里解压bundle");
            ReactNativeFlow.unZipFile(getActivity(), "bundle-v0.1.0.android.zip", filePath + "/current", true);
        }
        registerReactNativeReceiver();
    }

    /**
     * 注册刷新广播
     */
    private void registerReactNativeReceiver() {
        if(reactNativeReceiver == null){
            reactNativeReceiver = new RefreshReactNativeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.inspur.react.success");
            getActivity().registerReceiver(reactNativeReceiver, filter);
        }
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        getActivity().onBackPressed();
    }

    class RefreshReactNativeReceiver extends BroadcastReceiver{
        private static final String ACTION_REFRESH = "com.inspur.react.success";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_REFRESH)){
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
