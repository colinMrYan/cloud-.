package com.inspur.emmcloud.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.common.EditTextUtils;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.IntentUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.LoginUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.keyboardview.EmmSecurityKeyboard;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;


/**
 * 登录页面
 */

@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity {

    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_FAIL = 1;
    private static final int LOGIN_MORE = 2;
    private String userName;
    private String password;

    private LoadingDialog LoadingDlg;
    private Handler handler;
    @ViewInject(R.id.et_username)
    private ClearEditText usernameEdit;
    @ViewInject(R.id.et_password)
    private EditText passwordEdit;
    @ViewInject(R.id.bt_login)
    private Button loginBtn;
    @ViewInject(R.id.tv_current_login_enterprise)
    private TextView currentLoginEnterpriseText;
    @ViewInject(R.id.tv_welcome)
    private TextView welcomeText;
    private EmmSecurityKeyboard securityKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        PreferencesUtils.putString(this, Constant.PREF_APP_PREVIOUS_VERSION, AppUtils.getVersion(this));
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        ImmersionBar.with(this).statusBarColor(android.R.color.white).statusBarDarkFont(true).init();
        MyApplication.getInstance().closeOtherActivity(LoginActivity.this);
        initView();
        handMessage();
    }

    private void initView() {
        welcomeText.setText(getString(R.string.login_tv_welcome, AppUtils.getAppName(this)));
        LoadingDlg = new LoadingDialog(LoginActivity.this, getString(R.string.login_loading_text));
        EditWatcher watcher = new EditWatcher();
        usernameEdit.addTextChangedListener(watcher);
        passwordEdit.addTextChangedListener(watcher);
        userName = PreferencesUtils.getString(getApplicationContext(),
                Constant.PREF_LOGIN_USERNAME, "");
        EditTextUtils.setText(usernameEdit, userName);
        setCurrentLoginEnterpriseName();
        securityKeyboard = new EmmSecurityKeyboard(this);
        passwordEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                securityKeyboard.showSecurityKeyBoard(passwordEdit);
                return false;
            }
        });
        usernameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                securityKeyboard.showSecurityKeyBoard(passwordEdit);
                return false;
            }
        });
    }


    /**
     * 显示当前登录租户信息
     */
    private void setCurrentLoginEnterpriseName() {
        String enterpriseName = PreferencesUtils.getString(LoginActivity.this, Constant.PREF_LOGIN_ENTERPRISE_NAME, "");
        currentLoginEnterpriseText.setVisibility(StringUtils.isBlank(enterpriseName)
                ? View.INVISIBLE : View.VISIBLE);
        currentLoginEnterpriseText.setText(getString(R.string.login_current_login_enterprise, enterpriseName));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setCurrentLoginEnterpriseName();
    }

    public void onClick(View v) {
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.bt_login:
                userName = usernameEdit.getText().toString();
                password = passwordEdit.getText().toString();
                loginApp();
                break;
            case R.id.tv_forget_password:
                bundle.putInt(LoginBySmsActivity.EXTRA_MODE, LoginBySmsActivity.MODE_FORGET_PASSWORD);
                IntentUtils.startActivity(LoginActivity.this, LoginBySmsActivity.class, bundle);
                break;
            case R.id.tv_login_via_sms:
                bundle.putInt(LoginBySmsActivity.EXTRA_MODE, LoginBySmsActivity.MODE_LOGIN);
                IntentUtils.startActivity(LoginActivity.this, LoginBySmsActivity.class, bundle);
                break;
            case R.id.bt_more:
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, LoginMoreActivity.class);
                startActivityForResult(intent, LOGIN_MORE);
                break;
            case R.id.ll_main:
                InputMethodUtils.hide(LoginActivity.this);
                if (securityKeyboard.isShowing()) {
                    securityKeyboard.dismiss();
                }
                break;
            default:
                break;
        }
    }


    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                LoadingDialog.dimissDlg(LoadingDlg);
                switch (msg.what) {
                    case LOGIN_SUCCESS:
                        enterApp();
                        break;
                    case LOGIN_FAIL:
                        break;

                    default:
                        break;
                }
            }

        };
    }

    /**
     * 进入app
     */
    private void enterApp() {
        //当没有设置短密码时进入密码设置界面
        PreferencesUtils.putString(getApplicationContext(),
                Constant.PREF_LOGIN_USERNAME, userName);
        boolean isHasSetShortPassword = PreferencesUtils.getBoolean(LoginActivity.this, Constant.PREF_LOGIN_HAVE_SET_PASSWORD, false);
        if (!isHasSetShortPassword) {
            IntentUtils.startActivity(LoginActivity.this, PasswordFirstSettingActivity.class, true);
        } else {
            IntentUtils.startActivity(LoginActivity.this, IndexActivity.class, true);
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }
        if (securityKeyboard != null) {
            securityKeyboard.dismiss();
            securityKeyboard = null;
        }
    }

    /**
     * 登录应用
     */
    private void loginApp() {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(LoginActivity.this)) {
            LoginUtils loginUtils = new LoginUtils(LoginActivity.this, handler);
            loginUtils.login(userName, password);
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
            boolean isInputValaid = passwordEdit.getText().toString().length() >= 6
                    && !StringUtils.isBlank(usernameEdit.getText()
                    .toString());
            loginBtn.setEnabled(isInputValaid);
            loginBtn.setBackgroundResource(isInputValaid ? R.drawable.selector_login_btn : R.drawable.bg_login_btn_unable);
        }

    }
}
