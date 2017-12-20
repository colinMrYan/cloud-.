package com.inspur.emmcloud.ui.mine.feedback;

import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.ToastUtils;

public class FeedBackActivity extends BaseActivity {

    private EditText contentEdit;
    private CheckBox anonymouscheck;
    private TextView textCountText;
    private EditText contactEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        contentEdit = (EditText) findViewById(R.id.feedback_edit);
        anonymouscheck = (CheckBox) findViewById(R.id.checkbox);
        textCountText = (TextView) findViewById(R.id.text_count_text);
        contentEdit.addTextChangedListener(mTextWatcher);
        contactEdit = (EditText) findViewById(R.id.contact_edit);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.submit_bt:
                String content = contentEdit.getText().toString();
                if (TextUtils.isEmpty(content.trim())) {
                    ToastUtils.show(getApplicationContext(), getString(R.string.feed_back_no_empty));
                } else if (NetUtils.isNetworkConnected(getApplicationContext())) {
                    uploadFeedback();
                }
                break;
            case R.id.check_layout:
                anonymouscheck.setChecked(!anonymouscheck.isChecked());
                break;

            default:
                break;
        }
    }

    private void uploadFeedback() {
        String content = contentEdit.getText().toString();
        String contact = contactEdit.getText().toString();
        String userName = "";
        if (!anonymouscheck.isChecked()) {
            userName = PreferencesUtils.getString(FeedBackActivity.this,
                    "userRealName", "");
        } else {
            userName = "";
        }
        MineAPIService apiService = new MineAPIService(FeedBackActivity.this);
        apiService.uploadFeedback(content, contact, userName);
        ToastUtils.show(getApplicationContext(), getString(R.string.feed_back_success));
        contentEdit.setText("");
        contactEdit.setText("");
        textCountText.setText("(0/200)");
        anonymouscheck.setChecked(false);
    }

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
            Editable editable = contentEdit.getText();
            int len = editable.length();

            if (len > 200) {
                Toast.makeText(FeedBackActivity.this, getString(R.string.feed_back_out_of_length),
                        Toast.LENGTH_SHORT).show();
                int selEndIndex = Selection.getSelectionEnd(editable);
                String str = editable.toString();
                //截取新字符串
                String newStr = str.substring(0, 200);
                contentEdit.setText(newStr);
                editable = contentEdit.getText();

                //新字符串的长度
                int newLen = editable.length();
                //旧光标位置超过字符串长度
                if (selEndIndex > newLen) {
                    selEndIndex = editable.length();
                }
                //设置新光标所在的位置
                Selection.setSelection(editable, selEndIndex);

            }
            textCountText.setText("(" + contentEdit.getText().length() + "/200)");
        }

        @Override
        public void afterTextChanged(Editable s) {
//			editStart = contentEdit.getSelectionStart();
//			editEnd = contentEdit.getSelectionEnd();
//			textCountText.setText("(" + temp.length() + "/200)");
//			if (temp.length() > 200) {
//				Toast.makeText(FeedBackActivity.this, "你输入的字数已经超过了限制！",
//						Toast.LENGTH_SHORT).show();
//				s.delete(editStart-1, editEnd);
//				int tempSelection = s.length();
//				contentEdit.setText(s);
//				contentEdit.setSelection(tempSelection);
//			}
        }
    };

}
