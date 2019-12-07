package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.SearchModel;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.GetCreateSingleChannelResult;
import com.inspur.emmcloud.bean.chat.ScanCodeJoinConversationBean;
import com.inspur.emmcloud.ui.chat.mvp.view.ConversationSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.ui.contact.ContactSearchFragment;
import com.inspur.emmcloud.ui.mine.setting.RecommendAppActivity;
import com.inspur.emmcloud.util.privates.ChatCreateUtils;
import com.inspur.emmcloud.util.privates.ConversationCreateUtils;
import com.inspur.emmcloud.util.privates.ShareFile2OutAppUtils;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.PlatformName;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yufuchang on 2019/1/19.
 */
public class ConversationQrCodeActivity extends BaseActivity {

    private static final int SHARE_LINK = 1;
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
    @BindView(R.id.tv_valid_date)
    TextView validDateText;
    private String cid;
    private LoadingDialog loadingDialog;
    private CustomShareListener shareConversationListener;
    private String shareUrl = "";
    private String groupName = "";

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
        groupName = getIntent().getStringExtra("groupName");
        groupNameText.setText(getString(R.string.chat_group_member_size, groupName, getIntent().getIntExtra(ConversationGroupInfoActivity.MEMBER_SIZE, 0)));
        validDateText.setText(getValidDate());
    }

    /**
     * 获取有效时间
     *
     * @return
     */
    private String getValidDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        return getString(R.string.channel_group_qrcode_valid, TimeUtils.calendar2FormatString(this, calendar, TimeUtils.FORMAT_MONTH_DAY));
    }

    /**
     * 获取扫码加群二维码内容
     */
    private void getQrCodeContent() {
        if (NetUtils.isNetworkConnected(this)) {
            loadingDialog.show();
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
        ShareAction shareAction = new ShareAction(ConversationQrCodeActivity.this);
        shareAction.setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (snsPlatform.mKeyword.equals("CLOUDPLUSE")) {
                            shareGroupToFriends();
                        } else {
                            String tip = getString(R.string.chat_group_welcome_join_group, groupName);
                            UMImage thumb = new UMImage(ConversationQrCodeActivity.this, R.drawable.ic_launcher_share);
                            UMWeb web = new UMWeb(shareUrl);
                            web.setThumb(thumb);
                            web.setDescription(tip);
                            web.setTitle(tip);
                            new ShareAction(ConversationQrCodeActivity.this).withMedia(web)
                                    .setPlatform(getPlatform(snsPlatform.mKeyword))
                                    .setCallback(shareConversationListener)
                                    .share();
                        }

                    }
                });

        shareAction.addButton(getString(R.string.clouddrive_internal_sharing), "CLOUDPLUSE", "ic_launcher_share", "ic_launcher_share");
        if (AppUtils.isAvilibleByPackageName(BaseApplication.getInstance(), ShareFile2OutAppUtils.PACKAGE_WECHAT)) {
            shareAction.addButton(PlatformName.WEIXIN, "wechat", "umeng_socialize_wechat", "umeng_socialize_wechat");
            shareAction.addButton(PlatformName.WEIXIN_CIRCLE, "wxcircle", "umeng_socialize_wxcircle", "umeng_socialize_wxcircle");
        }
        if (AppUtils.isAvilibleByPackageName(BaseApplication.getInstance(), ShareFile2OutAppUtils.PACKAGE_MOBILE_QQ)) {
            shareAction.addButton(PlatformName.QQ, "qq", "umeng_socialize_qq", "umeng_socialize_qq");
            shareAction.addButton(PlatformName.QZONE, "qzone", "umeng_socialize_qzone", "umeng_socialize_qzone");
        }
        shareAction.open();

    }

    private SHARE_MEDIA getPlatform(String mKeyword) {
        SHARE_MEDIA shareMedia = null;
        switch (mKeyword) {
            case "wechat":
                shareMedia = SHARE_MEDIA.WEIXIN;
                break;
            case "wxcircle":
                shareMedia = SHARE_MEDIA.WEIXIN_CIRCLE;
                break;
            case "qq":
                shareMedia = SHARE_MEDIA.QQ;
                break;
            case "qzone":
                shareMedia = SHARE_MEDIA.QZONE;
                break;
            default:
                break;
        }
        return shareMedia;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK && requestCode == SHARE_LINK
                && NetUtils.isNetworkConnected(getApplicationContext())) {
            handleShareResult(data);
        } else {
            finish();
        }
    }

    /**
     * 分享群给朋友
     */
    private void shareGroupToFriends() {
        Intent intent = new Intent();
        intent.putExtra("select_content", 0);
        intent.putExtra("isMulti_select", false);
        intent.putExtra("isContainMe", false);
        intent.putExtra("title", getString(R.string.baselib_share_to));
        String title = JSONUtils.getString(shareUrl, "title", shareUrl);
        title = getString(R.string.baselib_share_link) + title;
        intent.putExtra(ContactSearchFragment.EXTRA_SHOW_COMFIRM_DIALOG_WITH_MESSAGE, title);
        intent.putExtra(ContactSearchFragment.EXTRA_SHOW_COMFIRM_DIALOG, true);
        intent.setClass(getApplicationContext(),
                ContactSearchActivity.class);
        Intent shareIntent = new Intent(this, ConversationSearchActivity.class);
        shareIntent.putExtra(Constant.SHARE_CONTENT, title);
        startActivityForResult(shareIntent, SHARE_LINK);
    }

    private void handleShareResult(Intent data) {
        SearchModel searchModel = (SearchModel) data.getSerializableExtra("searchModel");
        if (searchModel != null) {
            String userOrChannelId = searchModel.getId();
            boolean isUser = searchModel.getType().equals(SearchModel.TYPE_USER);
            share2Conversation(userOrChannelId, isUser);
        } else {
            finish();
        }
    }

    /**
     * 分享到聊天界面
     *
     * @param userOrChannelId
     * @param isUser
     */
    private void share2Conversation(String userOrChannelId, boolean isUser) {
        if (StringUtils.isBlank(userOrChannelId)) {
            ToastUtils.show(MyApplication.getInstance(), getString(R.string.baselib_share_fail));
        } else {
            if (isUser) {
                createConversation(userOrChannelId);
            } else {
                startChannelActivity(userOrChannelId);
            }
        }
    }

    /**
     * 创建聊天
     *
     * @param uid
     */
    private void createConversation(String uid) {
        if (WebServiceRouterManager.getInstance().isV1xVersionChat()) {
            new ConversationCreateUtils().createDirectConversation(ConversationQrCodeActivity.this, uid,
                    new ConversationCreateUtils.OnCreateDirectConversationListener() {
                        @Override
                        public void createDirectConversationSuccess(Conversation conversation) {
                            startChannelActivity(conversation.getId());
                        }

                        @Override
                        public void createDirectConversationFail() {

                        }
                    });
        } else {
            new ChatCreateUtils().createDirectChannel(ConversationQrCodeActivity.this, uid,
                    new ChatCreateUtils.OnCreateDirectChannelListener() {
                        @Override
                        public void createDirectChannelSuccess(GetCreateSingleChannelResult getCreateSingleChannelResult) {
                            startChannelActivity(getCreateSingleChannelResult.getCid());
                        }

                        @Override
                        public void createDirectChannelFail() {
                            //ToastUtils.show(ShareLinkActivity.this,getString(R.string.news_share_fail));
                            finish();
                        }
                    });
        }

    }

    /**
     * 组织链接内容
     *
     * @return
     */
    private String conbineGroupNewsContent() {
        JSONObject jsonObject = new JSONObject();
        String tip = getString(R.string.chat_group_welcome_join_group, groupName);
        try {
            jsonObject.put("url", shareUrl);
            jsonObject.put("poster", "");
            jsonObject.put("digest", tip);
            jsonObject.put("title", tip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * 打开channel
     */
    private void startChannelActivity(String cid) {
        Bundle bundle = new Bundle();
        bundle.putString("cid", cid);
        bundle.putString("share_type", "link");
        bundle.putSerializable(Constant.SHARE_LINK, conbineGroupNewsContent());
        IntentUtils.startActivity(ConversationQrCodeActivity.this, ConversationActivity.class, bundle, true);
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
            LoadingDialog.dimissDlg(loadingDialog);
            shareUrl = scanCodeJoinConversationBean.getConversationQrCode();
            Router router = Router.getInstance();
            if (router.getService(com.inspur.emmcloud.componentservice.web.WebService.class) != null) {
                com.inspur.emmcloud.componentservice.web.WebService service = router.getService(com.inspur.emmcloud.componentservice.web.WebService.class);
//                File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH + "/" +
//                        MyApplication.getInstance().getTanent() + cid + "_100.png1");
//                Bitmap logoBitmap = ImageUtils.getBitmapByFile(file);
                Bitmap bitmap = service.getQrCodeWithContent(scanCodeJoinConversationBean.getConversationQrCode(), SHARE_QR_CODE_SIZE);
                //测试代码，写死地址测试跳转到群消息页面，并拉消息，排序会话列表
//                Bitmap bitmap = service.getQrCodeWithContent("http://emm.inspuronline.com:83/demo/join_group.html", BitmapFillet.fillet(logoBitmap, 15, BitmapFillet.CORNER_ALL), SHARE_QR_CODE_SIZE);
                groupQrCodeImage.setImageBitmap(bitmap);
            }
            shareGroupQrCodeBtn.setVisibility(View.VISIBLE);
        }

        @Override
        public void returnInvitationContentFail(String error, int errorCode) {
            shareGroupQrCodeBtn.setVisibility(View.GONE);
            //返回失败不消失 微信是这样
//            LoadingDialog.dimissDlg(loadingDialog);
        }
    }
}
