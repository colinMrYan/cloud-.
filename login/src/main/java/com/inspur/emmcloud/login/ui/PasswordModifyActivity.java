package com.inspur.emmcloud.login.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.login.R;
import com.inspur.emmcloud.login.R2;
import com.inspur.emmcloud.login.api.LoginAPIInterfaceImpl;
import com.inspur.emmcloud.login.api.LoginAPIService;
import com.inspur.emmcloud.login.widget.keyboardview.EmmSecurityKeyboard;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 修改密码
 */
@Route(path = "/login/password_modify")
public class PasswordModifyActivity extends BaseActivity implements View.OnTouchListener {

    @BindView(R2.id.bt_save)
    Button saveBtn;
    @BindView(R2.id.et_password_origin)
    EditText passwordOriginEdit;
    @BindView(R2.id.et_password_new)
    EditText passwordNewEdit;
    @BindView(R2.id.et_password_confirm)
    EditText passwordConfirmEdit;
    private String passwordOrigin;
    private String passwordNew;
    private String passwordConfirm;
    private LoadingDialog loadingDlg;
    private EmmSecurityKeyboard emmSecurityKeyboard;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        EditWatcher editWatcher = new EditWatcher();
        passwordOriginEdit.addTextChangedListener(editWatcher);
        passwordNewEdit.addTextChangedListener(editWatcher);
        passwordConfirmEdit.addTextChangedListener(editWatcher);
        loadingDlg = new LoadingDialog(this);
        emmSecurityKeyboard = new EmmSecurityKeyboard(this);
        initListeners();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.login_activity_password_modify;
    }

    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }

    private void initListeners() {
        passwordOriginEdit.setOnTouchListener(this);
        passwordNewEdit.setOnTouchListener(this);
        passwordConfirmEdit.setOnTouchListener(this);
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bt_save) {
            if (!passwordNew.equals(passwordConfirm)) {
                ToastUtils.show(PasswordModifyActivity.this, R.string.modify_not_same);
                return;
            }
            if (passwordNew.equals(passwordOrigin)) {
                ToastUtils.show(PasswordModifyActivity.this, R.string.modify_new_old_same);
            }
            if (passwordNew.length() < 6 || passwordNew.length() > 16 || !FomatUtils.isPasswrodStrong(passwordNew)) {
                ToastUtils.show(BaseApplication.getInstance(), R.string.modify_password_invalid);
                return;
            }
            modifyPassword();

        } else if (i == R.id.ibt_back) {
            finish();

        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int i = v.getId();
        if (i == R.id.et_password_origin) {
            emmSecurityKeyboard.showSecurityKeyBoard(passwordOriginEdit);

        } else if (i == R.id.et_password_new) {
            emmSecurityKeyboard.showSecurityKeyBoard(passwordNewEdit);

        } else if (i == R.id.et_password_confirm) {
            emmSecurityKeyboard.showSecurityKeyBoard(passwordConfirmEdit);

        }
        return false;
    }

    private void modifyPassword() {
        if (NetUtils.isNetworkConnected(BaseApplication.getInstance())) {
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

    class WebService extends LoginAPIInterfaceImpl {

        @Override
        public void returnModifyPasswordSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            BaseApplication.getInstance().signout();
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
            ToastUtils.show(BaseApplication.getInstance(), error);
        }
    }
}
