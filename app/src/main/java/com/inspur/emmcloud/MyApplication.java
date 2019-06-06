package com.inspur.emmcloud;

import com.beefe.picker.PickerViewPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.horcrux.svg.SvgPackage;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.login.communication.CommunicationService;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.luojilab.component.componentlib.router.Router;
import com.luojilab.component.componentlib.router.ui.UIRouter;
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
        UIRouter.enableDebug();
        Router.registerComponent("com.inspur.emmcloud.applike.AppApplike");
        Router router = Router.getInstance();
        if (router.getService(CommunicationService.class.getSimpleName()) != null) {
            CommunicationService service = (CommunicationService) router.getService(CommunicationService.class.getSimpleName());
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
