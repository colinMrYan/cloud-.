package com.inspur.emmcloud.ui.login;

import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;
import com.inspur.emmcloud.widget.keyboardview.EmmSecurityKeyboard;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 用户初次使用短信验证码登录后，当没有设置密码时进入改页面
 */

public class PasswordFirstSettingActivity extends BaseActivity {
    @BindView(R.id.bt_save)
    Button saveBtn;
    @BindView(R.id.et_password_new)
    EditText passwordNewEdit;
    @BindView(R.id.et_password_confirm)
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
        loadingDlg = new LoadingDialog(this);
        emmSecurityKeyboard = new EmmSecurityKeyboard(this);
        EditOnTouchListener editOnTouchListener = new EditOnTouchListener();
        passwordNewEdit.setOnTouchListener(editOnTouchListener);
        passwordConfirmEdit.setOnTouchListener(editOnTouchListener);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_password_first_setting;
    }

    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_save:
                if (!passwordNew.equals(passwordConfirm)) {
                    ToastUtils.show(PasswordFirstSettingActivity.this, R.string.modify_not_same);
                    return;
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
            case R.id.bt_skip:
                IntentUtils.startActivity(this, IndexActivity.class, true);
                break;
        }
    }

    private void showPasswordSettingFailDlg() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (which == -2) {
                    IntentUtils.startActivity(PasswordFirstSettingActivity.this, IndexActivity.class, true);
                }
            }
        };

        EasyDialog.showDialog(PasswordFirstSettingActivity.this, getString(R.string.prompt),
                getString(R.string.modify_user_password_fail),
                getString(R.string.ok),
                getString(R.string.cancel), listener, false);
    }

    private void modifyPassword() {
        if (NetUtils.isNetworkConnected(MyApplication.getInstance())) {
            loadingDlg.show();
            LoginAPIService apiService = new LoginAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.modifyPassword("", passwordNew);
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
            boolean isInputValaid = passwordNew.length() >= 1 && passwordConfirm.length() >= 1;
            saveBtn.setEnabled(isInputValaid);
            saveBtn.setBackgroundResource(isInputValaid ? R.drawable.selector_login_btn : R.drawable.bg_login_btn_unable);
        }
    }

    class WebService extends APIInterfaceInstance {
        @Override
        public void returnModifyPasswordSuccess() {
            LoadingDialog.dimissDlg(loadingDlg);
            ToastUtils.show(PasswordFirstSettingActivity.this, getString(R.string.modify_user_password_success));
            MyApplication.getInstance().signout();
        }

        @Override
        public void returnResetPasswordFail(String error, int errorCode) {
            LoadingDialog.dimissDlg(loadingDlg);
//            WebServiceMiddleUtils.hand(PasswordFirstSettingActivity.this, error,errorCode);
            showPasswordSettingFailDlg();

        }
    }
}
