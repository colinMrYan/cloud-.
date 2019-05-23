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
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.keyboardview.EmmSecurityKeyboard;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 修改密码
 */

public class PasswordModifyActivity extends BaseActivity implements View.OnTouchListener {

    @BindView(R.id.bt_save)
    Button saveBtn;
    @BindView(R.id.et_password_origin)
    EditText passwordOriginEdit;
    @BindView(R.id.et_password_new)
    EditText passwordNewEdit;
    @BindView(R.id.et_password_confirm)
    EditText passwordConfirmEdit;
    private String passwordOrigin;
    private String passwordNew;
    private String passwordConfirm;
    private LoadingDialog loadingDlg;
    private EmmSecurityKeyboard emmSecurityKeyboard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_modify);
        ButterKnife.bind(this);
        ImmersionBar.with(this).statusBarColor(android.R.color.white).statusBarDarkFont(true, 0.2f).init();
        EditWatcher editWatcher = new EditWatcher();
        passwordOriginEdit.addTextChangedListener(editWatcher);
        passwordNewEdit.addTextChangedListener(editWatcher);
        passwordConfirmEdit.addTextChangedListener(editWatcher);
        loadingDlg = new LoadingDialog(this);
        emmSecurityKeyboard = new EmmSecurityKeyboard(this);
        initListeners();
    }

    private void initListeners() {
        passwordOriginEdit.setOnTouchListener(this);
        passwordNewEdit.setOnTouchListener(this);
        passwordConfirmEdit.setOnTouchListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_save:
                if (!passwordNew.equals(passwordConfirm)) {
                    ToastUtils.show(PasswordModifyActivity.this, R.string.modify_not_same);
                    return;
                }
                if (passwordNew.equals(passwordOrigin)) {
                    ToastUtils.show(PasswordModifyActivity.this, R.string.modify_new_old_same);
                }
                if (passwordNew.length() < 6 || passwordNew.length() > 16 || !FomatUtils.isPasswrodStrong(passwordNew)) {
                    ToastUtils.show(MyApplication.getInstance(), R.string.modify_password_invalid);
                    return;
                }
                modifyPassword();
                break;
            case R.id.ibt_back:
                finish();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.et_password_origin:
                emmSecurityKeyboard.showSecurityKeyBoard(passwordOriginEdit);
                break;
            case R.id.et_password_new:
                emmSecurityKeyboard.showSecurityKeyBoard(passwordNewEdit);
                break;
            case R.id.et_password_confirm:
                emmSecurityKeyboard.showSecurityKeyBoard(passwordConfirmEdit);
                break;
        }
        return false;
    }

    private void modifyPassword() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            LoginAPIService apiService = new LoginAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.modifyPassword(passwordOrigin, passwordNew);
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
            passwordOrigin = passwordOriginEdit.getText().toString();
            passwordNew = passwordNewEdit.getText().toString();
            passwordConfirm = passwordConfirmEdit.getText().toString();
            boolean isInputValaid = !StringUtils.isBlank(passwordOrigin) && passwordNew.length() >= 1 && passwordConfirm.length() >= 1;
            saveBtn.setEnabled(isInputValaid);
            saveBtn.setBackgroundResource(isInputValaid ? R.drawable.selector_login_btn : R.drawable.bg_login_btn_unable);
        }

    }

    class WebService extends APIInterfaceInstance {

        @Override
        public void returnModifyPasswordSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            MyApplication.getInstance().signout();
        }

        @Override
        public void returnModifyPasswordFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
            String code = JSONUtils.getString(error, "status", "");
            if (code.equals("400")) {
                error = getString(R.string.modify_password_not_correct);
            } else {
                error = getString(R.string.modify_password_fail);
            }
            ToastUtils.show(MyApplication.getInstance(), error);
        }
    }
}
