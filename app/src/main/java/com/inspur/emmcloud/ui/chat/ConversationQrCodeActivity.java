package com.inspur.emmcloud.ui.chat;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.ScanCodeJoinConversationBean;
import com.inspur.emmcloud.ui.mine.setting.RecommendAppActivity;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.io.File;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yufuchang on 2019/1/19.
 */
public class ConversationQrCodeActivity extends BaseActivity {

    //二维码大小
    private final static int SHARE_QR_CODE_SIZE = 500;
    @BindView(R.id.iv_group_image)
    CircleTextImageView groupCircleTextImageView;
    @BindView(R.id.tv_group_name)
    TextView groupNameText;
    @BindView(R.id.iv_group_qrcode)
    ImageView groupQrCodeImage;
    @BindView(R.id.btn_share_group_qrcode)
    Button shareGroupQrCodeBtn;
    private String cid;
    private LoadingDialog loadingDialog;
    private CustomShareListener shareConversationListener;
    private String shareUrl = "";

    @Override
    public void onCreate() {
        ButterKnife.bind(this);


        UMConfigure.init(this, "59aa1f8f76661373290010d3"
                , "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
//        QueuedWork.isUseThreadPool = false;
//        UMShareAPI.get(this);
        PlatformConfig.setWeixin("wx4eb8727ea9c26495", "56a0426315f1d0985a1cc1e75e96130d");
        PlatformConfig.setQQZone("1105561850", "1kaw4r1c37SUupFL");
        initViews();
    }

    private void initViews() {
        loadingDialog = new LoadingDialog(this);
        this.cid = getIntent().getStringExtra("cid");
        File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH + "/" +
                MyApplication.getInstance().getTanent() + cid + "_100.png1");
        if (file.exists()) {
            groupCircleTextImageView.setImageBitmap(ImageUtils.getBitmapByFile(file));
        } else {
            groupCircleTextImageView.setImageResource(R.drawable.icon_channel_group_default);
        }
        getQrCodeContent();
        groupNameText.setText(getString(R.string.chat_group_member_size, getIntent().getIntExtra(ConversationGroupInfoActivity.MEMBER_SIZE, 0)));
    }

    /**
     * 获取扫码加群二维码内容
     */
    private void getQrCodeContent() {
        if (NetUtils.isNetworkConnected(this)) {
//            loadingDialog.show();
            ChatAPIService chatAPIService = new ChatAPIService(this);
            chatAPIService.setAPIInterface(new WebService());
            chatAPIService.getInvitationContent(cid);
        } else {
            finish();
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_qrcode;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
        }
    }


    @OnClick(R.id.btn_share_group_qrcode)
    public void ShareWeb() {
        shareConversationListener = new CustomShareListener(ConversationQrCodeActivity.this);
        new ShareAction(ConversationQrCodeActivity.this).setDisplayList(
                SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.SMS
        )
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (share_media == SHARE_MEDIA.SMS) {
                            new ShareAction(ConversationQrCodeActivity.this).withText("欢迎入群" + shareUrl)
                                    .setPlatform(share_media)
                                    .setCallback(shareConversationListener)
                                    .share();
                        } else {
                            UMImage thumb = new UMImage(ConversationQrCodeActivity.this, R.drawable.ic_launcher_share);
                            UMWeb web = new UMWeb(shareUrl);
                            web.setThumb(thumb);
                            web.setDescription("欢迎入群");
                            web.setTitle("欢迎入群");
                            new ShareAction(ConversationQrCodeActivity.this).withMedia(web)
                                    .setPlatform(share_media)
                                    .setCallback(shareConversationListener)
                                    .share();
                        }
                    }
                })
                .open();

    }

    private static class CustomShareListener implements UMShareListener {

        private WeakReference<RecommendAppActivity> mActivity;

        private CustomShareListener(ConversationQrCodeActivity activity) {
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

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnInvitationContentSuccess(ScanCodeJoinConversationBean scanCodeJoinConversationBean) {
            LogUtils.YfcDebug("返回成功：" + JSONUtils.toJSONString(scanCodeJoinConversationBean));
            LoadingDialog.dimissDlg(loadingDialog);
            shareUrl = scanCodeJoinConversationBean.getConversationQrCode();
            Router router = Router.getInstance();
            if (router.getService(com.inspur.emmcloud.componentservice.web.WebService.class) != null) {
                com.inspur.emmcloud.componentservice.web.WebService service = router.getService(com.inspur.emmcloud.componentservice.web.WebService.class);
                Bitmap bitmap = service.getQrCodeWithContent(scanCodeJoinConversationBean.getConversationQrCode(), SHARE_QR_CODE_SIZE);
                groupQrCodeImage.setImageBitmap(bitmap);
            }
            shareGroupQrCodeBtn.setVisibility(View.VISIBLE);
        }

        @Override
        public void returnInvitationContentFail(String error, int errorCode) {
            LogUtils.YfcDebug("返回失败：" + error + "错误码：" + errorCode);
            shareGroupQrCodeBtn.setVisibility(View.GONE);
            //返回失败不消失 微信是这样
//            LoadingDialog.dimissDlg(loadingDialog);
        }
    }
}
