package com.inspur.emmcloud.ui.contact;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ContactAPIService;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.util.privates.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ChannelOperationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.SwitchView.OnStateChangedListener;

/**
 * classes : com.inspur.emmcloud.ui.contact.RobotActivity
 * Create at 2016年12月1日 下午5:32:01
 */
public class RobotInfoActivity extends BaseActivity implements OnStateChangedListener {

    private String id = "";
    private ContactAPIService apiService;
    private LoadingDialog loadingDialog;
    private CircleTextImageView robotHeadImg;
    private TextView robotNameText, functionIntroductionText, supportText;
    private SwitchView setTopSwitch;
    private String cid;

    @Override
    public void onCreate() {
        setContentView(R.layout.activity_conversation_cast_info);
        initViews();
        getRobotInfo();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_cast_info;
    }

    /**
     * 初始化Views
     */
    private void initViews() {
        apiService = new ContactAPIService(RobotInfoActivity.this);
        apiService.setAPIInterface(new WebService());
        loadingDialog = new LoadingDialog(RobotInfoActivity.this);
        robotHeadImg = (CircleTextImageView) findViewById(R.id.img_photo);
        robotNameText = (TextView) findViewById(R.id.tv_name);
        supportText = (TextView) findViewById(R.id.support_text);
        functionIntroductionText = (TextView) findViewById(R.id.function_introduction_text);
        id = getIntent().getStringExtra("uid");
        cid = getIntent().getStringExtra("cid");
        setTopSwitch = (SwitchView) findViewById(R.id.sv_stick);
        boolean isSetTop = ChannelOperationCacheUtils.isChannelSetTop(
                this, cid);
        setTopSwitch.setOpened(isSetTop);
        setTopSwitch.setOnStateChangedListener(this);
        if (!getIntent().hasExtra("type")) {
            findViewById(R.id.support_top_layout).setVisibility(View.GONE);
        } else {
            //预留，以后类型增加的时候在这里编写UI逻辑，入如显示需要展示哪些信息等
            if (!getIntent().getStringExtra("type").equals("SERVICE")) {
                findViewById(R.id.support_top_layout).setVisibility(View.GONE);
            }
        }

    }

    /**
     * 获取机器人信息,先从本地读取，如果本地没有则从网络获取
     */
    private void getRobotInfo() {
        Robot robotInfo = RobotCacheUtils.getRobotById(RobotInfoActivity.this, id);
        if (robotInfo == null) {
            getRobotInfoFromNet();
        } else {
            showRobotInfo(robotInfo);
        }
    }

    /**
     * 本地没有存储机器人信息就从网络获取
     */
    private void getRobotInfoFromNet() {
        if (NetUtils.isNetworkConnected(RobotInfoActivity.this)) {
            loadingDialog.show();
            apiService.getRobotInfoById(id);
        }
    }

    /**
     * 展示机器人信息
     */
    private void showRobotInfo(Robot robotInfo) {
        ImageDisplayUtils.getInstance().displayImage(robotHeadImg, APIUri.getRobotIconUrl(robotInfo.getAvatar()), R.drawable.icon_person_default);
        robotNameText.setText(robotInfo.getName());
        functionIntroductionText.setText(robotInfo.getTitle());
        supportText.setText(robotInfo.getSupport());
    }

    /**
     * 是否将频道置顶
     *
     * @param isSetIop
     */
    private void setChannelTop(boolean isSetIop) {
        setTopSwitch.toggleSwitch(isSetIop);
        ChannelOperationCacheUtils.setChannelTop(RobotInfoActivity.this, cid,
                isSetIop);
        // 通知消息页面重新创建群组头像
        Intent intent = new Intent("message_notify");
        intent.putExtra("command", "sort_session_list");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void toggleToOn(View view) {
        // TODO Auto-generated method stub
        setChannelTop(true);
    }

    @Override
    public void toggleToOff(View view) {
        // TODO Auto-generated method stub
        setChannelTop(false);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            default:
                break;
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnRobotByIdSuccess(Robot robot) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            showRobotInfo(robot);
        }

        @Override
        public void returnRobotByIdFail(String error, int errorCode) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(RobotInfoActivity.this, error, errorCode);
        }
    }


}
 