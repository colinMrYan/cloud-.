package com.inspur.emmcloud.ui.chat;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.ImageUtils;
import com.inspur.emmcloud.baselib.widget.CircleTextImageView;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;

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

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initViews();
    }

    private void initViews() {
        Router router = Router.getInstance();
        File file = new File(MyAppConfig.LOCAL_CACHE_PHOTO_PATH + "/" +
                MyApplication.getInstance().getTanent() + getIntent().
                getStringExtra("cid") + "_100.png1");
        if (file.exists()) {
            groupCircleTextImageView.setImageBitmap(ImageUtils.getBitmapByFile(file));
        } else {
            groupCircleTextImageView.setImageResource(R.drawable.icon_channel_group_default);
        }
        getQrCodeContent();
        if (router.getService(com.inspur.emmcloud.componentservice.web.WebService.class) != null) {
            com.inspur.emmcloud.componentservice.web.WebService service = router.getService(com.inspur.emmcloud.componentservice.web.WebService.class);
            Bitmap bitmap = service.getQrCodeWithContent("430Test", 500);
            groupQrCodeImage.setImageBitmap(bitmap);
        }
    }

    private void getQrCodeContent() {
        if (NetUtils.isNetworkConnected(this)) {

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
}
