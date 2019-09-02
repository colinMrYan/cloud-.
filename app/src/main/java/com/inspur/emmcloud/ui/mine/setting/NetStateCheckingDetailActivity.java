package com.inspur.emmcloud.ui.mine.setting;

import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.widget.CustomLoadingView;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.util.privates.CheckingNetStateUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/9/2.
 */

public class NetStateCheckingDetailActivity extends BaseActivity {

    public static String[] subUrls = {"www.baidu.com", "www.aliyun.com"};
    @BindView(R.id.tv_check_net_url_content)
    TextView checkNetUrlContentText;
    @BindView(R.id.tv_check_net_result_content)
    TextView checkNetResultText;
    @BindView(R.id.tv_check_net_current_net_type)
    TextView currentNetTypeText;
    @BindView(R.id.tv_check_net_current_net_state)
    TextView currentNetStateText;
    @BindView(R.id.iv_ping_inspur_state)
    ImageView pingInspurStateImage;
    @BindView(R.id.iv_ping_inspur_loading)
    CustomLoadingView pingInspurLoadingImage;
    @BindView(R.id.iv_ping_baidu_state)
    ImageView pingBaiduStateImage;
    @BindView(R.id.iv_ping_baidu_loading)
    CustomLoadingView pingbaiduLoadingImage;
    @BindView(R.id.iv_ping_ali_state)
    ImageView pingAliStateImage;
    @BindView(R.id.iv_ping_ali_loading)
    CustomLoadingView pingAliLoadingImage;
    CheckingNetStateUtils checkingNetStateUtils;
    private String PortalCheckingUrls = NetUtils.httpUrls[0];
    private String[] CheckHttpUrls = NetUtils.httpUrls;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_network_state_detail_new;
    }
}
