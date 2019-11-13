package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.bean.chat.Conversation;
import com.inspur.emmcloud.util.privates.cache.ConversationCacheUtils;

/**
 * Created by libaochao on 2019/11/11.
 */

public class FileTransferDetailActivity extends BaseActivity {
    private static final int QEQUEST_FILE_TRANSFER = 4;
    String cid = "";
    @Override
    public void onCreate() {
        if (getIntent().hasExtra(ConversationCastInfoActivity.EXTRA_CID)) {
            cid = getIntent().getStringExtra(ConversationCastInfoActivity.EXTRA_CID);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.chat_file_transfer_detail_activity;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.bt_send_message:
                if (!StringUtils.isBlank(cid)) {
                    Conversation conversation = ConversationCacheUtils.getConversation(getApplicationContext(), cid);
                    Bundle bundle = new Bundle();
                    String conversationName = conversation.getType().equals(Conversation.TYPE_TRANSFER) ?
                            getString(R.string.chat_file_transfer) : conversation.getName();
                    conversation.setName(conversationName);
                    bundle.putSerializable(ConversationActivity.EXTRA_CONVERSATION, conversation);
                    IntentUtils.startActivity(this, ConversationActivity.class, bundle);
                    setResult(QEQUEST_FILE_TRANSFER, null);
                    finish();
                } else {
                    finish();
                }
                break;
        }
    }
}
