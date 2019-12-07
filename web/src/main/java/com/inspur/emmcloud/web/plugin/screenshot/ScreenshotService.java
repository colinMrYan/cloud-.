package com.inspur.emmcloud.web.plugin.screenshot;

import android.app.Activity;
import android.content.Intent;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.PlatformName;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;

/**
 * Created by chenmch on 2019/12/6.
 */

public class ScreenshotService extends ImpPlugin {
    private CustomShareListener mShareListener;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if (action.equals("do")) {
            screenshot();
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return null;
    }

    private void screenshot() {
        String screenshotImgPath = ScreenshotUtil.screenshot(getActivity());
        if (getImpCallBackInterface() != null) {
            getImpCallBackInterface().showScreenshotImg(screenshotImgPath);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String screenshotImgPath = data.getStringExtra(IMGEditActivity.OUT_FILE_PATH);
            shareScreenshotImg(screenshotImgPath);

        }
    }

    /**
     * 分享到微信 QQ
     **/
    public void shareScreenshotImg(final String screenshotImgPath) {
        mShareListener = new CustomShareListener((BaseActivity) getActivity());
        ShareAction shareAction = new ShareAction(getActivity()).setShareboardclickCallback(new ShareBoardlistener() {
            @Override
            public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                if (snsPlatform.mKeyword.equals("CLOUDPLUSE")) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), Sharef.class);
                    intent.putExtra(Constant.SHARE_FILE_URI_LIST, (Serializable) uriList);
                    context.startActivity(intent);
                } else {

                }
            }
        });
        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), "com.tencent.mm") {
            shareAction.addButton(PlatformName.WEIXIN, "WEIXIN", "umeng_socialize_wechat", "umeng_socialize_wechat");
        }
        if (AppUtils.isAppInstalled(BaseApplication.getInstance(), "com.tencent.mobileqq") {
            shareAction.addButton(PlatformName.QQ, "QQ", "umeng_socialize_qq", "umeng_socialize_qq");
        }
        shareAction.addButton(getFragmentContext().getString(R.string.clouddrive_internal_sharing), "CLOUDPLUSE", "ic_launcher_share", "ic_launcher_share");
        shareAction.open();

    }

    @Override
    public void onDestroy() {

    }

    private static class CustomShareListener implements UMShareListener {

        private WeakReference<BaseActivity> mActivity;

        private CustomShareListener(BaseActivity activity) {
            mActivity = new WeakReference(activity);
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

        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {

        }
    }
}
