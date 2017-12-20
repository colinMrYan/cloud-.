package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.bean.appcenter.App;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.ui.appcenter.ReactNativeAppActivity;
import com.inspur.emmcloud.ui.appcenter.groupnews.GroupNewsActivity;
import com.inspur.emmcloud.ui.appcenter.volume.VolumeHomePageActivity;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.privates.db.PVCollectModelCacheUtils;
import com.inspur.imp.api.ImpActivity;


public class UriUtils {

    public static void openApp(final Activity activity,final App app) {
        String uri = app.getUri();
        int appType = app.getAppType();
        switch (appType) {
            case 0:
            case 1:
                switch (uri){
                    case "emm://news":
                        IntentUtils.startActivity(activity, GroupNewsActivity.class);
                        break;
                    case "emm://volume":
                        IntentUtils.startActivity(activity, VolumeHomePageActivity.class);
                        break;
                    default:
                        ToastUtils.show(activity,
                                R.string.not_support_app_type);
                        break;
                }
                break;
            case 2:
                new AppCenterNativeAppUtils().InstallOrOpen(activity, app);
                break;
            case 3:
            case 4:
                if (!uri.startsWith(APIUri.getECMBaseUrl()+"ssohandler/gs/")) {
                    openWebApp(activity, uri, app);
                } else {
                    uri = uri.replace("/gs/", "/gs_uri/");
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
                        }).getWebAppRealUrl(uri);
                    }

                }
                break;
            case 5:
                Bundle bundle = new Bundle();
                bundle.putString("ecc-app-react-native", uri);
                IntentUtils.startActivity(activity, ReactNativeAppActivity.class, bundle);
                break;

            default:
                ToastUtils.show(activity,
                        R.string.not_support_app_type);
                break;
        }
        saveAPPPVCollect(activity, app);
    }

    /**
     * 应用pv收集
     * @param activity
     * @param app
     */
    private static void saveAPPPVCollect(Activity activity, App app) {
        String appID = (app.getAppID().equals("inspur_news_esg"))?"news":app.getAppID();  //新闻应用跟普通应用区分开
        PVCollectModel pvCollectModel = new PVCollectModel(appID, "application");
        PVCollectModelCacheUtils.saveCollectModel(activity, pvCollectModel);
    }

    /**
     * 打开web应用
     *
     * @param activity
     * @param uri
     * @param app
     */
    public static void openWebApp(Activity activity, String uri, App app) {
        Intent intent = new Intent();
        intent.setClass(activity, ImpActivity.class);
        intent.putExtra("uri", uri);
        if (app.getAppType() == 3) {
            intent.putExtra("appName", app.getAppName());
        }
        intent.putExtra("is_zoomable", app.getIsZoomable());
        intent.putExtra("help_url", app.getHelpUrl());
        intent.putExtra("appId", app.getAppID());
        activity.startActivity(intent);
    }


    /**
     * 打开url
     *
     * @param context
     * @param uri
     * @param header
     */
    public static void openUrl(Activity context, String uri) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        bundle.putString("appName", "");
        IntentUtils.startActivity(context, ImpActivity.class, bundle);
    }

}
