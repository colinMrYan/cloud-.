package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.MediaPlayBaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.widget.ConversationWatermarkView;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.bean.SimpleEventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppConfigCacheUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2018/10/8.
 */

public class ConversationBaseActivity extends MediaPlayBaseActivity {
    public static final String EXTRA_CID = "cid";
    public static final String EXTRA_CONVERSATION = "conversation";
    public static final String EXTRA_NEED_GET_NEW_MESSAGE = "get_new_msg";
    public static final String EXTRA_COME_FROM_SCANCODE = "com_from_scan_code";
    public static final String EXTRA_UNREAD_MESSAGE = "unread_count";
    public static final String EXTRA_POSITION_MESSAGE = "position_message";
    public static final String EXTRA_FROM_SEARCH = "from_search";

    protected String cid;
    protected LoadingDialog loadingDlg;
    protected Conversation conversation;
    @BindView(R.id.iv_config)
    View configView;

    @BindView(R.id.conversation_watermark)
    ConversationWatermarkView watermarkView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.bind(this);
        loadingDlg = new LoadingDialog(this);
        EventBus.getDefault().register(this);
        initConversationInfo();
        recordUserClickChannel();
        setConversationUnHide();
        configView.setVisibility(View.VISIBLE);
        showWaterMark();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_channel;
    }
    protected void initConversationInfo() {
        if (getIntent().hasExtra(EXTRA_CONVERSATION)) {
            conversation = (Conversation) getIntent().getExtras().getSerializable(EXTRA_CONVERSATION);
            cid = conversation.getId();
        } else {
            cid = getIntent().getExtras().getString(EXTRA_CID);
            conversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), cid);
        }
        MyApplication.getInstance().setCurrentChannelCid(cid);
        if (conversation == null) {
            getConversationInfo();
        } else {
            initChannelMessage();
        }
    }

    protected void initChannelMessage() {

    }

    private void showWaterMark() {
        boolean showWaterMark = AppConfigCacheUtils.getAppConfigValue(this, "EnableChatWatermark", "false").equals("true");
        if(!showWaterMark){
            return;
        }
        watermarkView.setVisibility(View.VISIBLE);
        String myInfo = PreferencesUtils.getString(this, "myInfo");
        GetMyInfoResult getMyInfoResult = new GetMyInfoResult(myInfo);
        watermarkView.setText(getMyInfoResult.getName());
    }

    /**
     * 当进入这个聊天时将取消这个聊天的隐藏状态
     */
    private void setConversationUnHide() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConversationCacheUtils.updateConversationHide(MyApplication.getInstance(), cid, false);
            }
        }).start();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recordUserClickChannel();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * 记录用户点击的频道，修改不是云+客服的时候才记录频道点击事件170629
     */
    private void recordUserClickChannel() {
        if (getIntent() != null && getIntent().hasExtra("from")) {
            String from = getIntent().getExtras().getString("from", "");
            if (!from.equals("customer")) {
                PVCollectModelCacheUtils.saveCollectModel("channel", "communicate");
            }
        }
    }

    private void getConversationInfo() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            ChatAPIService apiService = new ChatAPIService(this);
            apiService.setAPIInterface(new Webservice());
            apiService.getConversationInfo(cid);
        } else {
            finish();
        }

    }


    private class Webservice extends APIInterfaceInstance {
        @Override
        public void returnConversationInfoSuccess(Conversation conversation) {
            LoadingDialog.dimissDlg(loadingDlg);
            ConversationBaseActivity.this.conversation = conversation;
            ConversationCacheUtils.saveConversation(BaseApplication.getInstance(), conversation);
            EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG_CHAT_CHANGE, conversation));
            initChannelMessage();
        }

        @Override
        public void returnConversationInfoFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
//            WebServiceMiddleUtils.hand(BaseApplication.getInstance(), error, errorCode);
            if (errorCode == 400) {
                ToastUtils.show(R.string.chat_group_chanel_not_exist);
            }
            finish();
        }

    }
}
