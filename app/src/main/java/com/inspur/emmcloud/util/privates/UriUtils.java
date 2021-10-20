package com.inspur.emmcloud.util.privates;

import android.app.Activity;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.config.Constant;


public class UriUtils {


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
     */
    public static void openUrl(Activity context, String uri, boolean isShowHeader) {
        openUrl(context, uri, "  ", isShowHeader);
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
