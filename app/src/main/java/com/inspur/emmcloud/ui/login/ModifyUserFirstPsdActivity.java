package com.inspur.emmcloud.ui.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModifyUserFirstPsdActivity extends BaseActivity {


	private Button confirmModifyButton;
	private LoginAPIService apiService;
	private LoadingDialog loadingDialog;
//	private ClearEditText oldpsdEdit;
	private ClearEditText newpsdEdit;
	private ClearEditText confirmpsdEdit;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_firstuserpsd);
		apiService = new LoginAPIService(ModifyUserFirstPsdActivity.this);
		apiService.setAPIInterface(new WebService());
		confirmModifyButton = (Button) findViewById(R.id.modifyuserpsd_button);
		loadingDialog = new LoadingDialog(ModifyUserFirstPsdActivity.this);
		
		newpsdEdit = (ClearEditText) findViewById(R.id.modifyuserpsd_new_edit);
		confirmpsdEdit = (ClearEditText) findViewById(R.id.modifyuserpsd_confirm_edit);
	}
	
	public void onClick(View v){
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.back_layout:
			finish();
			break;
		case R.id.headerright_text:
			intent.setClass(ModifyUserFirstPsdActivity.this, IndexActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.modifyuserpsd_button:
			
			String newpsd = newpsdEdit.getText().toString();
			String confirmpsd = confirmpsdEdit.getText().toString();
			Pattern pattern = Pattern.compile("^\\S{6,128}$");
			Matcher matcher = pattern.matcher(newpsd);
			if(TextUtils.isEmpty(newpsd)||TextUtils.isEmpty(confirmpsd)||!newpsd.equals(confirmpsd)){
				ToastUtils.show(ModifyUserFirstPsdActivity.this, getString(R.string.modify_user_password));
				break;

			}
			if (newpsd.length()<8 || newpsd.length()>128 ||!FomatUtils.isPasswrodStrong(newpsd) ){
				ToastUtils.show(MyApplication.getInstance(),R.string.modify_password_invalid);
				return;
			}
			if (NetUtils.isNetworkConnected(MyApplication.getInstance())){
				apiService.modifyPassword("", newpsd);
			}
			break;

		default:
			break;
		}
		
	}
	
	class WebService extends APIInterfaceInstance{

		@Override
		public void returnModifyPasswordSuccess() {
			// TODO Auto-generated method stub
			super.returnModifyPasswordSuccess();
			if(loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
//			Toast.makeText(ModifyUserFirstPsdActivity.this, "通行证密码修改成功，请使用新密码登录", Toast.LENGTH_SHORT).show();
			ToastUtils.show(ModifyUserFirstPsdActivity.this, getString(R.string.modify_user_password_success));
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClass(ModifyUserFirstPsdActivity.this, LoginActivity.class);
			ModifyUserFirstPsdActivity.this.startActivity(intent);
			ModifyUserFirstPsdActivity.this.finish();
		}

		@Override
		public void returnModifyPasswordFail(String error, int errorCode) {
			// TODO Auto-generated method stub
			WebServiceMiddleUtils.hand(ModifyUserFirstPsdActivity.this, error,errorCode);
			if(loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					switch (which) {
					case -1:
						LogUtils.debug("yfcLog", "继续修改");
						break;
					case -2:
						LogUtils.debug("yfcLog", "进入应用");
						Intent intent = new Intent();
						intent.setClass(ModifyUserFirstPsdActivity.this, IndexActivity.class);
						startActivity(intent);
						ModifyUserFirstPsdActivity.this.finish();
						break;

					default:
						break;
					}
				}
			};
			
			EasyDialog.showDialog(ModifyUserFirstPsdActivity.this, getString(R.string.prompt), 
					getString(R.string.modify_user_password_fail), 
					getString(R.string.ok), 
					getString(R.string.cancel), listener, false);
		}
		
	}
}
