package com.inspur.emmcloud.ui.find;


import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;


import com.facebook.react.ReactActivity;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactRootView;

import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.inspur.emmcloud.BuildConfig;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.LogUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2017/2/13.
 */

public class MyReactFindActivity extends ReactActivity {
    @Override
    protected String getMainComponentName() {
        return "MyProjectOne";
    }
//
//    @Override
//    protected boolean getUseDeveloperSupport() {
//        return false;
//    }
//
//    @Override
//    protected List<ReactPackage> getPackages() {
//        MainReactPackage mainReactPackage = new MainReactPackage();
//        ArrayList<ReactPackage> arrayList = new ArrayList<ReactPackage>();
//        arrayList.add(mainReactPackage);
//        return arrayList;
//    }



//    private ReactRootView mReactRootView;
//    private ReactInstanceManager mReactInstanceManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        LogUtils.YfcDebug("进入OnCreate");
////        mReactRootView = new ReactRootView(this);
//        setContentView(R.layout.activity_myreact);
//        mReactRootView = (ReactRootView) findViewById(R.id.test_js);
//        mReactInstanceManager = ReactInstanceManager.builder()
//                .setApplication(getApplication())
//                .setBundleAssetName("androidRN.jsbundle")
//                .setJSMainModuleName("index.android")
//                .addPackage(new MainReactPackage())
//                .setUseDeveloperSupport(BuildConfig.DEBUG)
//                .setInitialLifecycleState(LifecycleState.RESUMED)
//                .build();
//        LogUtils.YfcDebug("完成ReactRootView配置");
//        try{
//            mReactRootView.startReactApplication(mReactInstanceManager, "MyProjectOne", null);
////            setContentView(mReactRootView);
//        }catch (Exception e){
//           LogUtils.YfcDebug("捕获到的异常信息："+e.getMessage());
//        }
//
//    }
//
//    @Override
//    public void invokeDefaultOnBackPressed() {
//        super.onBackPressed();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
////        if (mReactInstanceManager != null) {
////            mReactInstanceManager.onPause();
////        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
////        if (mReactInstanceManager != null) {
////            mReactInstanceManager.onResume(this);
////        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (mReactInstanceManager != null) {
//            mReactInstanceManager.onBackPressed();
//        } else {
//            super.onBackPressed();
//        }
//        finish();
//    }
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_MENU && mReactInstanceManager != null) {
//            mReactInstanceManager.showDevOptionsDialog();
//            return true;
//        }
//        return super.onKeyUp(keyCode, event);
//    }

}

