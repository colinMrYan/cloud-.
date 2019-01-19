package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.widget.CircleTextImageView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by yufuchang on 2019/1/19.
 */
@ContentView(R.layout.activity_group_qrcode)
public class ConversationQrCodeActivity extends BaseActivity{

    @ViewInject(R.id.iv_group_image)
    private CircleTextImageView groupCircleTextImageView;
    @ViewInject(R.id.tv_group_name)
    private TextView groupNameText;
    @ViewInject(R.id.iv_group_qrcode)
    private ImageView groupQrCodeImage;
    @ViewInject(R.id.btn_share_group_qrcode)
    private Button shareGroupQrCodeBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
