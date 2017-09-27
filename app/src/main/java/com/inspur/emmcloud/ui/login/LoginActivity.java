package com.inspur.emmcloud.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.EditTextUtils;
import com.inspur.emmcloud.util.InputMethodUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.LoginUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;

/**
 * 登录页面
 * 
 * @author Administrator
 *
 */
public class LoginActivity extends BaseActivity {

	private static final int LOGIN_SUCCESS = 0;
	private static final int LOGIN_FAIL = 1;
	private String userName;
	private String password;

	private LoadingDialog LoadingDlg;
	private Handler handler;
	private ClearEditText userNameEdit;
	private EditText passwordEdit;
	private Button loginBtn;
	private ImageView seePWImg;
	private boolean canSee = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initView();
		handMessage();
	}

	private void initView() {
		((RelativeLayout) findViewById(R.id.main_layout))
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						InputMethodUtils.hide(LoginActivity.this);
						return false;
					}
				});
		loginBtn = (Button) findViewById(R.id.login_btn);
		LoadingDlg = new LoadingDialog(LoginActivity.this,
				getString(R.string.login_loading_text));
		userNameEdit = ((ClearEditText) findViewById(R.id.username_edit));
		passwordEdit = ((EditText) findViewById(R.id.password_edit));
		// 为用户名输入框设置输入监听
		userNameEdit.addTextChangedListener(new EditWatcher(userNameEdit));
		passwordEdit.addTextChangedListener(new EditWatcher(passwordEdit));
		userName = PreferencesUtils.getString(getApplicationContext(),
				"userName", "");
		EditTextUtils.setText(userNameEdit, userName);
		seePWImg = (ImageView) findViewById(R.id.see_pw_img);
		seePWImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (canSee) {
					seePWImg.setImageResource(R.drawable.icon_no_see_pw);
					passwordEdit.setInputType(InputType.TYPE_CLASS_TEXT
							| InputType.TYPE_TEXT_VARIATION_PASSWORD);

				} else {
					seePWImg.setImageResource(R.drawable.icon_see_pw);
					passwordEdit
							.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				}
				EditTextUtils.setText(passwordEdit, passwordEdit.getText()
						.toString());
				canSee = !canSee;
			}
		});
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_btn:
			userName = userNameEdit.getText().toString();
			password = passwordEdit.getText().toString();
			if (StringUtils.isEmpty(userName)) {
				ToastUtils.show(getApplicationContext(),
						getString(R.string.please_input_username));
				return;
			}
			if (StringUtils.isEmpty(password)) {
				ToastUtils.show(getApplicationContext(),
						getString(R.string.please_input_password));
				return;
			}
			loginApp();
			break;
		case R.id.register_text:
			IntentUtils
					.startActivity(LoginActivity.this, NewUserActivity.class);
			break;
		case R.id.forget_pwd_text:
			IntentUtils
				.startActivity(LoginActivity.this, ModifyUserPwdBySMSActivity.class);
			break;
		case R.id.captchas_login_text:
			IntentUtils.startActivity(LoginActivity.this,
					CaptchasLoginActivity.class);
			break;
		default:
			break;
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

	private void handMessage() {
		// TODO Auto-generated method stub
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (LoadingDlg != null && LoadingDlg.isShowing()) {
					LoadingDlg.dismiss();
				}
				switch (msg.what) {
				case LOGIN_SUCCESS:
					PreferencesUtils.putString(getApplicationContext(),
							"userName", userName);
					goIndex();
					break;
				case LOGIN_FAIL:
					break;

				default:
					break;
				}
			}

		};
	}

	// 跳转到主页面
	private void goIndex() {
		Intent intent = new Intent();
		if (!PreferencesUtils.getBoolean(LoginActivity.this, "hasPassword")) {
			intent.setClass(LoginActivity.this,
					ModifyUserFirstPsdActivity.class);
		} else {
			boolean hasPassWord = false;
			hasPassWord = PreferencesUtils.getBoolean(LoginActivity.this,"hasPassword",false);
			if(hasPassWord){
				intent.setClass(LoginActivity.this, IndexActivity.class);
			}else{
				intent.setClass(LoginActivity.this,
						ModifyUserFirstPsdActivity.class);
			}
		}
		startActivity(intent);
		LoginActivity.this.finish();
	}

	/**
	 * 检测用户名和密码输入框
	 *
	 */
	private class EditWatcher implements TextWatcher {

		private EditText editText;

		public EditWatcher(EditText editText) {
			this.editText = editText;
		}

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
			if (editText.getId() != R.id.username_edit) {
				if (passwordEdit.getText().toString().length() >= 6
						&& !StringUtils.isBlank(userNameEdit.getText()
								.toString())) {
					loginBtn.setEnabled(true);
					loginBtn.setBackgroundResource(R.drawable.selector_login_btn);
				} else {
					loginBtn.setEnabled(false);
					loginBtn.setBackgroundResource(R.drawable.bg_login_btn_unable);
				}

			}
		}

	}



	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (handler != null) {
			handler = null;
		}
	}
}
