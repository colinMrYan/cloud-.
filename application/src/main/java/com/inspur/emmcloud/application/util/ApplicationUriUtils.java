package com.inspur.emmcloud.application.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.application.R;
import com.inspur.emmcloud.application.api.ApplicationAPIUri;
import com.inspur.emmcloud.application.bean.App;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;


public class ApplicationUriUtils {

    public static void openApp(final Activity activity, final App app, final String appCollectType) {
        String uri = app.getUri();
        switch (app.getAppType()) {
            case 0:
            case 1:
                try {
                    Intent intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME);
                    intent.putExtra("installUri", app.getInstallUri());
                    intent.setComponent(null);
                    activity.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.show(activity,
                            R.string.app_not_support_app_type);
                }
                break;
            case 2:
                new AppCenterNativeAppUtils().InstallOrOpen(activity, app);
                break;
            case 3:
            case 4:
            case 6:
                if (app.getAppID().equals("456166a362436750d74bfeaef997693d")) {
                    new AppCenterApprovalUtils().openApprovalApp(activity, app);
                } else if (app.getIsSSO() == 1) {
                    String url = ApplicationAPIUri.getAppRealUrl(app.getAppID());
                    if (NetUtils.isNetworkConnected(activity)) {
                        new WebAppUtils(activity, new WebAppUtils.OnGetWebAppRealUrlListener() {
                            @Override
                            public void getWebAppRealUrlSuccess(String webAppUrl) {
                                openWebApp(activity, webAppUrl, app);
                            }

                            @Override
                            public void getWebAppRealUrlFail() {
                                ToastUtils.show(activity, R.string.react_native_app_open_failed);
                            }
                        }).getWebAppRealUrl(url);
                    }

                } else {
                    openWebApp(activity, uri, app);
                }
                break;
            case 5:
                Bundle bundle = new Bundle();
                bundle.putString("ecc-app-react-native", uri);
                IntentUtils.startActivity(activity, ReactNativeAppActivity.class, bundle);
                break;
            case 7:
                OfflineAppUtil.handleOfflineWeb(activity, app);
                break;

            default:
                ToastUtils.show(activity,
                        R.string.app_not_support_app_type);
                break;
        }
        //应用pv收集
        String appID = (app.getAppID().equals("inspur_news_esg")) ? "news" : app.getAppID();  //新闻应用跟普通应用区分开
        PVCollectModelCacheUtils.saveCollectModel(appID, appCollectType);
    }

    /**
     * 打开web应用
     *
     * @param activity
     * @param uri
     * @param app
     */
    public static void openWebApp(Activity activity, String uri, App app) {
        Bundle bundle = new Bundle();
        bundle.putString(Constant.APP_WEB_URI, uri);
        bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, (app.getAppType() == 3 || (app.getAppType() == 6 && app.getUserHeader() == 1)) ? true : false);
        bundle.putString(Constant.WEB_FRAGMENT_APP_NAME, app.getAppName());
        bundle.putInt("is_zoomable", app.getIsZoomable());
        bundle.putString("help_url", app.getHelpUrl());
        bundle.putString("appId", app.getAppID());
        ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();


    }

    /**
     * 打开url
     *
     * @param context
     * @param uri
     */
    public static void openUrl(Activity context, String uri) {
        openUrl(context, uri, "  ", true);
    }

    /**
     * 打开url
     *
     * @param context
     * @param uri
     */
    public static void openUrl(Activity context, String uri, String appName, boolean isHaveNavBar) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        LogUtils.jasonDebug("uri===" + uri);
        bundle.putString("appName", appName);
        bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, isHaveNavBar);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
    }

}
