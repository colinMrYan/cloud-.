package com.inspur.emmcloud.ui.appcenter.mail;

import android.os.Bundle;
import android.text.Editable;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MailApiService;
import com.inspur.emmcloud.util.common.EncryptUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by chenmch on 2018/12/28.
 */
@ContentView(R.layout.activity_mail_login)
public class MailLoginActivity extends BaseActivity {

    @ViewInject(R.id.et_username)
    private ClearEditText usernameEdit;
    @ViewInject(R.id.et_password)
    private ClearEditText passwordEdit;
    @ViewInject(R.id.bt_login)
    private QMUIRoundButton loginBtn;
    private LoadingDialog loadingDlg;
    private MailApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        loadingDlg = new LoadingDialog(this);
        apiService = new MailApiService(this);
        apiService.setAPIInterface(new WebServie());
        TextWatcher watcher = new TextWatcher();
        usernameEdit.addTextChangedListener(watcher);
        passwordEdit.addTextChangedListener(watcher);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login:
                String username = usernameEdit.getText().toString();
                String password= passwordEdit.getText().toString();
                login(username, password);
                break;
        }
    }


    private void login(String username, String password) {
        if (NetUtils.isNetworkConnected(this)) {
            String key = EncryptUtils.stringToMD5(username);
            String iv = "inspurcloud+2019";
            try {
                password = EncryptUtils.encode(password, key, iv, Base64.NO_WRAP);
            } catch (Exception e) {
                e.printStackTrace();
            }

            loadingDlg.show();
            apiService.loginMail(username, password);

        }
    }

    private class TextWatcher implements android.text.TextWatcher{
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String username = usernameEdit.getText().toString();
            String password= passwordEdit.getText().toString();
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password) || password.length()<6){
                loginBtn.setEnabled(false);
            }else {
                loginBtn.setEnabled(true);
            }
        }
    }

    private class WebServie extends APIInterfaceInstance {
        @Override
        public void returnMailLoginSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
        }

        @Override
        public void returnMailLoginFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(MailLoginActivity.this, error, errorCode);
        }
    }
}
