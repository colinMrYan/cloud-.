package com.inspur.emmcloud.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.system.GetBoolenResult;
import com.inspur.emmcloud.broadcastreceiver.SmsCaptchasReceiver;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.common.EditTextUtils;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.privates.LoginUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

/**
 * 验证码登录界面
 *
 * @author Administrator
 */
public class CaptchasLoginActivity extends BaseActivity {

    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAIL = 1;
    private static final int SENDED_CAPTCHAS_MSG = 2;
    private EditText phoneNumEdit;
    private EditText captchasEdit;
    private Button getCaptchasBtn;
    private TimeCount time;
    private LoginAPIService apiService;
    private String phoneNum;
    private Handler handler;
    private LoadingDialog loadingDlg;
    private String captchas;
    private SmsCaptchasReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captchas_login);
        phoneNumEdit = (EditText) findViewById(R.id.phone_num_edit);
        InputMethodUtils.display(CaptchasLoginActivity.this, phoneNumEdit);
        captchasEdit = (EditText) findViewById(R.id.captchas_edit);
        captchasEdit.addTextChangedListener(new EditWatcher());
        phoneNumEdit.addTextChangedListener(new EditWatcher());
        getCaptchasBtn = (Button) findViewById(R.id.get_captchas_btn);
        loadingDlg = new LoadingDialog(this);

        Intent intent = getIntent();
        String phoneNum = intent.getStringExtra("phoneNum");
        phoneNumEdit.setText(phoneNum);

        handMessage();
        (findViewById(R.id.main_layout))
                .setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // TODO Auto-generated method stub
                        InputMethodUtils.hide(CaptchasLoginActivity.this);
                        return false;
                    }
                });
        time = new TimeCount(60000, 1000);// 构造CountDownTimer对象
        apiService = new LoginAPIService(CaptchasLoginActivity.this);
        apiService.setAPIInterface(new WebService());
        String uerName = PreferencesUtils.getString(this, "userName", "");
        boolean isPhoneNum = FomatUtils.isPhoneNum(uerName);
        if (isPhoneNum) {
            EditTextUtils.setText(phoneNumEdit, uerName);
        }
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                if (loadingDlg.isShowing()) {
                    loadingDlg.dismiss();
                }
                switch (msg.what) {
                    case LOGIN_SUCCESS:
                        phoneNumEdit.setEnabled(true);
                        time.cancel();
                        PreferencesUtils.putString(getApplicationContext(),
                                "userName", phoneNum);
                        PreferencesUtils.putString(getApplicationContext(),
                                "password", "");
                        goIndex();
                        break;
                    case LOGIN_FAIL:
                        phoneNumEdit.setEnabled(true);
                        // getCaptchasBtn.setText(getString(R.string.recover));
                        // getCaptchasBtn.setClickable(true);
                        break;
                    case SENDED_CAPTCHAS_MSG:
                        String captchas = (String) msg.obj;
                        unRegisterSMSReceiver();
                        captchasEdit.setText(AppUtils.getDynamicPassword(captchas));
                        break;
                    default:
                        break;
                }
            }

        };
    }

    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.back_layout:
                finish();

                break;

            case R.id.enter_app_btn:
                captchas = captchasEdit.getText().toString();
                phoneNum = phoneNumEdit.getText().toString();
                if (StringUtils.isBlank(phoneNum)) {
                    ToastUtils.show(getApplicationContext(),
                            getString(R.string.login_please_input_phone_num));
                }
                if (!FomatUtils.isPhoneNum(phoneNum)) {
                    ToastUtils.show(getApplicationContext(),
                            getString(R.string.login_phone_num_illegal_format));
                    return;
                }
                // time.cancel();
                LoginUtils loginUtils = new LoginUtils(
                        CaptchasLoginActivity.this, handler);
                loginUtils.login(phoneNum, captchas, true);

                break;
            case R.id.get_captchas_btn:
                phoneNum = phoneNumEdit.getText().toString();
                if (StringUtils.isBlank(phoneNum)) {
                    ToastUtils.show(getApplicationContext(),
                            getString(R.string.login_please_input_phone_num));
                    return;
                }
                if (!FomatUtils.isPhoneNum(phoneNum)) {
                    ToastUtils.show(getApplicationContext(),
                            getString(R.string.login_phone_num_illegal_format));
                    return;
                }

                if (NetUtils.isNetworkConnected(getApplicationContext())) {
                    loadingDlg.show();
                    registerSMSReceiver();
                    apiService.reqLoginSMS(phoneNum);
                }

                break;
            default:
                break;
        }
    }

    private void registerSMSReceiver() {
        // TODO Auto-generated method stub
        SmsCaptchasReceiver receiver = new SmsCaptchasReceiver(
                CaptchasLoginActivity.this, handler);
        // 注册短信变化监听
        this.getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"), true, receiver);
    }

    private void unRegisterSMSReceiver() {
        // TODO Auto-generated method stub
        if (receiver != null) {
            this.getContentResolver().unregisterContentObserver(receiver);
            receiver = null;
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
            Boolean isPhoneNumBlank = StringUtils.isBlank(phoneNumEdit
                    .getText().toString());
            Boolean isCaptchasBlank = StringUtils.isBlank(captchasEdit
                    .getText().toString());
            // TODO Auto-generated method stub
            if (isPhoneNumBlank || isCaptchasBlank
                    || phoneNumEdit.getText().toString().length() < 11) {
                findViewById(R.id.enter_app_btn).setEnabled(false);
                findViewById(R.id.enter_app_btn)
                        .setBackgroundResource(R.drawable.bg_login_btn_unable);
            } else {
                findViewById(R.id.enter_app_btn).setEnabled(true);
                findViewById(R.id.enter_app_btn)
                        .setBackgroundResource(R.drawable.selector_login_btn);
            }

            // 修改填完验证码后修改手机号置空
            if (!isCaptchasBlank) {
                String phoneNumTmp = phoneNumEdit.getText().toString();
                if (phoneNumTmp.length() < 11) {
                    captchasEdit.setText("");
                }

            }

        }

    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {// 计时完毕时触发
            getCaptchasBtn.setText(getString(R.string.recover));
            getCaptchasBtn.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程显示
            getCaptchasBtn.setClickable(false);
            getCaptchasBtn.setText(getString(R.string.recover) + "("
                    + millisUntilFinished / 1000 + ")");
        }
    }

    class WebService extends APIInterfaceInstance {

        @Override
        public void returnReqLoginSMSSuccess(GetBoolenResult getBoolenResult) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            phoneNumEdit.setEnabled(false);
            ToastUtils.show(getApplicationContext(),
                    getString(R.string.login_captchas_getcode_success));
            time.start();
        }

        @Override
        public void returnReqLoginSMSFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            if (errorCode == 400) {
                handleErrorCode(error,errorCode);
            } else {
                WebServiceMiddleUtils.hand(CaptchasLoginActivity.this, error,errorCode);
            }
            unRegisterSMSReceiver();
        }

    }

    /**
     * 处理400错，因现在10901错不是基本的错误类型
     * 只在此处进行了特殊处理，如果以后是统一错误将封装，
     * 此处理方法已确认
     * 20170513  yfc
     *
     * @param error
     */
    private void handleErrorCode(String error,int errorCode) {
        String code = JSONUtils.getString(error, "code", "");
        if (!StringUtils.isBlank(code) && code.equals("10901")) {
            ToastUtils.show(getApplicationContext(), getApplicationContext().getString(R.string.login_cant_login_with_sms));
        } else {
            WebServiceMiddleUtils.hand(CaptchasLoginActivity.this, error,errorCode);
        }
    }


    // 跳转到主页面
    private void goIndex() {
        Intent intent = new Intent();
        if (!PreferencesUtils.getBoolean(CaptchasLoginActivity.this, "hasPassword")) {
            intent.setClass(CaptchasLoginActivity.this,
                    ModifyUserFirstPsdActivity.class);
        } else {
            boolean hasPassWord = false;
            hasPassWord = PreferencesUtils.getBoolean(CaptchasLoginActivity.this,"hasPassword",false);
            if(hasPassWord){
                LogUtils.YfcDebug("CaptchasLoginActivity有haspassword");
                intent.setClass(CaptchasLoginActivity.this, IndexActivity.class);
            }else{
                LogUtils.YfcDebug("CaptchasLoginActivity没有haspassword");
                intent.setClass(CaptchasLoginActivity.this,
                        ModifyUserFirstPsdActivity.class);
            }
        }
        startActivity(intent);
        CaptchasLoginActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unRegisterSMSReceiver();
        if (handler != null) {
            handler = null;
        }

    }

}
