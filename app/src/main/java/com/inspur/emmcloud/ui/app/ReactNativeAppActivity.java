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
import com.inspur.emmcloud.config.MyAppConfig;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.inspur.reactnative.ReactNativeFlow;

/**
 * Created by yufuchang on 2017/3/15.
 */

public class ReactNativeAppActivity extends Activity implements DefaultHardwareBackBtnHandler {
    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String reactAppFilePath = MyAppConfig.getReactAppFilePath(ReactNativeAppActivity.this,
                ((MyApplication)getApplication()).getUid());

        if (!ReactNativeFlow.checkBundleFileIsExist(reactAppFilePath + "/index.android.bundle")) {
            ReactNativeFlow.unZipFile(ReactNativeAppActivity.this, "WhoseCar.zip", reactAppFilePath, true);
        }

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

        // 注意这里的HelloWorld必须对应“index.android.js”中的
        // “AppRegistry.registerComponent()”的第一个参数
        mReactRootView.startReactApplication(mReactInstanceManager, "WhoseCar", null);

        setContentView(mReactRootView);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }
}