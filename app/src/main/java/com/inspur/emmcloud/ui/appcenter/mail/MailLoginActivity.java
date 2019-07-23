package com.inspur.emmcloud.ui.appcenter.mail;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.EditTextUtils;
import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.componentservice.mail.OnExchangeLoginListener;
import com.inspur.emmcloud.util.privates.ExchangeLoginUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2018/12/28.
 */

@Route(path = Constant.AROUTER_CLASS_MAIL_LOGIN)
public class MailLoginActivity extends BaseActivity {

    @BindView(R.id.et_mail)
    ClearEditText mailEdit;
    @BindView(R.id.et_password)
    EditText passwordEdit;
    @BindView(R.id.bt_login)
    Button loginBtn;
    @BindView(R.id.text_input_layout_username)
    TextInputLayout usernameTextInputLayout;
    private String mail = "";
    private String password = "";

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        TextWatcher watcher = new TextWatcher();
        mail = PreferencesByUserAndTanentUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
        if (StringUtils.isBlank(mail)) {
            mail = ContactUserCacheUtils.getUserMail(MyApplication.getInstance().getUid());
        }
        EditTextUtils.setText(mailEdit, mail);
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
        switch (v.getId()) {
            case R.id.bt_login:
                mail = mailEdit.getText().toString();
                password = passwordEdit.getText().toString();
                login(mail, password);
                break;
            case R.id.ibt_back:
                finish();
                break;
        }
    }

    private void login(final String mail, final String password) {
        if (NetUtils.isNetworkConnected(this)) {
            new ExchangeLoginUtils.Builder(this)
                    .setShowLoadingDlg(true)
                    .setExchangeLoginAccount(mail, password)
                    .setOnExchageLoginListener(new OnExchangeLoginListener() {
                        @Override
                        public void onMailLoginSuccess() {
                            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, mail);
                            PreferencesByUserAndTanentUtils.putString(MyApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, password);
                            if (getIntent().hasExtra("from") && getIntent().getExtras().getString("from").equals("schedule_exchange_login")) {
                                setResult(RESULT_OK);
                                finish();
                            } else {
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
                usernameTextInputLayout.setError(getString(R.string.input_correct_emails));
            } else {
                usernameTextInputLayout.setError("");
            }
            boolean isInputValid = password.length() >= 6 && !StringUtils.isBlank(mail) && FomatUtils.isValiadEmail(mail);
            loginBtn.setEnabled(isInputValid);
            loginBtn.setBackgroundResource(isInputValid ? R.drawable.selector_login_btn : R.drawable.bg_login_btn_unable);
        }
    }

}
