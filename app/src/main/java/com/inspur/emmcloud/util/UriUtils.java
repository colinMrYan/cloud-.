package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.App;
import com.inspur.emmcloud.bean.Contact;
import com.inspur.emmcloud.bean.PVCollectModel;
import com.inspur.emmcloud.ui.app.ReactNativeAppActivity;
import com.inspur.emmcloud.ui.app.groupnews.GroupNewsActivity;
import com.inspur.imp.api.ImpActivity;


public class UriUtils {
    public static String tanent;

    public static void openApp(final Activity activity,final App app) {
        String uri = app.getUri();
        int appType = app.getAppType();
        switch (appType) {
            case 0:
            case 1:
                if (uri.equals("emm://news")) {
                    IntentUtils.startActivity(activity, GroupNewsActivity.class);
                } else {
                    ToastUtils.show(activity,
                            R.string.not_support_app_type);
                }

                break;
            case 2:
                new AppCenterNativeAppUtils().InstallOrOpen(activity, app);
                break;
            case 3:
            case 4:
                if (!uri.startsWith("https://emm.inspur.com:443/ssohandler/gs/") && !uri.startsWith("https://emm.inspur.com/ssohandler/gs/")) {
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
    public static void openUrl(Activity context, String uri, String header) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        String token = ((MyApplication) context.getApplicationContext())
                .getToken();
        bundle.putString("Authorization", token);
        bundle.putString("userAgentExtra",
                "/emmcloud/" + AppUtils.getVersion(context));
        String webLanguageCookie = getLanguageCookie(context);
        bundle.putString("cookie", webLanguageCookie);
        bundle.putString("appName", header);
        IntentUtils.startActivity(context, ImpActivity.class, bundle);
    }


    /**
     * 获取带语言的cookie
     *
     * @return
     */
    public static String getLanguageCookie(Context context) {
        // TODO Auto-generated method stub
        String languageJson = PreferencesUtils.getString(context, tanent
                + "appLanguageObj");
        String cookie = "";
        if (languageJson != null) {
            cookie = languageJson;
        }
        return cookie;
    }

    /**
     * 预览图片或视频
     **/
    public static String getPreviewUri(String fileName) {
        return getResUri("stream/" + fileName);
    }


    public static String getResUri(String url) {
        return "https://ecm.inspur.com/" + tanent + "/res/" + url;
    }

    /**
     * 获取推送新闻
     **/
    public static String getGroupNewsUrl(String url) {
        return "https://ecm.inspur.com/" + url;
    }

    ;

    /**
     * 个人信息头像显示图片
     **/
    public static String getUserInfoPhotoUri(String url) {
        return "https://emm.inspur.com" + url;
    }

    /**
     * 获取机器人头像路径
     *
     * @return
     */
    public static String getRobotIconUri(String iconUrl) {
        return "https://ecm.inspur.com/" + UriUtils.tanent + "/avatar/stream/"
                + iconUrl;
    }


    /**
     * 频道页面头像显示图片
     **/
    public static String getChannelImgUri(Context context, String inspurID) {
        String headImgUrl = null;
        if (StringUtils.isBlank(inspurID) || inspurID.equals("null"))
            return null;
        headImgUrl = ((MyApplication) context.getApplicationContext()).getUserPhotoUrl(inspurID);
        if (headImgUrl == null && !((MyApplication) context.getApplicationContext()).isKeysContainUid(inspurID)) {
            Contact contact = ContactCacheUtils.getUserContact(context, inspurID);
            if (contact != null) {
                headImgUrl = "https://emm.inspur.com/img/userhead/" + inspurID;
                String lastUpdateTime = contact.getLastUpdateTime();
                if (!StringUtils.isBlank(lastUpdateTime) && (!lastUpdateTime.equals("null"))) {
                    headImgUrl = headImgUrl + "?" + lastUpdateTime;
                }
                ((MyApplication) context.getApplicationContext()).setUsesrPhotoUrl(inspurID, headImgUrl);
            } else if (((MyApplication) context.getApplicationContext())
                    .getIsContactReady()) {
                ((MyApplication) context.getApplicationContext()).setUsesrPhotoUrl(inspurID, null);
            }
        }
        return headImgUrl;
    }




}
