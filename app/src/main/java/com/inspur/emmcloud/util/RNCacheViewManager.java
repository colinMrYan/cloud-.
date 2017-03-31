package com.inspur.emmcloud.util;

import android.app.Activity;
import android.view.ViewParent;

import com.facebook.react.BuildConfig;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.shell.MainReactPackage;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.reactnativecomponent.swiperefreshlayout.RCTSwipeRefreshLayoutPackage;

public class RNCacheViewManager {
    private static ReactRootView mRootView = null;
    private static ReactInstanceManager mManager = null;

    /**
     * 获取ReactRootView
     * @return
     */
    public static ReactRootView getmRootView() {
        return mRootView;
    }

    /**
     * 获取host
     * @param activity
     * @return
     */
    protected static ReactNativeHost getReactNativeHost(Activity activity) {
        return ((ReactApplication) activity.getApplication()).getReactNativeHost();
    }

    /**
     * 初始化ReactNative
     * @param activity
     */
    public static void init(Activity activity){
        if (mManager==null){
            String userId = ((MyApplication) activity.getApplication()).getUid();
            String reactCurrentFilePath = MyAppConfig.getReactAppFilePath(activity, userId,"discover");
            mManager = ReactInstanceManager.builder()
                    .setApplication(activity.getApplication())
                    .setCurrentActivity(activity)
                    .setJSMainModuleName("index.android")
                    .setJSBundleFile(reactCurrentFilePath + "/index.android.bundle")
                    .addPackage(new MainReactPackage())
                    .addPackage(new RCTSwipeRefreshLayoutPackage())
                    .addPackage(new AuthorizationManagerPackage())
                    .setUseDeveloperSupport(BuildConfig.DEBUG)
                    .setInitialLifecycleState(LifecycleState.RESUMED)
                    .build();
        }
        mRootView=new ReactRootView(activity);
        mRootView.startReactApplication(mManager, "discover", null);
    }

    /**
     * 销毁
     */
    public static void onDestroy() {
        try {
            ViewParent parent = getmRootView().getParent();
            if (parent != null)
                ((android.view.ViewGroup) parent).removeView(getmRootView());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}