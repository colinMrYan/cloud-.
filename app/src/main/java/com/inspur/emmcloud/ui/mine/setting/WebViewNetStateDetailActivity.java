package com.inspur.emmcloud.ui.mine.setting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.CustomLoadingView;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.util.privates.CheckingNetStateUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by libaochao on 2019/9/2.
 */

public class WebViewNetStateDetailActivity extends BaseActivity {


    public static String EXTRA_OUTSIDE_URL = "extra_outside_url";
    public static String EXTRA_OUTSIDE_URL_REQUEST_RESULT = "extra_outside_url_request_result";
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
    CustomLoadingView pingBaiduLoadingImage;
    @BindView(R.id.iv_ping_ali_state)
    ImageView pingAliStateImage;
    @BindView(R.id.iv_ping_ali_loading)
    CustomLoadingView pingAliLoadingImage;

    CheckingNetStateUtils checkingNetStateUtils;
    private String[] CheckHttpUrls = NetUtils.httpUrls;

    private String urlContent; //展示url
    private String checkNetResultContent; //网络状态详情
    private Drawable drawableError;
    private Drawable drawableSuccess;


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        checkingNetStateUtils = new CheckingNetStateUtils(this);
        initData();
        iniView();
    }

    private void initData() {
        if (getIntent().hasExtra(EXTRA_OUTSIDE_URL)) {
            urlContent = getIntent().getStringExtra(EXTRA_OUTSIDE_URL);
        }
        if (getIntent().hasExtra(EXTRA_OUTSIDE_URL_REQUEST_RESULT)) {
            checkNetResultContent = getIntent().getStringExtra(EXTRA_OUTSIDE_URL_REQUEST_RESULT);
        }
    }

    private void iniView() {
        drawableError = getBaseContext().getResources().getDrawable(R.drawable.ic_netchecking_error);
        drawableSuccess = getBaseContext().getResources().getDrawable(R.drawable.ic_netchecking_ok);
        pingBaiduStateImage.setVisibility(View.GONE);
        pingBaiduLoadingImage.setVisibility(View.VISIBLE);
        pingAliStateImage.setVisibility(View.GONE);
        pingAliLoadingImage.setVisibility(View.VISIBLE);
        pingInspurStateImage.setVisibility(View.GONE);
        pingInspurLoadingImage.setVisibility(View.VISIBLE);
        checkNetUrlContentText.setText(urlContent);
        checkNetResultText.setText(checkNetResultContent);
        checkNetResultText.setMovementMethod(ScrollingMovementMethod.getInstance());
        checkNetUrlContentText.setMovementMethod(ScrollingMovementMethod.getInstance());
        checkNetResultText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 通知ScrollView控件不要干扰
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    // 通知ScrollView控件不要干扰
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });
        checkNetResultText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 通知ScrollView控件不要干扰
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    // 通知ScrollView控件不要干扰
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_webview_net_state_detail;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkingNetConnectState();
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.tv_copy_net_url:
                copyToClipboard(this, urlContent);
                break;
            case R.id.tv_copy_net_result_content:
                copyToClipboard(this, checkNetResultContent);
                break;
        }
    }

    /**
     * 文本复制到剪切板
     */
    private void copyToClipboard(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setPrimaryClip(ClipData.newPlainText(null, content));
        ToastUtils.show(context, R.string.copyed_to_paste_board);
    }


    /**
     * 通过个Url检测网络状态
     */
    private void checkingNetConnectState() {
        checkingNetStateUtils.CheckNetPingThreadStart(subUrls, 5, Constant.EVENTBUS_TAG_NET_PING_CONNECTION);
        checkingNetStateUtils.CheckNetHttpThreadStart(CheckHttpUrls);
        String networksType = checkingNetStateUtils.getNetworksType();
        currentNetTypeText.setText(getString(R.string.net_check_net_current_type) + networksType);   //net_check_net_current_type

    }

    /**
     * 沟通页网络异常提示框
     *
     * @param netState 通过Action获取操作类型
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dealCheckingPingUrls(SimpleEventMessage netState) {
        if (netState.getAction().equals(Constant.EVENTBUS_TAG_NET_PING_CONNECTION)) {
            CheckingNetStateUtils.PingUrlStateAction idAndData = (CheckingNetStateUtils.PingUrlStateAction) netState.getMessageObj();
            if (idAndData.getUrl().equals(subUrls[0])) {
                pingBaiduStateImage.setBackground(idAndData.isPingState() ? drawableSuccess : drawableError);
                pingBaiduStateImage.setVisibility(View.VISIBLE);
                pingBaiduLoadingImage.setVisibility(View.GONE);
            }
            if (idAndData.getUrl().equals(subUrls[1])) {
                pingAliStateImage.setBackground(idAndData.isPingState() ? drawableSuccess : drawableError);
                pingAliStateImage.setVisibility(View.VISIBLE);
                pingAliLoadingImage.setVisibility(View.GONE);
            }
        } else if (netState.getAction().equals(Constant.EVENTBUS_TAG_NET_HTTP_POST_CONNECTION)) {
            CheckingNetStateUtils.PingUrlStateAction idAndData = (CheckingNetStateUtils.PingUrlStateAction) netState.getMessageObj();
            if (idAndData.getUrl().equals(CheckHttpUrls[0])) {
                pingInspurStateImage.setBackground(idAndData.isPingState() ? drawableSuccess : drawableError);
                pingInspurStateImage.setVisibility(View.VISIBLE);
                pingInspurLoadingImage.setVisibility(View.GONE);
            }
        }
        String netStatePre = getApplication().getString(R.string.net_check_net_current_state);
        if (pingBaiduLoadingImage.getVisibility() == View.GONE && pingAliLoadingImage.getVisibility() == View.GONE &&
                pingInspurLoadingImage.getVisibility() == View.GONE) {
            if (pingBaiduStateImage.getBackground().equals(drawableError) && pingAliStateImage.getBackground().equals(drawableError)
                    && pingInspurStateImage.getBackground().equals(drawableError)) {
                currentNetStateText.setText(netStatePre + getApplication().getString(R.string.net_check_net_current_state_unconnected));
            } else {
                currentNetStateText.setText(netStatePre + getApplication().getString(R.string.net_check_net_current_state_connected));
            }
        } else {
            currentNetStateText.setText(netStatePre + getApplication().getString(R.string.net_check_net_current_state_unknown));
        }
        String networksType = checkingNetStateUtils.getNetworksType();
        currentNetTypeText.setText(getString(R.string.net_check_net_current_type) + networksType);   //net_check_net_current_type
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


}
