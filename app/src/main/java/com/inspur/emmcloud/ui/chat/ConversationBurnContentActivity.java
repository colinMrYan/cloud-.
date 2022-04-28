package com.inspur.emmcloud.ui.chat;

import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.mobile.auth.core.signin.ui.DisplayUtils;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.ui.chat.emotion.EmotionUtil;

public class ConversationBurnContentActivity extends BaseActivity {

    @Override
    public void onCreate() {
        TextView view = findViewById(R.id.content);
        SpannableString spannableString = new SpannableString(getIntent().getStringExtra("content"));
        view.setText(EmotionUtil.getInstance(this).getSmiledText(spannableString, DisplayUtils.dp(20)));
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
