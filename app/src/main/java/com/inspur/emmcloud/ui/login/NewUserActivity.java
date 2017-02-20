package com.inspur.emmcloud.ui.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.bean.GetRegisterResult;
import com.inspur.emmcloud.ui.mine.setting.ServiceTermActivity;
import com.inspur.emmcloud.util.InputMethodUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

/**
 * 新用户注册页面
 * 
 * @author Administrator
 *
 */
public class NewUserActivity extends BaseActivity {

//	private ClearEditText phoneNumEdit;
//	private LoadingDialog loadingDialog;
//	private static final int GET_CODE_FAIL = 2;
//	// APIService apiService;
//	// String token;
//	// String verificationCode;
//	private String smsCode;
//	private String mobile;
//
//	Handler handler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
//			switch (msg.what) {
//			case GET_CODE_FAIL:
////				EasyDialog.showDialog(NewUserActivity.this,
////						getString(R.string.prompt),
////						getString(R.string.login_timeout),
////						getString(R.string.ok), null, false);
//				break;
//
//			default:
//				break;
//			}
//		}
//
//	};
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//		((MyApplication) getApplicationContext())
//				.addActivity(NewUserActivity.this);
//		setContentView(R.layout.activity_new_user);
//		phoneNumEdit = (ClearEditText) findViewById(R.id.phone_num_edit);
//		phoneNumEdit.addTextChangedListener(new EditWatcher());
//		InputMethodUtils.display(NewUserActivity.this, phoneNumEdit);
//		((RelativeLayout) findViewById(R.id.main_layout))
//				.setOnTouchListener(new OnTouchListener() {
//
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						// TODO Auto-generated method stub
//						InputMethodUtils.hide(NewUserActivity.this);
//						return false;
//					}
//				});
//		loadingDialog = new LoadingDialog(NewUserActivity.this);
//	}
//
//	public void onClick(View v) {
//		Intent intent = new Intent();
//		switch (v.getId()) {
//		case R.id.back_layout:
//			finish();
//
//			break;
//		case R.id.service_term_text:
//			intent.setClass(getApplicationContext(), ServiceTermActivity.class);
//			startActivity(intent);
//
//			break;
//		case R.id.next_step_btn:
//			ToastUtils.show(NewUserActivity.this, R.string.function_not_implemented);
////			mobile = phoneNumEdit.getText().toString();
////			if (StringUtils.isBlank(mobile)) {
////				ToastUtils.show(getApplicationContext(),
////						getString(R.string.phone_num_cannot_null));
////				return;
////			}
////			if (!FomatUtils.isPhoneNum(mobile)) {
////				ToastUtils.show(getApplicationContext(),
////						getString(R.string.phone_num_illegal_format));
////				return;
////			}
////
////			if (NetUtils.isNetworkConnected(NewUserActivity.this)) {
////				loadingDialog.show();
////
////				APIService apiService = new APIService(NewUserActivity.this);
////				apiService.setAPIInterface(new WebService());
////				apiService.SMSRegister(mobile);
////
////			}
//
//			break;
//
//		default:
//			break;
//		}
//	}
//
//	/**
//	 * 检测手机号输入
//	 *
//	 */
//	private class EditWatcher implements TextWatcher {
//
//		@Override
//		public void beforeTextChanged(CharSequence s, int start, int count,
//				int after) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void onTextChanged(CharSequence s, int start, int before,
//				int count) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void afterTextChanged(Editable s) {
//			// TODO Auto-generated method stub
//			if (phoneNumEdit.getText().toString().length() >= 11) {
//				((Button) findViewById(R.id.next_step_btn)).setEnabled(true);
//				((Button) findViewById(R.id.next_step_btn))
//						.setBackgroundDrawable(getResources().getDrawable(
//								R.drawable.selector_login_btn));
//			} else {
//				((Button) findViewById(R.id.next_step_btn)).setEnabled(false);
//				((Button) findViewById(R.id.next_step_btn))
//						.setBackgroundDrawable(getResources().getDrawable(
//								R.drawable.bg_login_btn_unable));
//			}
//		}
//
//	}
//
//	public class WebService extends APIInterfaceInstance {
//
//		@Override
//		public void returnRegisterSMSSuccess(GetRegisterResult getRegisterResult) {
//			// TODO Auto-generated method stub
//			if (loadingDialog != null && loadingDialog.isShowing()) {
//				loadingDialog.dismiss();
//			}
//			LogUtils.debug("smsRegister--------->", getRegisterResult.getCode());
//			smsCode = getRegisterResult.getCode();
//			LogUtils.debug("smsRegisterInservice--------->", mobile);
//
//			if (smsCode.equals("1")) {
//				Intent intent = new Intent();
//				intent.setClass(getApplicationContext(),
//						FillCaptchasActivity.class);
//				intent.putExtra("phoneNum", mobile);
//
//				intent.putExtra("smsCode", smsCode);
//				startActivity(intent);
//				finish();
//			} else {
//				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//						dialog.dismiss();
//						if (which == -1) {
//							Intent intent = new Intent();
//							intent.setClass(getApplicationContext(),
//									CaptchasLoginActivity.class);
//							intent.putExtra("phoneNum", mobile);
//
//							startActivity(intent);
//							finish();
//						} else {
//							phoneNumEdit.setText(null);
//						}
//					}
//				};
//				EasyDialog.showDialog(NewUserActivity.this,
//						getString(R.string.prompt),
//						getString(R.string.phonenum_has_beeb_resgistered),
//						getString(R.string.login_direct),
//						getString(R.string.change_phone_num), listener, false);
//			}
//
//		}
//
//		@Override
//		public void returnRegisterSMSFail(String error) {
//			// TODO Auto-generated method stub
//			if (loadingDialog != null && loadingDialog.isShowing()) {
//				loadingDialog.dismiss();
//			}
//			WebServiceMiddleUtils.hand(NewUserActivity.this, error, handler,
//					GET_CODE_FAIL);
//
//		}
//
//	}
//
//
//	@Override
//	protected void onDestroy() {
//		// TODO Auto-generated method stub
//		super.onDestroy();
//		if (handler != null) {
//			handler = null;
//		}
//	}

}
