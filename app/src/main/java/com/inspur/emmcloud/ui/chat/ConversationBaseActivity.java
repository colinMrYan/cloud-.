package com.inspur.emmcloud.ui.chat;

import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.MediaPlayBaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.bean.system.PVCollectModel;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;
import com.inspur.emmcloud.util.privates.cache.PVCollectModelCacheUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

/**
 * Created by chenmch on 2018/10/8.
 */

public class ConversationBaseActivity extends MediaPlayBaseActivity{
    public static final String EXTRA_CID= "cid";
    public static final String EXTRA_CONVERSATION= "conversation";
    public static final String EXTRA_NEED_GET_NEW_MESSAGE = "get_new_msg";
    protected String cid;
    protected LoadingDialog loadingDlg;
    protected Conversation conversation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordUserClickChannel();
        initAudioConverter();
        loadingDlg = new LoadingDialog(this);
        if (getIntent().hasExtra(EXTRA_CONVERSATION)){
            conversation = (Conversation) getIntent().getExtras().getSerializable(EXTRA_CONVERSATION);
            cid = conversation.getId();
        }else {
            cid = getIntent().getExtras().getString(EXTRA_CID);
            conversation = ConversationCacheUtils.getConversation(MyApplication.getInstance(),cid);
        }
    }

    private void init() {

//        new ChannelInfoUtils().getChannelInfo(this, cid, loadingDlg, new ChannelInfoUtils.GetChannelInfoCallBack() {
//            @Override
//            public void getChannelInfoSuccess(Channel channel) {
//                ConversationActivity.this.channel = channel;
//                isSpecialUser = channel.getType().equals("SERVICE") && channel.getTitle().contains(robotUid);
//                if (getIntent().hasExtra("get_new_msg") && NetUtils.isNetworkConnected(getApplicationContext(), false)) {//通过scheme打开的频道
//                    getNewMsgOfChannel();
//                } else {
//                    initViews();
//                }
//            }
//
//            @Override
//            public void getChannelInfoFail(String error, int errorCode) {
//                finishActivity();
//            }
//        });
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
     * 加载语音转换库
     */
    private void initAudioConverter() {
        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onFailure(Exception error) {
            }
        });
    }

    /**
     * 记录用户点击的频道，修改不是云+客服的时候才记录频道点击事件170629
     */
    private void recordUserClickChannel() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String from = getIntent().getExtras().getString("from", "");
                if (!from.equals("customer")) {
                    PVCollectModel pvCollectModel = new PVCollectModel("channel", "communicate");
                    PVCollectModelCacheUtils.saveCollectModel(MyApplication.getInstance(), pvCollectModel);
                }
            }
        }).start();
    }
}
