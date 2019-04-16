package com.inspur.emmcloud.ui.appcenter.mail;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MailApiService;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.EditTextUtils;
import com.inspur.emmcloud.util.common.EncryptUtils;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.util.privates.cache.ContactUserCacheUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by chenmch on 2018/12/28.
 */
@ContentView(R.layout.activity_mail_login)
public class MailLoginActivity extends BaseActivity {

    @ViewInject(R.id.et_mail)
    private ClearEditText mailEdit;
    @ViewInject(R.id.et_password)
    private EditText passwordEdit;
    @ViewInject(R.id.bt_login)
    private Button loginBtn;
    @ViewInject(R.id.text_input_layout_username)
    private TextInputLayout usernameTextInputLayout;
    private LoadingDialog loadingDlg;
    private MailApiService apiService;
    private String mail = "";
    private String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this).statusBarColor(android.R.color.white).statusBarDarkFont(true, 0.2f).init();
        loadingDlg = new LoadingDialog(this);
        apiService = new MailApiService(this);
        apiService.setAPIInterface(new WebServie());
        TextWatcher watcher = new TextWatcher();
        mail = PreferencesByUsersUtils.getString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, "");
        if (StringUtils.isBlank(mail)) {
            mail = ContactUserCacheUtils.getUserMail(MyApplication.getInstance().getUid());
        }
        EditTextUtils.setText(mailEdit, mail);
        mailEdit.addTextChangedListener(watcher);
        passwordEdit.addTextChangedListener(watcher);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
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

    private void login(String mail, String password) {
        if (NetUtils.isNetworkConnected(this)) {
            String key = EncryptUtils.stringToMD5(mail);
            try {
                password = EncryptUtils.encode(password, key, Constant.MAIL_ENCRYPT_IV, Base64.NO_WRAP);
            } catch (Exception e) {
                e.printStackTrace();
            }
            loadingDlg.show();
            apiService.loginMail(mail, password);

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
                usernameTextInputLayout.setError(getString(R.string.webex_input_correct_invitee_emails));
            } else {
                usernameTextInputLayout.setError("");
            }
            boolean isInputValaid = password.length() >= 6 && !StringUtils.isBlank(mail) && FomatUtils.isValiadEmail(mail);
            loginBtn.setEnabled(isInputValaid);
            loginBtn.setBackgroundResource(isInputValaid ? R.drawable.selector_login_btn : R.drawable.bg_login_btn_unable);
        }
    }

    private class WebServie extends APIInterfaceInstance {
        @Override
        public void returnMailLoginSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            PreferencesByUsersUtils.putString(MyApplication.getInstance(), Constant.PREF_MAIL_ACCOUNT, mail);
            PreferencesByUsersUtils.putString(MyApplication.getInstance(), Constant.PREF_MAIL_PASSWORD, password);
            IntentUtils.startActivity(MailLoginActivity.this, MailHomeActivity.class, true);
        }

        @Override
        public void returnMailLoginFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MailLoginActivity.this, error, errorCode);
        }
    }
}
