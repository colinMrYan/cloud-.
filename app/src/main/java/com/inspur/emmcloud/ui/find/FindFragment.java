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

import com.beefe.picker.PickerViewPackage;
import com.facebook.react.BuildConfig;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.inspur.reactnative.ReactNativeFlow;
import com.reactnativecomponent.swiperefreshlayout.RCTSwipeRefreshLayoutPackage;


/**
 * com.inspur.emmcloud.ui.FindFragment create at 2016年8月29日 下午3:27:26
 */
public class FindFragment extends Fragment implements DefaultHardwareBackBtnHandler {
    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceManager;
    private String reactCurrentFilePath;
    private RefreshReactNativeReceiver reactNativeReceiver;
    private String userId = "";
    public static boolean hasUpdated = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mReactRootView == null) {
            createReactNativeView(false);
        }
        if (hasUpdated) {
            createReactNativeView(true);
            hasUpdated = false;
        }
        return mReactRootView;
    }

    /**
     * 创建并启动ReactNativeView
     */
    private void createReactNativeView(boolean needToRefresh) {
        mReactRootView = new ReactRootView(getActivity());
        //暂时屏蔽以下8行
//        try {
//            InputStream fis = new FileInputStream(new File(reactCurrentFilePath+"/default.9.png"));
//            Drawable da = Drawable.createFromStream(fis, "default.9.png");
//            mReactRootView.setBackground(da);
//        } catch (FileNotFoundException e) {
//            LogUtils.YfcDebug("出现文件解析异常："+e.getMessage());
//            e.printStackTrace();
//        }
//        mReactRootView.setBackgroundResource(R.drawable.loading2);
        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getActivity().getApplication())
                .setCurrentActivity(getActivity())
                .setJSMainModuleName("index.android")
                .setJSBundleFile(reactCurrentFilePath + "/index.android.bundle")
                .addPackage(new MainReactPackage())
                .addPackage(new RCTSwipeRefreshLayoutPackage())
                .addPackage(new AuthorizationManagerPackage())
                .addPackage(new PickerViewPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        mReactRootView.startReactApplication(mReactInstanceManager, "discover", null);
        if (needToRefresh) {
            mReactRootView.invalidate();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = ((MyApplication) getActivity().getApplication()).getUid();
        reactNativeReceiver = new RefreshReactNativeReceiver();
//        reactCurrentFilePath = MyAppConfig.getReactCurrentFilePath(getActivity(), userId);
        reactCurrentFilePath = MyAppConfig.getReactAppFilePath(getActivity(),userId,"discover");
        if (!ReactNativeFlow.checkBundleFileIsExist(reactCurrentFilePath + "/index.android.bundle")) {
            ReactNativeFlow.unZipFile(getActivity(), "bundle-v0.1.0.android.zip", reactCurrentFilePath, true);
        }
        registerReactNativeReceiver();
    }


    /**
     * 注册刷新广播
     */
    private void registerReactNativeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.inspur.react.success");
        getActivity().registerReceiver(reactNativeReceiver, filter);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        getActivity().onBackPressed();
    }

    class RefreshReactNativeReceiver extends BroadcastReceiver {
        private static final String ACTION_REFRESH = "com.inspur.react.success";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_REFRESH)) {
                createReactNativeView(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reactNativeReceiver != null) {
            getActivity().unregisterReceiver(reactNativeReceiver);
            reactNativeReceiver = null;
        }
        hasUpdated = false;
    }


}
