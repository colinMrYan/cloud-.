package com.inspur.mdm.ui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.inspur.mdm.MDM;
import com.inspur.mdm.api.APIInterfaceInstance;
import com.inspur.mdm.api.MDMApiService;
import com.inspur.mdm.bean.GetDeviceCheckResult;
import com.inspur.mdm.utils.MDMResUtils;
import com.inspur.mdm.utils.MDMUtils;
import com.inspur.mdm.widght.MDMLoadingDialog;

import java.util.List;

public class DeviceRegisterActivity extends MDMBaseActivity {

	private EditText deviceNameEdit, phoneNumEdit, mailEdit, remarkEdit;
	private boolean isDeviceNameNeed;
	private boolean isPhoneNumNeed;
	private boolean isEmailNeed;
	private boolean isRemarkNeed;
	private MDMLoadingDialog loadingDlg;
	private String userCode;
	private String tanentId;
	private String userName;
	private List<String> requireFieldList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(MDMResUtils.getLayoutID("mdm_activity_device_register"));
		getIntentData();
		initViews();
	}

	private void getIntentData() {
		// TODO Auto-generated method stub
		Bundle bundle = getIntent().getExtras().getBundle("bundle");
		userCode = bundle.getString("userCode");
		tanentId = bundle.getString("tanentId");
		userName = bundle.getString("userName");
		requireFieldList = bundle.getStringArrayList("requireFields");
	}

	private void initViews() {
		// TODO Auto-generated method stub
		deviceNameEdit = (EditText) findViewById(MDMResUtils
				.getWidgetID("device_name_edit"));
		phoneNumEdit = (EditText) findViewById(MDMResUtils
				.getWidgetID("phone_num_edit"));
		mailEdit = (EditText) findViewById(MDMResUtils.getWidgetID("mail_edit"));
		remarkEdit = (EditText) findViewById(MDMResUtils
				.getWidgetID("remark_edit"));
		loadingDlg = new MDMLoadingDialog(this);
		initDeviceRegisterUI();
	}

	/**
	 * 显示那些项是注册必填项
	 */
	private void initDeviceRegisterUI() {
		// TODO Auto-generated method stub
		String phoneNum = MDMUtils.getPhoneNum(this);
		if (phoneNum != null) {
			phoneNumEdit.setText(phoneNum);
		}

		if (userName.length() > 17) {
			userName = userName.substring(0, 16);
		}
		boolean isPad = MDMUtils.isTablet(this);
		if (isPad){
			deviceNameEdit.setText(String.format(getResources().getString(MDMResUtils.getStringID("_pad")),userName));;
		}else{
			deviceNameEdit.setText(String.format(getResources().getString(MDMResUtils.getStringID("_mobile")),userName));
		}
		Editable editable = deviceNameEdit.getText();
		int position = deviceNameEdit.getText().toString().length();
		Selection.setSelection(editable, position);
		if (requireFieldList.contains("device_name")) {
			isDeviceNameNeed = true;
			(findViewById(MDMResUtils
					.getWidgetID("device_name_img")))
					.setVisibility(View.VISIBLE);
		}

		if (requireFieldList.contains("phone_number")) {
			isPhoneNumNeed = true;
			(findViewById(MDMResUtils.getWidgetID("phone_num_img")))
					.setVisibility(View.VISIBLE);
		}

		if (requireFieldList.contains("email")) {
			isEmailNeed = true;
			(findViewById(MDMResUtils.getWidgetID("mail_img")))
					.setVisibility(View.VISIBLE);
		}

		if (requireFieldList.contains("remark")) {
			isRemarkNeed = true;
			(findViewById(MDMResUtils.getWidgetID("remark_img")))
					.setVisibility(View.VISIBLE);
		}

	}

	public void onClick(View v) {
		if (v.getId() == MDMResUtils.getWidgetID("regesiter_btn")) {
			String phoneNum = phoneNumEdit.getText().toString().trim();
			String mail = mailEdit.getText().toString().trim();
			String remark = remarkEdit.getText().toString().trim();
			String deviceName = deviceNameEdit.getText().toString().trim();
			if (isDeviceNameNeed && TextUtils.isEmpty(deviceName)) {
				Toast.makeText(getApplicationContext(),
						MDMResUtils.getStringID("device_name_cannot_null"),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (isPhoneNumNeed && TextUtils.isEmpty(phoneNum)) {
				Toast.makeText(getApplicationContext(),
						MDMResUtils.getStringID("phone_num_cannot_null"),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (isEmailNeed && TextUtils.isEmpty(mail)) {
				Toast.makeText(getApplicationContext(),
						MDMResUtils.getStringID("mail_cannot_null"),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (isRemarkNeed && TextUtils.isEmpty(remark)) {
				Toast.makeText(getApplicationContext(),
						MDMResUtils.getStringID("remark_cannot_null"),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (!TextUtils.isEmpty(phoneNum) && !MDMUtils.isMobileNum(phoneNum)) {
				Toast.makeText(getApplicationContext(),
						MDMResUtils.getStringID("telephone_input_illegal"),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (!TextUtils.isEmpty(mail) && !MDMUtils.isEmail(mail)) {
				Toast.makeText(getApplicationContext(),
						MDMResUtils.getStringID("email_input_illegal"),
						Toast.LENGTH_SHORT).show();
				return;
			}
			deviceRegister(phoneNum, mail, remark, deviceName);
		} else if (v.getId() == MDMResUtils.getWidgetID("device_detail_img")) {
			showUUIDDialog();
		} else if (v.getId() == MDMResUtils.getWidgetID("back_layout")) {
			onBackPressed();
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		new MDM().getMDMListener().MDMStatusNoPass();
	}

	/**
	 * 设备注册
	 * 
	 * @param phoneNum
	 * @param mail
	 * @param remark
	 * @param deviceName
	 */
	private void deviceRegister(String phoneNum, String mail, String remark,
			String deviceName) {
		// TODO Auto-generated method stub
		if (MDMUtils.isNetworkConnected(getApplicationContext())) {
			loadingDlg.show();
			MDMApiService apiService = new MDMApiService(
					DeviceRegisterActivity.this);
			apiService.setAPIInterface(new WebService());
			apiService.deviceRegister(userCode, phoneNum, mail, remark,
					deviceName, tanentId);
		}
	}

	private void showUUIDDialog() {
		// TODO Auto-generated method stub
		final String uuid = MDMUtils.getMyUUID(DeviceRegisterActivity.this);
		String title = getString(MDMResUtils.getStringID("uuid"))+":"+uuid;
		AlertDialog.Builder builder = new AlertDialog.Builder(this,
				android.R.style.Theme_Holo_Light_Dialog);

		builder.setMessage(title);
		builder.setPositiveButton(getString(MDMResUtils.getStringID("mdm_sure")), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				android.content.ClipboardManager c = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				c.setPrimaryClip(ClipData.newPlainText("clip",
						uuid));
				Toast.makeText(getApplicationContext(), MDMResUtils.getStringID("copyed_to_paste_board"), Toast.LENGTH_SHORT).show();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		dialog.setCancelable(true);
		dialog.show();

	}

	private class WebService extends APIInterfaceInstance {

		@Override
		public void returnDeviceRegisterFail(String error) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			Toast.makeText(DeviceRegisterActivity.this, error,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void returnDeviceRegisterSuccess(
				GetDeviceCheckResult getDeviceCheckResult) {
			// TODO Auto-generated method stub
			if (loadingDlg != null && loadingDlg.isShowing()) {
				loadingDlg.dismiss();
			}
			MDM mdm = new MDM(DeviceRegisterActivity.this, tanentId, userCode,
					userName, getDeviceCheckResult);
			if (!TextUtils.isEmpty(getDeviceCheckResult.getError())) {
				mdm.showRegisterFailDlg(getDeviceCheckResult.getError());
			} else {
				mdm.handCheckResult(getDeviceCheckResult.getState());
			}
		}

	}
}
