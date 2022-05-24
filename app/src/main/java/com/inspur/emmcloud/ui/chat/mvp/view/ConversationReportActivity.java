package com.inspur.emmcloud.ui.chat.mvp.view;

import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.ChatAPIService;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConversationReportActivity extends BaseActivity {
    @BindView(R.id.et_feedback_content)
    EditText feedbackContentEdit;
    @BindView(R.id.et_feedback_contact)
    EditText feedbackContactEdit;
    @BindView(R.id.switch_compat_anonymous)
    SwitchCompat anonymousSwitch;

    TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart;
        private int editEnd;

        @Override
        public void beforeTextChanged(CharSequence s, int arg1, int arg2,
                                      int arg3) {
            temp = s;
        }

        @Override
        public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            Editable editable = feedbackContentEdit.getText();
            int len = editable.length();

            if (len > 200) {
                ToastUtils.show(ConversationReportActivity.this, getString(R.string.report_out_of_length));
                int selEndIndex = Selection.getSelectionEnd(editable);
                String str = editable.toString();
                //截取新字符串
                String newStr = str.substring(0, 200);
                feedbackContentEdit.setText(newStr);
                editable = feedbackContentEdit.getText();

                //新字符串的长度
                int newLen = editable.length();
                //旧光标位置超过字符串长度
                if (selEndIndex > newLen) {
                    selEndIndex = editable.length();
                }
                //设置新光标所在的位置
                Selection.setSelection(editable, selEndIndex);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        feedbackContentEdit.addTextChangedListener(mTextWatcher);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.conversation_report_activity;
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.bt_submit_feedback) {
            String content = feedbackContentEdit.getText().toString();
            if (TextUtils.isEmpty(content.trim())) {
                ToastUtils.show(getApplicationContext(), getString(R.string.report_no_empty));
            } else if (NetUtils.isNetworkConnected(getApplicationContext())) {
                uploadFeedback();
            }
        }
    }

    private void uploadFeedback() {
        String content = feedbackContentEdit.getText().toString();
        String contact = feedbackContactEdit.getText().toString();
        String userName = "";
        if (!anonymousSwitch.isChecked()) {
            userName = PreferencesUtils.getString(ConversationReportActivity.this,
                    "userRealName", "");
        } else {
            userName = "";
        }
        ChatAPIService apiService = new ChatAPIService(ConversationReportActivity.this);
        apiService.uploadFeedback(content, contact, userName);
        ToastUtils.show(getApplicationContext(), getString(R.string.feed_back_success));
        feedbackContentEdit.setText("");
        feedbackContactEdit.setText("");
        anonymousSwitch.setChecked(false);
    }

}
