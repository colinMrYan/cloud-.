package com.inspur.emmcloud.web.plugin.share;

import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.componentservice.communication.ShareToConversationListener;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.json.JSONObject;

public class ShareService extends ImpPlugin {
    public String successCb, failCb;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        if (action.equals("shareLink")) {
            shareLink(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return null;
    }

    private void shareLink(JSONObject paramsObject) {
        final String url = JSONUtils.getString(paramsObject, "url", "");
        final String title = JSONUtils.getString(paramsObject, "title", "");
        final String description = JSONUtils.getString(paramsObject, "description", "");
        final String poster = JSONUtils.getString(paramsObject, "poster", "");
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        UMConfigure.init(getFragmentContext(), "59aa1f8f76661373290010d3"
                , "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        UMConfigure.setLogEnabled(false);
        PlatformConfig.setWeixin("wx4eb8727ea9c26495", "56a0426315f1d0985a1cc1e75e96130d");
        PlatformConfig.setQQZone("1105561850", "1kaw4r1c37SUupFL");
        final CustomShareListener mShareListener = new CustomShareListener();
        ShareAction shareAction = new ShareAction(getActivity())
                .setDisplayList(SHARE_MEDIA.EMAIL, SHARE_MEDIA.SMS)
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (share_media == null) {
                            if (snsPlatform.mKeyword.equals("app_name")) {
                                Router router = Router.getInstance();
                                if (router.getService(CommunicationService.class) != null) {
                                    CommunicationService service = router.getService(CommunicationService.class);
                                    service.shareExtendedLinksToConversation(poster, title, description, url, new ShareToConversationListener() {
                                        @Override
                                        public void shareSuccess(String cid) {
                                            callbackSuccess();
                                        }

                                        @Override
                                        public void shareFail() {
                                            callbackFail();
                                        }

                                        @Override
                                        public void shareCancel() {

                                        }
                                    });
                                }
                            }
                        } else {
                            UMImage thumb = new UMImage(getActivity(), poster);
                            UMWeb web = new UMWeb(url);
                            web.setThumb(thumb);
                            web.setDescription(description);
                            web.setTitle(title);
                            new ShareAction(getActivity()).withMedia(web)
                                    .setPlatform(share_media)
                                    .setCallback(mShareListener)
                                    .share();
                        }
                    }
                });
        String appIconResName = AppUtils.getAppIconResName(getFragmentContext());
        Router router = Router.getInstance();
        if (router.getService(AppService.class) != null) {
            AppService service = router.getService(AppService.class);
            if (service.isTabExist(Constant.APP_TAB_BAR_COMMUNACATE)) {
                shareAction = shareAction.addButton(getFragmentContext().getString(R.string.baselib_internal_share), "app_name", appIconResName, appIconResName);
            }
        }
        shareAction.open();
    }

    private void callbackSuccess() {
        if (!StringUtils.isBlank(successCb)) {
            this.jsCallback(successCb);
        }
    }

    private void callbackFail() {
        if (!StringUtils.isBlank(failCb)) {
            this.jsCallback(failCb);
        }
    }

    @Override
    public void onDestroy() {

    }

    private class CustomShareListener implements UMShareListener {

        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            callbackSuccess();
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            callbackFail();
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {

        }
    }

}
