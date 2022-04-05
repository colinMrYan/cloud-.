package com.inspur.emmcloud.ui.chat;

import android.view.View;
import android.widget.TextView;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;

public class ConversationBurnContentActivity extends BaseActivity {

    @Override
    public void onCreate() {
        TextView view = findViewById(R.id.content);
        view.setText(getIntent().getStringExtra("content"));
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_burn_content;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
        }
    }

}
