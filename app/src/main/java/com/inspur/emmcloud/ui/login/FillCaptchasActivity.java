package com.inspur.emmcloud.ui.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.appcenter.GetRegisterCheckResult;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.common.InputMethodUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

/**
 * 填验证码页面
 * 
 * @author Administrator
 *
 */
public class FillCaptchasActivity extends BaseActivity {

	private Button getCaptchasBtn;
	private TimeCount time;
	private EditText captchasEdit;
	private TextView textView;
	private LoadingDialog loadingDialog;
	private LoginAPIService apiService;
	boolean isHasRegistered;
	private String userID;
	private String userName;

	private String phoneNum;
	private String smsCode;
	private String sms;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fill_captchas);
		findViewById(R.id.main_layout)
				.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						InputMethodUtils.hide(FillCaptchasActivity.this);
						return false;
					}
				});

		Intent intent = getIntent();
		phoneNum = intent.getStringExtra("phoneNum");
		getCaptchasBtn = (Button) findViewById(R.id.get_captchas_btn);
		captchasEdit = (EditText) findViewById(R.id.captchas_edit);
		textView = (TextView) findViewById(R.id.phone_num_text);
		textView.setText("+86 " + phoneNum);
		captchasEdit.addTextChangedListener(new EditWatcher());
		time = new TimeCount(30000, 1000);// 构造CountDownTimer对象

		loadingDialog = new LoadingDialog(this);
		apiService = new LoginAPIService(this);
		apiService.setAPIInterface(new WebService());
	}

	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.get_captchas_btn:
			time.start();
			break;
		case R.id.back_layout:
			finish();
			break;
		case R.id.next_step_btn:
			loadingDialog.show();
			if (isHasRegistered) {
				intent.setClass(getApplicationContext(), IndexActivity.class);
				startActivity(intent);
			} else {
				sms = captchasEdit.getText().toString();
				LogUtils.debug("sms", sms);
				if (NetUtils.isNetworkConnected(FillCaptchasActivity.this)) {
					apiService.SMSRegisterCheck(phoneNum, sms);
				}
			}
			break;
		default:
			break;
		}
	}

	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
		}

		@Override
		public void onFinish() {// 计时完毕时触发
			getCaptchasBtn.setText(getString(R.string.login_reverification));
			getCaptchasBtn.setClickable(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示
			getCaptchasBtn.setClickable(false);
			getCaptchasBtn.setText(getString(R.string.recover) + "("
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
			if (StringUtils.isBlank(captchasEdit.getText().toString())) {
				findViewById(R.id.next_step_btn).setEnabled(false);
				findViewById(R.id.next_step_btn)
						.setBackgroundDrawable(getResources().getDrawable(
								R.drawable.bg_login_btn_unable));
            } else {
				findViewById(R.id.next_step_btn).setEnabled(true);
				findViewById(R.id.next_step_btn)
						.setBackgroundDrawable(getResources().getDrawable(
								R.drawable.selector_login_btn));
			}
		}

	}

	public class WebService extends APIInterfaceInstance {

		@Override
		public void returnReisterSMSCheckSuccess(
				GetRegisterCheckResult getRegisterResult) {
			// TODO Auto-generated method stub
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			String registerId = getRegisterResult.getRegisterID();
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), PerfectInfoActivity.class);
			intent.putExtra("mobile", phoneNum);
			intent.putExtra("registerId", registerId);
			startActivity(intent);
			finish();
		}

		@Override
		public void returnReisterSMSCheckFail(String error,int errorCode) {
			// TODO Auto-generated method stub
			if (loadingDialog != null && loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					captchasEdit.setText(null);

				}
			};
			EasyDialog.showDialog(FillCaptchasActivity.this, getString(R.string.prompt), getString(R.string.login_captchas_input_error),
					getString(R.string.ok), listener, false);
			WebServiceMiddleUtils.hand(FillCaptchasActivity.this, error,errorCode);
		}

	}
}
