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
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.bean.chat.ScanCodeJoinConversationBean;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yufuchang on 2019/1/19.
 */
public class ConversationQrCodeActivity extends BaseActivity {

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

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
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

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnInvitationContentSuccess(ScanCodeJoinConversationBean scanCodeJoinConversationBean) {
            LogUtils.YfcDebug("返回成功：" + JSONUtils.toJSONString(scanCodeJoinConversationBean));
            LoadingDialog.dimissDlg(loadingDialog);
            Router router = Router.getInstance();
            if (router.getService(com.inspur.emmcloud.componentservice.web.WebService.class) != null) {
                com.inspur.emmcloud.componentservice.web.WebService service = router.getService(com.inspur.emmcloud.componentservice.web.WebService.class);
                Bitmap bitmap = service.getQrCodeWithContent("430Test", 1000);
                groupQrCodeImage.setImageBitmap(bitmap);
            }
        }

        @Override
        public void returnInvitationContentFail(String error, int errorCode) {
            LogUtils.YfcDebug("返回失败：" + error);
            //返回失败不消失 微信就是如此
//            LoadingDialog.dimissDlg(loadingDialog);
        }
    }
}
