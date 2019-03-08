package com.inspur.emmcloud.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.keyboardview.EmmSecurityKeyboard;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by chenmch on 2019/1/19.
 */

@ContentView(R.layout.activity_password_reset)
public class PasswordResetActivity extends BaseActivity implements View.OnTouchListener {
    public static final String EXTRA_CAPTCHA = "extra_captcha";
    @ViewInject(R.id.bt_ok)
    private Button okBtn;
    @ViewInject(R.id.et_password_new)
    private EditText passwordNewEdit;
    @ViewInject(R.id.et_password_confirm)
    private EditText passwordConfirmEdit;
    private String passwordNew;
    private String passwordConfirm;
    private LoadingDialog loadingDlg;
    private EmmSecurityKeyboard emmSecurityKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        ImmersionBar.with(this).statusBarColor(android.R.color.white).statusBarDarkFont(true,0.2f).init();
        EditWatcher editWatcher = new EditWatcher();
        passwordNewEdit.addTextChangedListener(editWatcher);
        passwordConfirmEdit.addTextChangedListener(editWatcher);
        emmSecurityKeyboard = new EmmSecurityKeyboard(this);
        EditOnTouchListener editOnTouchListener = new EditOnTouchListener();
        passwordNewEdit.setOnTouchListener(editOnTouchListener);
        passwordConfirmEdit.setOnTouchListener(editOnTouchListener);
        loadingDlg = new LoadingDialog(this);
        emmSecurityKeyboard = new EmmSecurityKeyboard(this);
        passwordNewEdit.setOnTouchListener(this);
        passwordConfirmEdit.setOnTouchListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_save:
                if (!passwordNew.equals(passwordConfirm)) {
                    ToastUtils.show(PasswordResetActivity.this, R.string.modify_not_same);
                    return;
                }
                if (passwordNew.length() < 8 || passwordNew.length() > 128 || !FomatUtils.isPasswrodStrong(passwordNew)) {
                    ToastUtils.show(MyApplication.getInstance(), R.string.modify_password_invalid);
                    return;
                }
                resetPassword();
                break;
            case R.id.ibt_back:
                finish();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.et_password_new:
                emmSecurityKeyboard.showSecurityKeyBoard(passwordNewEdit);
                break;
            case R.id.et_password_confirm:
                emmSecurityKeyboard.showSecurityKeyBoard(passwordConfirmEdit);
                break;
        }
        return false;
    }

    private void resetPassword() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            String captcha = getIntent().getStringExtra(EXTRA_CAPTCHA);
            LoginAPIService apiService = new LoginAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.resetPassword(captcha, passwordNew);
        }
    }

    class EditOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (view.getId()) {
                case R.id.et_password_new:
                    emmSecurityKeyboard.showSecurityKeyBoard(passwordNewEdit);
                    break;
                case R.id.et_password_confirm:
                    emmSecurityKeyboard.showSecurityKeyBoard(passwordConfirmEdit);
                    break;
            }
            return false;
        }
    }

    private class EditWatcher implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            passwordNew = passwordNewEdit.getText().toString();
            passwordConfirm = passwordConfirmEdit.getText().toString();
            boolean isInputValaid = passwordNew.length() > 7 && passwordConfirm.length() > 7;
            okBtn.setEnabled(isInputValaid);
            okBtn.setBackgroundResource(isInputValaid ? R.drawable.selector_login_btn : R.drawable.bg_login_btn_unable);
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnResetPasswordSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            MyApplication.getInstance().signout();
        }

        @Override
        public void returnResetPasswordFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(PasswordResetActivity.this, error, errorCode);
        }
    }
}
