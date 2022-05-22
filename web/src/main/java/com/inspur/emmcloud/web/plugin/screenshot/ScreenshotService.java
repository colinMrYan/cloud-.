package com.inspur.emmcloud.web.plugin.screenshot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.PlatformName;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by chenmch on 2019/12/6.
 */

public class ScreenshotService extends ImpPlugin {
    private CustomShareListener mShareListener;
    private String successCb, failCb;
    private boolean shareThirdParty = true;
    @Override
    public void execute(String action, JSONObject paramsObject) {
        if (action.equals("do")) {
            screenshot(paramsObject);
        } else if(action.equals("enableScreenshot")){
            setWindowSecure(true);
        } else if(action.equals("disableScreenshot")){
            setWindowSecure(false);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return null;
    }


    /**
     * 动态设置防截屏
     * @param isSecure
     */
    private void setWindowSecure(boolean isSecure) {
        if (isSecure) {
            if ((getActivity().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_SECURE) != 0) {
                LogUtils.YfcDebug( "flag already set secure");
                return;
            }
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            if ((getActivity().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_SECURE) == 0) {
                LogUtils.YfcDebug(  "flag already set unsecure");
                return;
            }
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    private void screenshot(JSONObject paramsObject) {
        if (getImpCallBackInterface() != null) {
            getImpCallBackInterface().hideScreenshotImg();
        }
        JSONObject options = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        shareThirdParty = JSONUtils.getBoolean(options, "isShare", true);
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        String screenshotImgPath = ScreenshotUtil.screenshot(getActivity());
        AppUtils.refreshMedia(getFragmentContext(), screenshotImgPath);
        if (getImpCallBackInterface() != null) {
            getImpCallBackInterface().showScreenshotImg(screenshotImgPath);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String screenshotImgPath = data.getStringExtra(IMGEditActivity.OUT_FILE_PATH);
            AppUtils.refreshMedia(getFragmentContext(), screenshotImgPath);
            if (shareThirdParty) {
                shareScreenshotImg(screenshotImgPath);
            }

        }
    }

    /**
     * 分享到微信 QQ
     **/
    public void shareScreenshotImg(final String screenshotImgPath) {
        UMConfigure.init(getActivity(), "59aa1f8f76661373290010d3"
                , "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        PlatformConfig.setWeixin(Constant.WECHAT_APPID, "56a0426315f1d0985a1cc1e75e96130d");
        PlatformConfig.setQQZone("1105561850", "1kaw4r1c37SUupFL");
        mShareListener = new CustomShareListener(getActivity(), new ShareCallback() {
            @Override
            public void shareSuccess() {
                JSONObject json = new JSONObject();
                try {
                    json.put("status", 1);
                    JSONObject result = new JSONObject();
                    json.put("result", result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsCallback(successCb, json);
            }

            @Override
            public void shareFailure(String errorMessage) {
                jsCallback(failCb, errorMessage);
            }

            @Override
            public void shareCancel() {
                jsCallback(failCb, "取消分享");
            }
        });

        ShareAction shareAction = new ShareAction(getActivity());
        shareAction.setShareboardclickCallback(new ShareBoardlistener() {
            @Override
            public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {

                switch (snsPlatform.mKeyword) {
                    case "CLOUDPLUSE":
                        ArrayList<String> urlList = new ArrayList<>();
                        urlList.add(screenshotImgPath);
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList(Constant.SHARE_FILE_URI_LIST, urlList);
                        ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_SHARE_FILE).with(bundle).navigation();
                        break;
                    case "wechat":
                        new ShareAction(getActivity()).withMedia(new UMImage(getActivity(), new File(screenshotImgPath)))
                                .setPlatform(SHARE_MEDIA.WEIXIN)
                                .setCallback(mShareListener)
                                .share();
                        break;
                    case "qq":
                        new ShareAction(getActivity()).withMedia(new UMImage(getActivity(), new File(screenshotImgPath)))
                                .setPlatform(SHARE_MEDIA.QQ)
                                .setCallback(mShareListener)
                                .share();
                        break;
                }
            }
        });


        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), "com.tencent.mm")) {
            shareAction.addButton(PlatformName.WEIXIN, "wechat", "umeng_socialize_wechat", "umeng_socialize_wechat");
        }
        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), "com.tencent.mobileqq")) {
            shareAction.addButton(PlatformName.QQ, "qq", "umeng_socialize_qq", "umeng_socialize_qq");
        }
        shareAction.addButton(getFragmentContext().getString(R.string.internal_sharing), "CLOUDPLUSE", "ic_launcher_share", "ic_launcher_share");
        shareAction.open();

    }


    @Override
    public void onDestroy() {

    }

    private interface ShareCallback {
        void shareSuccess();
        void shareFailure(String errorMessage);
        void shareCancel();
    }

    private static class CustomShareListener implements UMShareListener {

        private WeakReference<BaseActivity> mActivity;
        private ShareCallback shareCallback;
        private String failure;

        private CustomShareListener(Activity activity, ShareCallback callback) {
            mActivity = new WeakReference(activity);
            shareCallback = callback;
        }

        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            ToastUtils.show(mActivity.get(), R.string.baselib_share_success);
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            ToastUtils.show(mActivity.get(), R.string.baselib_share_fail);
            if (t != null) {
                LogUtils.jasonDebug("throw:" + t.getMessage());
            }
            shareCallback.shareFailure(t != null ? t.getMessage() : "分享失败！！");
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            shareCallback.shareCancel();
        }
    }
}
