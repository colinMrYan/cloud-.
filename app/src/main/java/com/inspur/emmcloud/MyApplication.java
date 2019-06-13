package com.inspur.emmcloud;

import com.beefe.picker.PickerViewPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.horcrux.svg.SvgPackage;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.oblador.vectoricons.VectorIconsPackage;

import java.util.Arrays;
import java.util.List;


/**
 * Application class
 */
public class MyApplication extends BaseApplication implements ReactApplication {
    /**
     * ReactNative相关代码
     */
    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return com.facebook.react.BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.asList(
                    new MainReactPackage(),
                    new AuthorizationManagerPackage(),
                    new PickerViewPackage(),
                    new SvgPackage(),
                    new VectorIconsPackage()
            );
        }
    };

    public void onCreate() {
        super.onCreate();
        Router.registerComponent("com.inspur.emmcloud.applike.AppApplike");
        Router.registerComponent("com.inspur.emmcloud.login.applike.LoginAppLike");
        Router router = Router.getInstance();
        if (router.getService(CommunicationService.class) != null) {
            CommunicationService service = router.getService(CommunicationService.class);
            service.startWebSocket();
        }
        SoLoader.init(this, false);//ReactNative相关初始化
    }

    /**
     * ReactNative相关代码
     *
     * @return
     */
    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }
}
