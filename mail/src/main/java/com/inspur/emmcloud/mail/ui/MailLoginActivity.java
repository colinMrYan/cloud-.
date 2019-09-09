package com.inspur.emmcloud.mail.ui;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.baselib.util.EditTextUtils;
import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.mail.OnExchangeLoginListener;
import com.inspur.emmcloud.mail.R;
import com.inspur.emmcloud.mail.R2;
import com.inspur.emmcloud.mail.util.ExchangeLoginUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2018/12/28.
 */

@Route(path = Constant.AROUTER_CLASS_MAIL_LOGIN)
public class MailLoginActivity extends BaseActivity {

    @BindView(R2.id.et_mail)
    ClearEditText mailEdit;
    @BindView(R2.id.et_password)
    EditText passwordEdit;
    @BindView(R2.id.bt_login)
    Button loginBtn;
    @BindView(R2.id.text_input_layout_username)
    TextInputLayout usernameTextInputLayout;
    private String mail = "";
    private String password = "";

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        TextWatcher watcher = new TextWatcher();
        mail = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
        password = PreferencesByUserAndTanentUtils.getString(BaseApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, "");
        EditTextUtils.setText(mailEdit, mail);
        if (!StringUtils.isBlank(mail)) {
            mailEdit.setEnabled(false);
        }
        mailEdit.addTextChangedListener(watcher);
        passwordEdit.addTextChangedListener(watcher);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_mail_login;
    }

    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bt_login) {
            mail = mailEdit.getText().toString();
            password = passwordEdit.getText().toString();
            login(mail, password);

        } else if (i == R.id.ibt_back) {
            finish();

        }
    }

    private void login(final String mail, final String password) {
        if (NetUtils.isNetworkConnected(this)) {
            new ExchangeLoginUtils.Builder(this)
                    .setShowLoadingDlg(true)
                    .setExchangeLoginAccount(mail, password)
                    .setOnExchangeLoginListener(new OnExchangeLoginListener() {
                        @Override
                        public void onMailLoginSuccess() {
                            LogUtils.LbcDebug("onMailLoginSuccess");
                            if (getIntent().hasExtra("from") && getIntent().getExtras().getString("from").equals("schedule_exchange_login")) {
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                LogUtils.LbcDebug("onMailLoginSuccess");
                                IntentUtils.startActivity(MailLoginActivity.this, MailHomeActivity.class, true);
                            }
                        }

                        @Override
                        public void onMailLoginFail(String error, int errorCode) {
                            WebServiceMiddleUtils.hand(MailLoginActivity.this, error, errorCode);
                        }
                    }).build().login();
        }
    }

    private class TextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String mail = mailEdit.getText().toString();
            String password = passwordEdit.getText().toString();
            if (!StringUtils.isBlank(mail) && !FomatUtils.isValiadEmail(mail)) {
                usernameTextInputLayout.setError(getString(R.string.mail_string_input_correct_invitee_emails));
            } else {
                usernameTextInputLayout.setError("");
            }
            boolean isInputValid = password.length() >= 6 && !StringUtils.isBlank(mail) && FomatUtils.isValiadEmail(mail);
            loginBtn.setEnabled(isInputValid);
            loginBtn.setBackgroundResource(isInputValid ? R.drawable.selector_login_btn : R.drawable.bg_login_btn_unable);
        }
    }

}
