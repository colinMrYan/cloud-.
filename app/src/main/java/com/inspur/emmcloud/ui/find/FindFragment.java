package com.inspur.emmcloud.ui.find;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//import com.beefe.picker.PickerViewPackage;
//import com.facebook.react.BuildConfig;
//import com.facebook.react.ReactInstanceManager;
//import com.facebook.react.ReactRootView;
//import com.facebook.react.common.LifecycleState;
//import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
//import com.facebook.react.shell.MainReactPackage;
//import com.horcrux.svg.SvgPackage;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ZipUtils;
import com.inspur.emmcloud.basemodule.bean.Enterprise;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.ui.BaseFragment;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.bean.appcenter.AndroidBundleBean;
import com.inspur.reactnative.AuthorizationManagerPackage;
//import com.oblador.vectoricons.VectorIconsPackage;
//import com.reactnativecomponent.swiperefreshlayout.RCTSwipeRefreshLayoutPackage;
// todo AndroidX
//import com.reactnativenavigation.bridge.NavigationReactPackage;


/**
 * com.inspur.emmcloud.ui.FindFragment create at 2016年8月29日 下午3:27:26
 */
public class FindFragment extends BaseFragment {

//public class FindFragment extends BaseFragment implements DefaultHardwareBackBtnHandler {
    public static boolean hasUpdated = false;
//    private ReactRootView mReactRootView;
//    private ReactInstanceManager mReactInstanceManager;
    private String reactCurrentFilePath;
    private String userId = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        if (mReactRootView == null) {
//            createReactNativeView(false);
//        }
        if (hasUpdated) {
            createReactNativeView(true);
            hasUpdated = false;
        }
//        return mReactRootView;
        return null;
    }

    /**
     * 创建并启动ReactNativeView
     */
    private void createReactNativeView(boolean needToRefresh) {
//        mReactRootView = new ReactRootView(getActivity());
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
//        mReactInstanceManager = ReactInstanceManager.builder()
//                .setApplication(getActivity().getApplication())
//                .setCurrentActivity(getActivity())
//                .setJSMainModuleName("index.android")
//                .setJSBundleFile(reactCurrentFilePath + "/index.android.bundle")
//                .addPackage(new MainReactPackage())
//                .addPackage(new RCTSwipeRefreshLayoutPackage())
//                .addPackage(new AuthorizationManagerPackage())
//                .addPackage(new PickerViewPackage())
//                .addPackage(new SvgPackage())
//                .addPackage(new NavigationReactPackage())
//                .addPackage(new VectorIconsPackage())
//                .setUseDeveloperSupport(BuildConfig.DEBUG)
//                .setInitialLifecycleState(LifecycleState.RESUMED)
//                .build();
//        mReactRootView.startReactApplication(mReactInstanceManager, "discover", createInitBundle());
        if (needToRefresh) {
//            mReactRootView.invalidate();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = ((MyApplication) getActivity().getApplicationContext()).getUid();
//        reactCurrentFilePath = MyAppConfig.getReactCurrentFilePath(getActivity(), userId);
        reactCurrentFilePath = MyAppConfig.getReactAppFilePath(getActivity(), userId, "discover");
        if (!FileUtils.isFileExist(reactCurrentFilePath + "/index.android.bundle")) {
            ZipUtils.unZip(getActivity(), "bundle-inspur_esg-v0.3.1-beta7-.android.zip", reactCurrentFilePath, true);
        }
    }


    /**
     * 创建初始化参数，与ReactNativeActivity
     *
     * @return
     */
    private Bundle createInitBundle() {
        Bundle bundle = new Bundle();
        String myInfo = PreferencesUtils.getString(getActivity(),
                "myInfo", "");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        bundle.putString("id", getMyInfoResult.getID());
        bundle.putString("code", getMyInfoResult.getCode());
        bundle.putString("name", getMyInfoResult.getName());
        bundle.putString("mail", getMyInfoResult.getMail());
        bundle.putString("avatar", getMyInfoResult.getAvatar());
        Enterprise currentEnterprise = ((MyApplication) getActivity().getApplicationContext()).getCurrentEnterprise();
        bundle.putString("enterpriseCode", currentEnterprise.getCode());
        bundle.putString("enterpriseName", currentEnterprise.getName());
        bundle.putString("enterpriseId", currentEnterprise.getId());

        //这里与IOS传值有所不同，建议是保留原来版本即上面的传值方式，下面是IOS传值方式
        //bundle.putString("profile",myInfo);
        bundle.putString("systemName", "Android");
        bundle.putString("systemVersion", AppUtils.getSystemVersion());
        bundle.putString("locale", LanguageManager.getInstance().getCurrentAppLanguage());

        AndroidBundleBean androidBundleBean = new AndroidBundleBean(FileUtils.readFile(reactCurrentFilePath + "/bundle.json",
                "UTF-8").toString());
        bundle.putString("reactNativeVersion", androidBundleBean.getVersion());
        bundle.putString("accessToken", ((MyApplication) getActivity().getApplicationContext()).getToken());
        bundle.putString("pushId", PushManagerUtils.getInstance().getPushId(getActivity()));
        bundle.putString("pushType", getPushType());
        bundle.putSerializable("userProfile", myInfo);
        bundle.putSerializable("currentEnterprise", ((MyApplication) getActivity().getApplicationContext()).getCurrentEnterprise().toJSONObject().toString());
        bundle.putString("appVersion", AppUtils.getVersion(getActivity()));
        return bundle;
    }

    private String getPushType() {
        if (AppUtils.getIsHuaWei()) {
            return Constant.HUAWEI_FLAG;
        } else if (AppUtils.getIsXiaoMi()) {
            return Constant.XIAOMI_FLAG;
        }
        return "jiguang";
    }


//    @Override
//    public void invokeDefaultOnBackPressed() {
//        getActivity().onBackPressed();
//    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        hasUpdated = false;
    }


}
