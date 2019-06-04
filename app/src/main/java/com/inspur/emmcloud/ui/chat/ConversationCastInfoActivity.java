package com.inspur.emmcloud.ui.chat;

import android.view.View;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.api.apiservice.ContactAPIService;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.chat.Robot;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.util.privates.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.RobotCacheUtils;
import com.inspur.emmcloud.widget.CircleTextImageView;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.SwitchView;
import com.inspur.emmcloud.widget.SwitchView.OnStateChangedListener;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConversationCastInfoActivity extends BaseActivity implements OnStateChangedListener {

    public static final String EXTRA_CID = "cid";
    @BindView(R.id.img_photo)
    CircleTextImageView robotIconImg;
    @BindView(R.id.tv_name)
    TextView robotNameText;
    @BindView(R.id.function_introduction_text)
    TextView introductionText;
    @BindView(R.id.support_text)
    TextView supportText;
    @BindView(R.id.sv_stick)
    SwitchView stickSwitch;
    private Conversation conversation;
    private ChatAPIService apiService;
    private LoadingDialog loadingDlg;
    private WebService webService;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(ConversationCastInfoActivity.this);
        String cid = getIntent().getExtras().getString(EXTRA_CID);
        conversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), cid);
        stickSwitch.setOpened(conversation.isStick());
        stickSwitch.setOnStateChangedListener(this);
        webService = new WebService();
        apiService = new ChatAPIService(this);
        apiService.setAPIInterface(webService);
        Robot robot = RobotCacheUtils.getRobotById(ConversationCastInfoActivity.this, conversation.getName());
        if (robot == null) {
            getRobotInfo();
        } else {
            showRobotInfo(robot);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_conversation_cast_info;
    }

    /**
     * 展示机器人信息
     */
    private void showRobotInfo(Robot robotInfo) {
        ImageDisplayUtils.getInstance().displayImage(robotIconImg, APIUri.getRobotIconUrl(robotInfo.getAvatar()), R.drawable.icon_person_default);
        robotNameText.setText(robotInfo.getName());
        introductionText.setText(robotInfo.getTitle());
        supportText.setText(robotInfo.getSupport());
    }


    @Override
    public void toggleToOn(View view) {
        // TODO Auto-generated method stub
        setConversationStick();
    }

    @Override
    public void toggleToOff(View view) {
        // TODO Auto-generated method stub
        setConversationStick();
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


    /**
     * 获取机器人信息
     */
    private void getRobotInfo() {
        if (NetUtils.isNetworkConnected(ConversationCastInfoActivity.this)) {
            loadingDlg.show();
            ContactAPIService apiService = new ContactAPIService(ConversationCastInfoActivity.this);
            apiService.setAPIInterface(webService);
            apiService.getRobotInfoById(conversation.getName());
        }
    }

    /**
     * 设置频道是否置顶
     */
    private void setConversationStick() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            apiService.setConversationStick(conversation.getId(), !conversation.isStick());
        } else {
            stickSwitch.setOpened(conversation.isStick());
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnRobotByIdSuccess(Robot robot) {
            LoadingDialog.dimissDlg(loadingDlg);
            RobotCacheUtils.saveRobot(MyApplication.getInstance(), robot);
            showRobotInfo(robot);
        }

        @Override
        public void returnRobotByIdFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(ConversationCastInfoActivity.this, error, errorCode);
        }

        @Override
        public void returnSetConversationStickSuccess(String id, boolean isStick) {
            LoadingDialog.dimissDlg(loadingDlg);
            conversation.setStick(isStick);
            stickSwitch.setOpened(isStick);
            ConversationCacheUtils.setConversationStick(MyApplication.getInstance(), id, isStick);
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_UPDATE_CHANNEL_FOCUS, conversation));
        }

        @Override
        public void returnSetConversationStickFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
            stickSwitch.setOpened(conversation.isStick());
        }

    }


}
 