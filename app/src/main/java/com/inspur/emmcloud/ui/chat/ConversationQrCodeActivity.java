package com.inspur.emmcloud.ui.chat;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.CircleTextImageView;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_qrcode;
    }
}
