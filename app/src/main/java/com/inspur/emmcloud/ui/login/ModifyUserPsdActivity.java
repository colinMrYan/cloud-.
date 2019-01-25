package com.inspur.emmcloud.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.LoginAPIService;
import com.inspur.emmcloud.util.common.FomatUtils;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.widget.ClearEditText;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.keyboardview.EmmSecurityKeyboard;

import org.json.JSONException;
import org.json.JSONObject;

public class ModifyUserPsdActivity extends BaseActivity {


	private Button confirmModifyButton;
	private LoginAPIService apiService;
	private LoadingDialog loadingDialog;
	private ClearEditText oldpsdEdit;
	private ClearEditText newpsdEdit;
	private ClearEditText confirmpsdEdit;
	private EmmSecurityKeyboard emmSecurityKeyboard;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_userpsd);
		init();
	}

	private void init() {
		apiService = new LoginAPIService(ModifyUserPsdActivity.this);
		apiService.setAPIInterface(new WebService());
		confirmModifyButton =  findViewById(R.id.modifyuserpsd_button);
		loadingDialog = new LoadingDialog(ModifyUserPsdActivity.this);
		emmSecurityKeyboard = new EmmSecurityKeyboard(this);
		oldpsdEdit =  findViewById(R.id.modifyuserpsd_old_edit);
		newpsdEdit = findViewById(R.id.modifyuserpsd_new_edit);
		confirmpsdEdit = findViewById(R.id.modifyuserpsd_confirm_edit);
		addListeners();
	}

	private void addListeners() {
		confirmModifyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String oldpsd = oldpsdEdit.getText().toString();
				String newpsd = newpsdEdit.getText().toString();
				String confirmpsd = confirmpsdEdit.getText().toString();
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

				if (newpsd.length()<8 || newpsd.length()>64 ||!FomatUtils.isPasswrodStrong(newpsd) ){
					ToastUtils.show(MyApplication.getInstance(),R.string.modify_password_invalid);
					return;
				}
				changePsw(oldpsd,newpsd);
			}
		});
		EditOnTouchListener editOnTouchListener = new EditOnTouchListener();
		oldpsdEdit.setOnTouchListener(editOnTouchListener);
		newpsdEdit.setOnTouchListener(editOnTouchListener);
		confirmpsdEdit.setOnTouchListener(editOnTouchListener);
	}

	class EditOnTouchListener implements View.OnTouchListener{

		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (view.getId()){
				case R.id.modifyuserpsd_old_edit:
					emmSecurityKeyboard.showSecurityKeyBoard(oldpsdEdit);
					break;
				case R.id.modifyuserpsd_new_edit:
					emmSecurityKeyboard.showSecurityKeyBoard(newpsdEdit);
					break;
				case R.id.modifyuserpsd_confirm_edit:
					emmSecurityKeyboard.showSecurityKeyBoard(confirmpsdEdit);
					break;
			}
			return false;
		}
	}

	public void onClick(View v){
		finish();
	}

	private void changePsw(String oldpsd,String newpsd){
		if (NetUtils.isNetworkConnected(MyApplication.getInstance())){
			loadingDialog.show();
			apiService.changePsd(oldpsd, newpsd);
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
			ModifyUserPsdActivity.this.finish();
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
