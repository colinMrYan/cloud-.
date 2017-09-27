package com.inspur.emmcloud.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModifyUserPsdActivity extends BaseActivity {


	private Button confirmModifyButton;
	private LoginAPIService apiService;
	private LoadingDialog loadingDialog;
	private ClearEditText oldpsdEdit;
	private ClearEditText newpsdEdit;
	private ClearEditText confirmpsdEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_userpsd);
		apiService = new LoginAPIService(ModifyUserPsdActivity.this);
		apiService.setAPIInterface(new WebService());
		confirmModifyButton = (Button) findViewById(R.id.modifyuserpsd_button);
		loadingDialog = new LoadingDialog(ModifyUserPsdActivity.this);
		
		oldpsdEdit = (ClearEditText) findViewById(R.id.modifyuserpsd_old_edit);
		newpsdEdit = (ClearEditText) findViewById(R.id.modifyuserpsd_new_edit);
		confirmpsdEdit = (ClearEditText) findViewById(R.id.modifyuserpsd_confirm_edit);
		confirmModifyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String oldpsd = oldpsdEdit.getText().toString();
				String newpsd = newpsdEdit.getText().toString();
				String confirmpsd = confirmpsdEdit.getText().toString();
				Pattern pattern = Pattern.compile("^\\S{6,128}$");
				Matcher matcher = pattern.matcher(newpsd);
				if(TextUtils.isEmpty(oldpsd)){
					Toast.makeText(ModifyUserPsdActivity.this, getString(R.string.modify_input_old_password), Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(TextUtils.isEmpty(newpsd)){
					Toast.makeText(ModifyUserPsdActivity.this, getString(R.string.modify_input_user_new_password), Toast.LENGTH_SHORT).show();
					return;
				}
				if(TextUtils.isEmpty(confirmpsd)){
					Toast.makeText(ModifyUserPsdActivity.this, getString(R.string.modify_input_confirm_passord), Toast.LENGTH_SHORT).show();
					return;
				}
				if(newpsd.equals(oldpsd)){
					Toast.makeText(ModifyUserPsdActivity.this, getString(R.string.modify_new_old_same), Toast.LENGTH_SHORT).show();
					return;
				}
				if(!newpsd.equals(confirmpsd)){
					Toast.makeText(ModifyUserPsdActivity.this, getString(R.string.modify_not_same), Toast.LENGTH_SHORT).show();
					return;
				}
				if(!TextUtils.isEmpty(newpsd)&&matcher.matches()&&!TextUtils.isEmpty(confirmpsd)&&NetUtils.isNetworkConnected(ModifyUserPsdActivity.this)){
					loadingDialog.show();
					apiService.changePsd(oldpsd, newpsd);
				}else if(!TextUtils.isEmpty(newpsd)&&!matcher.matches()){
					Toast.makeText(ModifyUserPsdActivity.this, getString(R.string.modify_input_password), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	public void onClick(View v){
		finish();
	}
	
	class WebService extends APIInterfaceInstance{

		@Override
		public void returnModifyPsdSuccess() {
			super.returnModifyPsdSuccess();
			if(loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClass(ModifyUserPsdActivity.this, LoginActivity.class);
			ModifyUserPsdActivity.this.startActivity(intent);
			((Activity) ModifyUserPsdActivity.this).finish();
		}

		@Override
		public void returnModifyPsdFail(String error,int errorCode) {
			if(loadingDialog.isShowing()){
				loadingDialog.dismiss();
			}
			String errCode = "";
			JSONObject jsonObject = null ;
			try {
				jsonObject = new JSONObject(error);
				if(jsonObject.has("status")){
					errCode = jsonObject.getString("status");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if(errCode.contains("400")){
				Toast.makeText(ModifyUserPsdActivity.this, getString(R.string.modify_password_not_correct), Toast.LENGTH_SHORT).show();
			}else {
				Toast.makeText(ModifyUserPsdActivity.this, getString(R.string.modify_password_fail), Toast.LENGTH_SHORT).show();
			}
			
		}
	}
}
