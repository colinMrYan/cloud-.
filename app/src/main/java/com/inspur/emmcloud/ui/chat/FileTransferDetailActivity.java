package com.inspur.emmcloud.ui.chat;

import android.os.Bundle;
import android.view.View;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

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
        }
    }
}
