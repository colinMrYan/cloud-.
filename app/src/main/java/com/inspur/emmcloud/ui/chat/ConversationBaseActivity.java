package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.MediaPlayBaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

/**
 * Created by chenmch on 2018/10/8.
 */

public class ConversationBaseActivity extends MediaPlayBaseActivity {
    public static final String EXTRA_CID = "cid";
    public static final String EXTRA_CONVERSATION = "conversation";
    public static final String EXTRA_NEED_GET_NEW_MESSAGE = "get_new_msg";
    public static final String EXTRA_UNREAD_MESSAGE = "unread_count";
    public static final String EXTRA_UIMESSAGE = "uimessage";

    protected String cid;
    protected LoadingDialog loadingDlg;
    protected Conversation conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDlg = new LoadingDialog(this);
        initConversationInfo();
        recordUserClickChannel();
        setConversationUnHide();
    }

    protected void initConversationInfo() {
        if (getIntent().hasExtra(EXTRA_CONVERSATION)) {
            conversation = (Conversation) getIntent().getExtras().getSerializable(EXTRA_CONVERSATION);
            cid = conversation.getId();
        } else {
            cid = getIntent().getExtras().getString(EXTRA_CID);
            conversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(), cid);
        }
        if (conversation == null) {
            getConversationInfo();
        } else {
            initChannelMessage();
        }

    }

    protected void initChannelMessage() {

    }

    /**
     * 当进入这个聊天时将取消这个聊天的隐藏状态
     */
    private void setConversationUnHide(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConversationCacheUtils.updateConversationHide(MyApplication.getInstance(),cid,false);
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
        MyApplication.getInstance().setCurrentChannelCid(cid);
    }

    /**
     * 记录用户点击的频道，修改不是云+客服的时候才记录频道点击事件170629
     */
    private void recordUserClickChannel() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(getIntent() != null && getIntent().hasExtra("from")){
                    String from = getIntent().getExtras().getString("from", "");
                    if (!from.equals("customer")) {
                        PVCollectModel pvCollectModel = new PVCollectModel("channel", "communicate");
                        PVCollectModelCacheUtils.saveCollectModel(MyApplication.getInstance(), pvCollectModel);
                    }
                }
            }
        }).start();
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
            initChannelMessage();
        }

        @Override
        public void returnConversationInfoFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MyApplication.getInstance(), error, errorCode);
            finish();
        }

    }
}
