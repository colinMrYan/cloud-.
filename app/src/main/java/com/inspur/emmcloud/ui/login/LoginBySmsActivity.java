package com.inspur.emmcloud.ui.login;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.baselib.util.EditTextUtils;
import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.privates.InputMethodUtils;
import com.inspur.emmcloud.util.privates.LoginUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 短信登录
 */

public class LoginBySmsActivity extends BaseActivity {

    public static final int MODE_LOGIN = 1;
    public static final int MODE_FORGET_PASSWORD = 2;
    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_PHONE = "extra_phone";
    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAIL = 1;
    private static final int GET_SMS_CAPTCHA = 2;
    @BindView(R.id.tv_title)
    TextView titleText;
    @BindView(R.id.text_input_layout_phone)
    TextInputLayout phoneTextInputLayout;
    @BindView(R.id.et_phone)
    ClearEditText phoneEdit;
    @BindView(R.id.et_captcha)
    ClearEditText captchaEdit;
    @BindView(R.id.bt_get_captcha)
    Button getCapthaBtn;
    @BindView(R.id.bt_login)
    Button loginBtn;
    @BindView(R.id.tv_login_by_account)
    TextView loginByAccountText;
    private int mode = MODE_LOGIN;
    private Handler handler;
    private LoadingDialog loadingDlg;
    private String phone;
    private String captcha;
    private MyCountDownTimer myCountDownTimer;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        initView();
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_login_by_sms;
    }

    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        EditWatcher watcher = new EditWatcher();
        myCountDownTimer = new MyCountDownTimer(60000, 1000);
        phoneEdit.addTextChangedListener(watcher);
        captchaEdit.addTextChangedListener(watcher);
        mode = getIntent().getExtras().getInt(EXTRA_MODE, MODE_LOGIN);
        loginBtn.setText((mode == MODE_LOGIN) ? R.string.login : R.string.next_step);
        titleText.setText((mode == MODE_LOGIN) ? R.string.login_code_login_text : R.string.login_find_password);
//        loginByAccountText.setVisibility((mode == MODE_LOGIN) ? View.VISIBLE : View.INVISIBLE);
        handMessage();
        if (getIntent().hasExtra(EXTRA_PHONE)) {
            phone = getIntent().getExtras().getString(EXTRA_PHONE, "");
            if (!StringUtils.isBlank(phone)) {
                EditTextUtils.setText(phoneEdit, phone);
            }
        }
        if (StringUtils.isBlank(phone)) {
            //当用户使用手机号和密码登录后，将记录的手机号填入
            String username = PreferencesUtils.getString(this, Constant.PREF_LOGIN_USERNAME, "");
            boolean isPhoneNum = FomatUtils.isPhoneNum(username);
            if (isPhoneNum) {
                EditTextUtils.setText(phoneEdit, username);
            }
        }

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.ll_main:
                InputMethodUtils.hide(LoginBySmsActivity.this);
                break;
            case R.id.bt_get_captcha:
                phone = phoneEdit.getText().toString();
                if (StringUtils.isBlank(phone)) {
                    ToastUtils.show(MyApplication.getInstance(), R.string.login_please_input_phone_num);
                    return;
                }
                if (!FomatUtils.isPhoneNum(phone)) {
                    phoneTextInputLayout.setError(getString(R.string.login_phone_num_illegal_format));
                    return;
                }
                getSMSCaptcha();
                break;
            case R.id.bt_login:
                phone = phoneEdit.getText().toString();
                captcha = captchaEdit.getText().toString();
                if (!FomatUtils.isPhoneNum(phone)) {
                    phoneTextInputLayout.setError(getString(R.string.login_phone_num_illegal_format));
                    return;
                }
                login();
                break;
            case R.id.tv_login_by_account:
                finish();
                break;

        }
    }


    private void enterApp() {
        boolean isHasSetShortPassword = PreferencesUtils.getBoolean(LoginBySmsActivity.this, Constant.PREF_LOGIN_HAVE_SET_PASSWORD, false);
        if (!isHasSetShortPassword) {
            IntentUtils.startActivity(LoginBySmsActivity.this, PasswordFirstSettingActivity.class, true);
        } else {
            IntentUtils.startActivity(LoginBySmsActivity.this, IndexActivity.class, true);
        }
    }

    private void handMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case LOGIN_SUCCESS:
                        if (mode == MODE_LOGIN) {
                            myCountDownTimer.cancel();
                            //存储手机号作为登录用户名
                            PreferencesUtils.putString(MyApplication.getInstance(), Constant.PREF_LOGIN_USERNAME, phone);
                            PreferencesUtils.putString(getApplicationContext(), Constant.PREF_LOGIN_PASSWORD, "");
                            PVCollectModelCacheUtils.saveCollectModel("login", "smsLogin");
                            enterApp();
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString(PasswordResetActivity.EXTRA_CAPTCHA, captcha);
                            IntentUtils.startActivity(LoginBySmsActivity.this, PasswordResetActivity.class, bundle, true);
                        }
                        break;
                    case LOGIN_FAIL:
                        phoneEdit.setEnabled(true);
                        break;
                    case GET_SMS_CAPTCHA:
                        String captchas = (String) msg.obj;
                        EditTextUtils.setText(captchaEdit, AppUtils.getDynamicPassword(captchas));
                        break;
                }
            }

        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
            myCountDownTimer = null;
        }
    }

    private void login() {
        if (NetUtils.isNetworkConnected(this)) {
            LoginUtils loginUtils = new LoginUtils(
                    LoginBySmsActivity.this, handler);
            loginUtils.login(phone, captcha, true);
        }

    }

    private void getSMSCaptcha() {
        if (NetUtils.isNetworkConnected(this)) {
            loadingDlg.show();
            LoginAPIService apiService = new LoginAPIService(LoginBySmsActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getLoginSMSCaptcha(phone);
        }
    }

    /**
     * 检测输入框的输入
     */
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
            String phone = phoneEdit.getText().toString();
            String captcha = captchaEdit.getText().toString();
            boolean isInputValaid = !StringUtils.isBlank(phone) && !StringUtils.isBlank(captcha);
            loginBtn.setEnabled(isInputValaid);
            loginBtn.setBackgroundResource(isInputValaid ? R.drawable.selector_login_btn : R.drawable.bg_login_btn_unable);
            phoneTextInputLayout.setError("");
        }

    }

    class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            getCapthaBtn.setText(getString(R.string.recover));
            getCapthaBtn.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            getCapthaBtn.setClickable(false);
            getCapthaBtn.setText(getString(R.string.recover) + "("
                    + millisUntilFinished / 1000 + ")");
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnLoginSMSCaptchaSuccess() {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            phoneEdit.setEnabled(false);
            ToastUtils.show(LoginBySmsActivity.this, R.string.login_captchas_getcode_success);
            myCountDownTimer.start();
        }

        @Override
        public void returnLoginSMSCaptchaFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            String code = JSONUtils.getString(error, "code", "");
            if (errorCode == 400 && code.equals("10901")) {
                ToastUtils.show(LoginBySmsActivity.this, R.string.login_cant_login_with_sms);
            } else {
                WebServiceMiddleUtils.hand(LoginBySmsActivity.this, error, errorCode);
            }
        }

    }

}


