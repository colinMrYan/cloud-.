package com.inspur.emmcloud.login.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.EditTextUtils;
import com.inspur.emmcloud.baselib.util.FomatUtils;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.ClearEditText;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.login.R;
import com.inspur.emmcloud.login.R2;
import com.inspur.emmcloud.login.api.LoginAPIInterfaceImpl;
import com.inspur.emmcloud.login.api.LoginAPIService;
import com.inspur.emmcloud.login.util.LoginUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 短信登录
 */
@Route(path = Constant.AROUTER_CLASS_LOGIN_BY_SMS)
public class LoginBySmsActivity extends BaseActivity {

    public static final int MODE_LOGIN = 1;
    public static final int MODE_FORGET_PASSWORD = 2;
    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_PHONE = "extra_phone";
    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAIL = 1;
    private static final int GET_SMS_CAPTCHA = 2;
    @BindView(R2.id.tv_title)
    TextView titleText;
    @BindView(R2.id.text_input_layout_phone)
    TextInputLayout phoneTextInputLayout;
    @BindView(R2.id.et_phone)
    ClearEditText phoneEdit;
    @BindView(R2.id.et_captcha)
    ClearEditText captchaEdit;
    @BindView(R2.id.bt_get_captcha)
    Button getCapthaBtn;
    @BindView(R2.id.bt_login)
    Button loginBtn;
    @BindView(R2.id.tv_login_by_account)
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
        return R.layout.login_activity_login_by_sms;
    }

    protected int getStatusType() {
        return STATUS_WHITE_DARK_FONT;
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        EditWatcher watcher = new EditWatcher();
        long startCountTime = PreferencesUtils.getLong(LoginBySmsActivity.this, Constant.SMS_LOGIN_START_TIME, -1);
        long differTime = System.currentTimeMillis() - startCountTime;
        if (startCountTime > 0 && (differTime <= 60000)) {
            myCountDownTimer = new MyCountDownTimer(60000 - differTime, 1000);
            myCountDownTimer.start();
        } else {
            myCountDownTimer = new MyCountDownTimer(60000, 1000);
        }
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
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.ll_main) {
            InputMethodUtils.hide(LoginBySmsActivity.this);

        } else if (i == R.id.bt_get_captcha) {
            if (myCountDownTimer != null) {
                myCountDownTimer.cancel();
                myCountDownTimer = null;
            }
            myCountDownTimer = new MyCountDownTimer(60000, 1000);
            phone = phoneEdit.getText().toString();
            if (StringUtils.isBlank(phone)) {
                ToastUtils.show(BaseApplication.getInstance(), R.string.login_please_input_phone_num);
                return;
            }
            if (!FomatUtils.isPhoneNum(phone)) {
                phoneTextInputLayout.setError(getString(R.string.login_phone_num_illegal_format));
                return;
            }
            getSMSCaptcha();

        } else if (i == R.id.bt_login) {
            phone = phoneEdit.getText().toString();
            captcha = captchaEdit.getText().toString();
            if (!FomatUtils.isPhoneNum(phone)) {
                phoneTextInputLayout.setError(getString(R.string.login_phone_num_illegal_format));
                return;
            }
            login();

        } else if (i == R.id.tv_login_by_account) {
            finish();

        }
    }


    private void enterApp() {
        boolean isHasSetShortPassword = PreferencesUtils.getBoolean(LoginBySmsActivity.this, Constant.PREF_LOGIN_HAVE_SET_PASSWORD, false);
        if (!isHasSetShortPassword) {
            String forgetUrl = PreferencesUtils.getString(this, Constant.PREF_LOGIN_FORGET_URL);
            if (!StringUtils.isEmpty(forgetUrl) && forgetUrl.startsWith("http")) {
                Bundle bundle = new Bundle();
                bundle.putString("uri", forgetUrl);
                ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
                return;
            }
            IntentUtils.startActivity(LoginBySmsActivity.this, PasswordFirstSettingActivity.class, true);
        } else {
            ARouter.getInstance().build(BaseApplication.getInstance().getIntentClassRouterAfterLogin()).navigation();
            finish();
        }
    }

    private void handMessage() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case LOGIN_SUCCESS:
                        PreferencesUtils.putLong(LoginBySmsActivity.this, Constant.SMS_LOGIN_START_TIME, -1);
                        if (mode == MODE_LOGIN) {
                            myCountDownTimer.cancel();
                            //存储手机号作为登录用户名
                            PreferencesUtils.putString(BaseApplication.getInstance(), Constant.PREF_LOGIN_USERNAME, phone);
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
            loginUtils.login(phone, captcha, true, mode);
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
            getCapthaBtn.setText(getString(R.string.login_recover));
            getCapthaBtn.setClickable(true);
            PreferencesUtils.putLong(LoginBySmsActivity.this, Constant.SMS_LOGIN_START_TIME, -1);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            getCapthaBtn.setClickable(false);
            getCapthaBtn.setText(getString(R.string.login_recover) + "("
                    + millisUntilFinished / 1000 + ")");
        }
    }

    private class WebService extends LoginAPIInterfaceImpl {
        @Override
        public void returnLoginSMSCaptchaSuccess() {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            phoneEdit.setEnabled(false);
            ToastUtils.show(LoginBySmsActivity.this, R.string.login_captchas_getcode_success);
            myCountDownTimer.start();
            PreferencesUtils.putLong(LoginBySmsActivity.this, Constant.SMS_LOGIN_START_TIME, System.currentTimeMillis());
        }

        @Override
        public void returnLoginSMSCaptchaFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            LoadingDialog.dimissDlg(loadingDlg);
            String code = JSONUtils.getString(error, "code", "");
            /**10901 forbidden**/
            if (errorCode == 400 && code.equals("10901")) {
                ToastUtils.show(LoginBySmsActivity.this, R.string.login_cant_login_with_sms);
                /**1002 noAccount**/
            } else if(errorCode == 400 && code.equals("1002")){
                ToastUtils.show(R.string.login_user_account_not_exist);
            } else {
                WebServiceMiddleUtils.hand(LoginBySmsActivity.this, error, errorCode);
            }
        }

    }

}


