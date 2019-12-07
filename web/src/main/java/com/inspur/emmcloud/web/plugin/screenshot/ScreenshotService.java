package com.inspur.emmcloud.web.plugin.screenshot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.imageedit.IMGEditActivity;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
        UMConfigure.init(getActivity(), "59aa1f8f76661373290010d3"
                , "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        PlatformConfig.setWeixin(Constant.WECHAT_APPID, "56a0426315f1d0985a1cc1e75e96130d");
        PlatformConfig.setQQZone("1105561850", "1kaw4r1c37SUupFL");
        mShareListener = new CustomShareListener(getActivity());

        ShareAction shareAction = new ShareAction(getActivity());
        shareAction.setDisplayList(
                SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.SMS
        );
        shareAction.setShareboardclickCallback(new ShareBoardlistener() {
            @Override
            public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                if (snsPlatform.mKeyword.equals("CLOUDPLUSE")) {
                    ArrayList<String> urlList = new ArrayList<>();
                    urlList.add(screenshotImgPath);
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(Constant.SHARE_FILE_URI_LIST, urlList);
                    ARouter.getInstance().build(Constant.AROUTER_CLASS_COMMUNICATION_SHARE_FILE).with(bundle).navigation();
                } else {
                    UMImage thumb = new UMImage(getActivity(), new File(screenshotImgPath));
                    new ShareAction(getActivity()).withMedia(thumb)
                            .setPlatform(share_media)
                            .setCallback(mShareListener)
                            .share();
                }
            }
        });
        shareAction.addButton(getFragmentContext().getString(R.string.clouddrive_internal_sharing), "CLOUDPLUSE", "ic_launcher_share", "ic_launcher_share");
        shareAction.open();

    }

    @Override
    public void onDestroy() {

    }

    private static class CustomShareListener implements UMShareListener {

        private WeakReference<BaseActivity> mActivity;

        private CustomShareListener(Activity activity) {
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
