package com.inspur.emmcloud.web.plugin.share;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okio.ByteString;

public class ShareSocialService extends ImpPlugin {
    public String successCb, failCb;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        switch (action) {
            case "shareUrl":
                shareLink(paramsObject);
                break;
            case "shareText":
                shareText(paramsObject);
                break;
            case "shareImage":
                shareImage(paramsObject);
                break;
            default:
                showCallIMPMethodErrorDlg();
                break;
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return null;
    }

    private void shareText(JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        JSONObject optionsObj = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        final String text = JSONUtils.getString(optionsObj, "text", "");
        ShareBoardlistener shareBoardlistener = new ShareBoardlistener() {
            @Override
            public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                new ShareAction(getActivity()).withText(text).setPlatform(share_media).setCallback(new CustomShareListener()).share();
            }
        };
        showSharePlatform(shareBoardlistener);
    }

    private void shareLink(JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        JSONObject optionsObj = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        final String webpageUrl = JSONUtils.getString(optionsObj, "webpageUrl", "");
        final String title = JSONUtils.getString(optionsObj, "title", "");
        final String descr = JSONUtils.getString(optionsObj, "descr", "  ");
        final String thumImage = JSONUtils.getString(optionsObj, "thumImage", "");

        ShareBoardlistener shareBoardlistener = new ShareBoardlistener() {
            @Override
            public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                UMWeb web = new UMWeb(webpageUrl);
                if (!StringUtils.isBlank(thumImage)) {
                    UMImage thumb = new UMImage(getActivity(), thumImage);
                    web.setThumb(thumb);
                }
                web.setDescription(descr);
                web.setTitle(title);
                new ShareAction(getActivity()).withMedia(web)
                        .setPlatform(share_media)
                        .setCallback(new CustomShareListener())
                        .share();
            }
        };
        showSharePlatform(shareBoardlistener);
    }

    private void shareImage(JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        JSONObject optionsObj = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        final String shareImage = JSONUtils.getString(optionsObj, "shareImage", "");
        final String title = JSONUtils.getString(optionsObj, "title", "");
        final String descr = JSONUtils.getString(optionsObj, "descr", "  ");
        final String thumImage = JSONUtils.getString(optionsObj, "thumImage", "");
        final String base64ShareImage = JSONUtils.getString(optionsObj, "base64ShareImage", "");
        final String base64ThumbImage = JSONUtils.getString(optionsObj, "base64ThumbImage", "");
        final Bitmap decodeShareImage = decodeBase64ToBitmap(base64ShareImage);
        final Bitmap decodeThumbImage = decodeBase64ToBitmap(base64ThumbImage);
        if (TextUtils.isEmpty(shareImage) && decodeShareImage == null) return;
        ShareBoardlistener shareBoardlistener = new ShareBoardlistener() {
            @Override
            public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                if (snsPlatform.mKeyword.equals("SAVE")) {
                    saveBitmapFile(decodeShareImage,"share.jpg");
                    return;
                }
                UMImage image,thumbImage;
                if (!TextUtils.isEmpty(shareImage)) {
                    image = new UMImage(getActivity(), shareImage);
                } else {
                    image = new UMImage(getActivity(), decodeShareImage);
                }
                if (decodeThumbImage != null) {
                    thumbImage = new UMImage(getActivity(), decodeThumbImage);
                } else {
                    thumbImage = new UMImage(getActivity(), thumImage);
                }
                image.setTitle(title);
                image.setDescription(descr);
                image.setThumb(thumbImage);
                new ShareAction(getActivity()).withMedia(image)
                        .setPlatform(share_media)
                        .setCallback(new CustomShareListener())
                        .share();
            }
        };
        showSharePlatform(shareBoardlistener);
    }

    private void showSharePlatform(ShareBoardlistener shareBoardlistener) {
        UMConfigure.init(getFragmentContext(), "59aa1f8f76661373290010d3"
                , "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
        UMConfigure.setLogEnabled(false);
        PlatformConfig.setWeixin(Constant.WECHAT_APPID, "56a0426315f1d0985a1cc1e75e96130d");
        PlatformConfig.setQQZone("1105561850", "1kaw4r1c37SUupFL");
        ShareAction shareAction = new ShareAction(getActivity())
                .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                        SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE)
                .setShareboardclickCallback(shareBoardlistener);
        shareAction.addButton("保存到本地", "SAVE", "ic_file_download", "ic_file_download");
        shareAction.open();
    }

    public String saveBitmapFile(Bitmap bitmap, String saveImageName) {
        String saveImageFolder = "IMP-Cloud/cache/share/";
        File temp = new File(Environment.getExternalStorageDirectory() + "/" + saveImageFolder);// 要保存文件先创建文件夹
        if (!temp.exists()) {
            temp.mkdir();
        }
        String savedImagePath = MyAppConfig.LOCAL_CACHE_PATH + "share/" + saveImageName;

        File file = new File(savedImagePath);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            ToastUtils.show(BaseApplication.getInstance(), BaseApplication.getInstance().getString(com.inspur.baselib.R.string.communication_save_image_success, saveImageFolder));
        } catch (IOException e) {
            ToastUtils.show(BaseApplication.getInstance(), BaseApplication.getInstance().getString(com.inspur.baselib.R.string.save_fail));
            e.printStackTrace();
        }
        return savedImagePath;
    }

    private void callbackSuccess() {
        if (!StringUtils.isBlank(successCb)) {
            this.jsCallback(successCb);
        }
    }

    private void callbackFail(String errorMessage) {
        if (!StringUtils.isBlank(failCb)) {
            JSONObject object = new JSONObject();
            try {
                object.put("errorMessage", errorMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.jsCallback(failCb, object);
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64Str) {
        try {
            byte[] input = ByteString.decodeBase64(base64Str).toByteArray();
            return BitmapFactory.decodeByteArray(input, 0, input.length);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
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
            callbackFail(t.getMessage());
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {

        }
    }

}
