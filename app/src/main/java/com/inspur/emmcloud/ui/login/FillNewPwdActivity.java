package com.inspur.emmcloud.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.login.GetUpdatePwdBySMSCodeBean;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.keyboardview.EmmSecurityKeyboard;

/**
 * 填写重置密码的新密码
 *
 */
public class FillNewPwdActivity extends BaseActivity{

	private EditText newPwdEdit;
	private EditText confirmPwdEdit;
	private LoginAPIService apiService;
	private LoadingDialog loadingDialog;
	private String smsCode = "";
	private EmmSecurityKeyboard emmSecurityKeyboard;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fill_new_pwd);
		initViews();
	}
	
	public void onClick(View view){
		switch (view.getId()) {
		case R.id.back_layout:
			finish();
			break;
		case R.id.modify_password_confirm_btn:
			String newPsd = newPwdEdit.getText().toString();
			String confirmPsd = confirmPwdEdit.getText().toString();
			if(StringUtils.isBlank(newPsd)){
				ToastUtils.show(FillNewPwdActivity.this, getString(R.string.modify_input_user_new_password));
				break;
			}
			if(StringUtils.isBlank(newPsd)){
				ToastUtils.show(FillNewPwdActivity.this, getString(R.string.modify_input_confirm_passord));
				break;
			}
			if(!newPsd.equals(confirmPsd)){
				ToastUtils.show(FillNewPwdActivity.this, getString(R.string.modify_not_same));
				break;
			}
			if (newPsd.length()<8 || newPsd.length()>64 ||!FomatUtils.isPasswrodStrong(newPsd) ){
				ToastUtils.show(MyApplication.getInstance(),R.string.modify_password_invalid);
				break;
			}
			if(StringUtils.isBlank(smsCode)){
				ToastUtils.show(FillNewPwdActivity.this, getString(R.string.login_captchas_input_error));
				break;
			}
			resetPwd(newPsd);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(emmSecurityKeyboard != null){
			emmSecurityKeyboard.dismiss();
			emmSecurityKeyboard = null;
		}
	}

	/**
	 * 修改密码方法
	 * @param newPwd
	 */
	private void resetPwd(String newPwd) {
		if(NetUtils.isNetworkConnected(FillNewPwdActivity.this)){
			loadingDialog.show();
			apiService.updatePwdBySMSCode(smsCode, newPwd);
		}
	}

	/**
	 * 初始化Views
	 */
	private void initViews() {
		smsCode = getIntent().getStringExtra("smsCode");
		apiService = new LoginAPIService(FillNewPwdActivity.this);
		apiService.setAPIInterface(new WebService());
		loadingDialog = new LoadingDialog(FillNewPwdActivity.this);
		newPwdEdit = findViewById(R.id.new_password_edit);
		confirmPwdEdit =  findViewById(R.id.confirm_new_password_edit);
		emmSecurityKeyboard = new EmmSecurityKeyboard(this);
		EditOnTouchListener editOnTouchListener = new EditOnTouchListener();
		newPwdEdit.setOnTouchListener(editOnTouchListener);
		confirmPwdEdit.setOnTouchListener(editOnTouchListener);
	}

	class EditOnTouchListener implements View.OnTouchListener{
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (view.getId()){
				case R.id.new_password_edit:
					emmSecurityKeyboard.showSecurityKeyBoard(newPwdEdit);
					break;
				case R.id.confirm_new_password_edit:
					emmSecurityKeyboard.showSecurityKeyBoard(confirmPwdEdit);
					break;
			}
			return false;
		}
	}
	
	class WebService extends APIInterfaceInstance{
		@Override
		public void returnUpdatePwdBySMSCodeSuccess(
				GetUpdatePwdBySMSCodeBean getUpdatePwdBySMSCodeBean) {
			if(loadingDialog != null && loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
			reLogin();
		}
		
		@Override
		public void returnUpdatePwdBySMSCodeFail(String error,int errorCode) {
			if(loadingDialog != null && loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
			WebServiceMiddleUtils.hand(FillNewPwdActivity.this, error,errorCode);
		}
	}

	/**
	 * 重新登录
	 */
	private void reLogin() {
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClass(FillNewPwdActivity.this, LoginActivity.class);
		FillNewPwdActivity.this.startActivity(intent);
		finish();
	}
}
