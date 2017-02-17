package com.inspur.emmcloud.ui.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.bean.GetUpdatePwdBySMSCodeBean;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

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
			Pattern pattern = Pattern.compile("^\\S{6,64}$");
			Matcher matcher = pattern.matcher(newPsd);
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
			if(!matcher.matches()){
				ToastUtils.show(FillNewPwdActivity.this, getString(R.string.modify_input_password));
				break;
			}
			if(StringUtils.isBlank(smsCode)){
				ToastUtils.show(FillNewPwdActivity.this, getString(R.string.captchas_input_error));
				break;
			}
			resetPwd(newPsd);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 修改密码方法
	 * @param newPsd
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
		newPwdEdit = (EditText) findViewById(R.id.new_password_edit);
		confirmPwdEdit = (EditText) findViewById(R.id.confirm_new_password_edit);
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
		public void returnUpdatePwdBySMSCodeFail(String error) {
			if(loadingDialog != null && loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
			WebServiceMiddleUtils.hand(FillNewPwdActivity.this, error);
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
