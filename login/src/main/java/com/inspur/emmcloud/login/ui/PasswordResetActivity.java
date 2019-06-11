package com.inspur.emmcloud.login.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.login.R;
import com.inspur.emmcloud.login.R2;
import com.inspur.emmcloud.login.api.LoginAPIInterfaceImpl;
import com.inspur.emmcloud.login.api.LoginAPIService;
import com.inspur.emmcloud.login.widget.keyboardview.EmmSecurityKeyboard;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2019/1/19.
 */

public class PasswordResetActivity extends BaseActivity implements View.OnTouchListener {
    public static final String EXTRA_CAPTCHA = "extra_captcha";
    @BindView(R2.id.bt_ok)
    Button okBtn;
    @BindView(R2.id.et_password_new)
    EditText passwordNewEdit;
    @BindView(R2.id.et_password_confirm)
    EditText passwordConfirmEdit;
    private String passwordNew;
    private String passwordConfirm;
    private LoadingDialog loadingDlg;
    private EmmSecurityKeyboard emmSecurityKeyboard;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
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

    @Override
    public int getLayoutResId() {
        return R.layout.activity_password_reset;
    }

    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }


    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bt_ok) {
            if (!passwordNew.equals(passwordConfirm)) {
                ToastUtils.show(PasswordResetActivity.this, R.string.modify_not_same);
                return;
            }
            if (passwordNew.length() < 6 || passwordNew.length() > 16 || !FomatUtils.isPasswrodStrong(passwordNew)) {
                ToastUtils.show(BaseApplication.getInstance(), R.string.modify_password_invalid);
                return;
            }
            resetPassword();

        } else if (i == R.id.ibt_back) {
            finish();

        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int i = v.getId();
        if (i == R.id.et_password_new) {
            emmSecurityKeyboard.showSecurityKeyBoard(passwordNewEdit);

        } else if (i == R.id.et_password_confirm) {
            emmSecurityKeyboard.showSecurityKeyBoard(passwordConfirmEdit);

        }
        return false;
    }

    private void resetPassword() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
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
            int i = view.getId();
            if (i == R.id.et_password_new) {
                emmSecurityKeyboard.showSecurityKeyBoard(passwordNewEdit);

            } else if (i == R.id.et_password_confirm) {
                emmSecurityKeyboard.showSecurityKeyBoard(passwordConfirmEdit);

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
            boolean isInputValaid = passwordNew.length() >= 1 && passwordConfirm.length() >= 1;
            okBtn.setEnabled(isInputValaid);
            okBtn.setBackgroundResource(isInputValaid ? R.drawable.selector_login_btn : R.drawable.bg_login_btn_unable);
        }
    }

    class WebService extends LoginAPIInterfaceImpl {
        @Override
        public void returnResetPasswordSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            BaseApplication.getInstance().signout();
        }

        @Override
        public void returnResetPasswordFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            WebServiceMiddleUtils.hand(PasswordResetActivity.this, error, errorCode);
        }
    }
}
