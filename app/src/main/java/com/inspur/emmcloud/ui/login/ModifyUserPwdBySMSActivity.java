package com.inspur.emmcloud.ui.login;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.GetBoolenResult;
import com.inspur.emmcloud.broadcastreceiver.SmsCaptchasReceiver;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.FomatUtils;
import com.inspur.emmcloud.util.IntentUtils;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.LoginUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

/**
 * 
 * 修改密码短信验证
 *
 */
public class ModifyUserPwdBySMSActivity extends BaseActivity{

	private static final int SENDED_CAPTCHAS_MSG = 2;
	private static final int LOGIN_SUCCESS = 0;
	private static final int LOGIN_FAIL = 1;
	private TimeCount timeCount;
	private Button getSMSButton;
	private String phoneNum = "";
	private EditText phoneNumEdit;
	private EditText codeEdit;
	private LoginAPIService apiService;
	private Handler handler;
	private LoadingDialog loadingDialog;
	private SmsCaptchasReceiver receiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_userpwd_by_sms);
		initViews();
		handMessage();
		registerSMSReceiver();
	}
	
	/**
	 * 处理短信返回
	 */
	private void handMessage() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (loadingDialog.isShowing()) {
					loadingDialog.dismiss();
				}
				switch (msg.what) {
				case SENDED_CAPTCHAS_MSG:
					String captchas = (String) msg.obj;
					codeEdit.setText(AppUtils.getDynamicPassword(captchas));
					break;
				case LOGIN_SUCCESS:
					String smsCode = codeEdit.getText().toString();
					Bundle bundle = new Bundle();
					bundle.putString("smsCode",smsCode);
					IntentUtils.startActivity(ModifyUserPwdBySMSActivity.this, FillNewPwdActivity.class, bundle, true);
					break;
				case LOGIN_FAIL:
					if (loadingDialog != null && loadingDialog.isShowing()) {
						loadingDialog.dismiss();
					}
					break;
				default:
					break;
				}
			}

		};
	}
	
	/**
	 * 注册短信监听
	 */
	private void registerSMSReceiver() {
		receiver = new SmsCaptchasReceiver(
				ModifyUserPwdBySMSActivity.this, handler);
		this.getContentResolver().registerContentObserver(
				Uri.parse("content://sms/"), true, receiver);
	}
	
	/**
	 * 取消监听注册
	 */
	private void unRegisterSMSReceiver() {
		if (receiver != null) {
			this.getContentResolver().unregisterContentObserver(receiver);
			receiver = null;
		}
	}
	
	/**
	 * 初始化界面View
	 */
	private void initViews() {
		apiService = new LoginAPIService(ModifyUserPwdBySMSActivity.this);
		apiService.setAPIInterface(new WebService());
		loadingDialog = new LoadingDialog(ModifyUserPwdBySMSActivity.this);
		getSMSButton = (Button) findViewById(R.id.get_captchas_btn);
		phoneNumEdit = (EditText) findViewById(R.id.phone_num_edit);
		phoneNumEdit.addTextChangedListener(new EditWatcher());
		codeEdit = (EditText) findViewById(R.id.captchas_edit);
		codeEdit.addTextChangedListener(new EditWatcher());
		timeCount = new TimeCount(60000, 1000);
		//由MyInfoActivity界面保证有手机号码，并且手机号码格式正确
		if(getIntent().hasExtra("phoneNum")){
			phoneNumEdit.setEnabled(false);
			phoneNumEdit.setText(getIntent().getStringExtra("phoneNum"));
		}else {
			phoneNumEdit.setEnabled(true);
		}
	}

	public void onClick(View view){
		switch (view.getId()) {
		case R.id.back_layout:
			finish();
			break;
		case R.id.next_step_btn:
			if (!getIsPhoneNumIllegal()) {
				LoginUtils loginUtils = new LoginUtils(ModifyUserPwdBySMSActivity.this, handler);
				String smsCode = codeEdit.getText().toString();
				loginUtils.login(phoneNum, smsCode,true);
			}
			break;
		case R.id.get_captchas_btn:
			if(!getIsPhoneNumIllegal()){
				getSMSCode();
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 获取短信验证码
	 */
	private void getSMSCode() {
		if(NetUtils.isNetworkConnected(ModifyUserPwdBySMSActivity.this)){
			loadingDialog.show();
			apiService.reqLoginSMS(phoneNum);
		}
	}

	/**
	 * 判断手机号码是否合法;
	 * @return
	 */
	private boolean getIsPhoneNumIllegal() {
		phoneNum = phoneNumEdit.getText().toString();
		if (StringUtils.isBlank(phoneNum)) {
			ToastUtils.show(getApplicationContext(),
					getString(R.string.please_input_phone_num));
			return true;
		}
		if (!FomatUtils.isPhoneNum(phoneNum)) {
			ToastUtils.show(getApplicationContext(),
					getString(R.string.phone_num_illegal_format));
			return true;
		}
		return false;
	}

	/**
	 * 倒计时
	 */
	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
		}

		@Override
		public void onFinish() {// 计时完毕时触发
			getSMSButton.setText(getString(R.string.recover));
			getSMSButton.setClickable(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示
			getSMSButton.setClickable(false);
			getSMSButton.setText(getString(R.string.recover) + "("
					+ millisUntilFinished / 1000 + ")");
		}
	}
	
	/**
	 * 检测输入框的输入
	 */
	private class EditWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			boolean isPhoneNumBlank = StringUtils.isBlank(phoneNumEdit
					.getText().toString());
			boolean isCodeBlank = StringUtils.isBlank(codeEdit
					.getText().toString());
			if (isPhoneNumBlank || isCodeBlank
					|| !FomatUtils.isPhoneNum(phoneNumEdit.getText().toString())) {
				((Button) findViewById(R.id.next_step_btn)).setEnabled(false);
				((Button) findViewById(R.id.next_step_btn))
						.setBackgroundResource(R.drawable.bg_login_btn_unable);
			} else {
				((Button) findViewById(R.id.next_step_btn)).setEnabled(true);
				((Button) findViewById(R.id.next_step_btn))
						.setBackgroundResource(R.drawable.selector_login_btn);
			}
			// 修改填完验证码后修改手机号置空
			if (!isCodeBlank) {
				String phoneNumTmp = phoneNumEdit.getText().toString();
				if (!FomatUtils.isPhoneNum(phoneNumTmp)) {
					codeEdit.setText("");
				}
			}
		}
	}
	
	class WebService extends APIInterfaceInstance{
		@Override
		public void returnReqLoginSMSSuccess(GetBoolenResult getBoolenResult) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			phoneNumEdit.setEnabled(false);
			ToastUtils.show(getApplicationContext(),
					getString(R.string.captchas_getcode_success));
			timeCount.start();
		}

		@Override
		public void returnReqLoginSMSFail(String error, int errorCode) {
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			if (errorCode == 400) {
				handleErrorCode(error,errorCode);
//				ToastUtils.show(getApplicationContext(), getApplicationContext().getString(R.string.no_phone_num));
			} else {
				WebServiceMiddleUtils.hand(ModifyUserPwdBySMSActivity.this, error,errorCode);
			}
		}
		
		
	}

	/**
	 * 处理400错，因现在10901错不是基本的错误类型
	 * 只在此处进行了特殊处理，如果以后是统一错误将封装，
	 * 此处理方法已确认
	 * 20170516  yfc
	 * @param error
	 */
	private void handleErrorCode(String error,int errorCode) {
		String code = JSONUtils.getString(error, "code", "");
		if (!StringUtils.isBlank(code) && code.equals("10901")) {
			ToastUtils.show(getApplicationContext(), getApplicationContext().getString(R.string.cant_login_with_sms));
		} else {
			WebServiceMiddleUtils.hand(ModifyUserPwdBySMSActivity.this, error,errorCode);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegisterSMSReceiver();
	}
	
}
