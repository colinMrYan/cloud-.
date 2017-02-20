package com.inspur.mdm;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.inspur.emmcloud.util.LogUtils;
import com.inspur.mdm.api.APIInterfaceInstance;
import com.inspur.mdm.api.MDMApiService;
import com.inspur.mdm.bean.GetDeviceCheckResult;
import com.inspur.mdm.ui.DeviceRegisterActivity;
import com.inspur.mdm.ui.DeviceRegisterFailDetailActivity;
import com.inspur.mdm.utils.MDMResUtils;
import com.inspur.mdm.utils.MDMUtils;
import com.inspur.mdm.widght.MDMLoadingDialog;

public class MDM extends APIInterfaceInstance {
	private static final int STATUS_NOT_REGISTERED = 0;
	private static final int STATUS_NORMAL = 1;
	private static final int STATUS_DISABLE = 2;
	private static final int STATUS_WAITING_VERIFY = 3;
	private static final int STATUS_REVIEW_REJECT = 4;
	private static final int STATUS_IN_BLACKLIST = 5;

	private Activity context;
	private GetDeviceCheckResult getDeviceCheckResult;
	private String tanentId;
	private String userCode;
	private String userName;
	private ArrayList<String> requireFieldList;
	private static MDMListener mdmListener;
	public MDM() {

	}

	public MDM(Activity context, String tanentId, String userCode,
			String userName) {
		this.context = context;
		this.tanentId = tanentId;
		this.userCode = userCode;
		this.userName = userName;
	}

	public MDM(Activity context, String tanentId, String userCode,
			String userName, GetDeviceCheckResult getDeviceCheckResult) {
		this.userName = userName;
		this.context = context;
		this.tanentId = tanentId;
		this.userCode = userCode;
		this.getDeviceCheckResult = getDeviceCheckResult;
	}

	public void addOnMDMListener(MDMListener mdmListener) {
		this.mdmListener = mdmListener;
	}

	public MDMListener getMDMListener() {
		return mdmListener;
	}

	/** 处理设备检查结果 **/
	public void handCheckResult(int deviceStatus) {
		// TODO Auto-generated method stub
		switch (deviceStatus) {
		case STATUS_NORMAL:
			if (mdmListener != null) {
				mdmListener.MDMStatusPass();
			}
			if (context instanceof DeviceRegisterActivity) {
				context.finish();
			}
			break;
		case STATUS_DISABLE:
			showWraningDlg(STATUS_DISABLE);
			break;
		case STATUS_WAITING_VERIFY:
			showWraningDlg(STATUS_WAITING_VERIFY);
			break;
		case STATUS_NOT_REGISTERED:
			goDeviceRegister();
			Toast.makeText(context, MDMResUtils.getStringID("device_not_register"),
					Toast.LENGTH_SHORT).show();
			break;
		case STATUS_IN_BLACKLIST:
			showWraningDlg(STATUS_IN_BLACKLIST);
			break;
		case STATUS_REVIEW_REJECT:
			goDeviceRegisterFailDetail();
			break;
		default:
			break;
		}
	}

	/**
	 * 跳转到设备注册页面
	 * 
	 * @param userName
	 */
	private void goDeviceRegister() {
		// TODO Auto-generated method stub
		if (mdmListener != null) {
			mdmListener.dimissExternalLoadingDlg();
		}
		Intent intent = new Intent();
		intent.setClass(context, DeviceRegisterActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("userCode", userCode);
		bundle.putString("tanentId", tanentId);
		bundle.putString("userName", userName);
		bundle.putStringArrayList("requireFields", requireFieldList);
		intent.putExtra("bundle", bundle);
		context.startActivity(intent);
	}

	private void goDeviceRegisterFailDetail() {
		// TODO Auto-generated method stub
		if (mdmListener != null) {
			mdmListener.dimissExternalLoadingDlg();
		}
		Intent intent = new Intent();
		intent.setClass(context, DeviceRegisterFailDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("userCode", userCode);
		bundle.putString("tanentId", tanentId);
		bundle.putString("userName", userName);
		bundle.putString("message", getDeviceCheckResult.getMessage());
		bundle.putStringArrayList("requireFields", requireFieldList);
		intent.putExtra("bundle", bundle);
		context.startActivity(intent);
		if (context instanceof DeviceRegisterActivity) {
			context.finish();
		}
	}

	/**
	 * 弹出提示框（设备被禁用或设备正在审核中）
	 * 
	 * @param status
	 */
	private void showWraningDlg(final int status) {
		String title = "";
		if (status == STATUS_DISABLE) {
			title = context.getString(MDMResUtils.getStringID("device_disabled_cannot_login"));
		} else if (status == STATUS_WAITING_VERIFY) {
			if (context instanceof DeviceRegisterActivity) {
				title = context
						.getString(MDMResUtils.getStringID("register_submit_waitting_verify"));
			} else {
				title = context.getString(MDMResUtils.getStringID("device_waitting_verify"));
			}
		} else {
			title = context.getString(MDMResUtils.getStringID("device_in_blacklist"));
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(context,
				android.R.style.Theme_Holo_Light_Dialog);

		builder.setTitle(context.getString(MDMResUtils.getStringID("mdm_tips")));
		builder.setMessage(title);
		builder.setPositiveButton(context.getString(MDMResUtils.getStringID("mdm_sure")),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (context instanceof DeviceRegisterActivity) {
							context.finish();
						}
						if (mdmListener != null) {
							mdmListener.MDMStatusNoPass();
						}
					}
				});

		AlertDialog dialog = builder.create();
		dialog.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		dialog.setCancelable(false);
		dialog.show();
	}

	/**
	 * 弹出设备注册失败提示框
	 */
	public void showRegisterFailDlg(String errorMsg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context,
				android.R.style.Theme_Holo_Light_Dialog);

		builder.setTitle(context.getString(MDMResUtils.getStringID("mdm_tips")));
		builder.setMessage(errorMsg);
		builder.setPositiveButton(context.getString(MDMResUtils.getStringID("mdm_ok")), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (!(context instanceof DeviceRegisterActivity)) {
					if (mdmListener != null) {
						mdmListener.dimissExternalLoadingDlg();
					}
					Intent intent = new Intent();
					intent.setClass(context, DeviceRegisterActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("userCode", userCode);
					bundle.putString("tanentId", tanentId);
					bundle.putStringArrayList("requireFields", requireFieldList);
					intent.putExtras(bundle);
					context.startActivity(intent);
					context.finish();
				}
			}
		});
		builder.setNegativeButton(context.getString(MDMResUtils.getStringID("mdm_cancel")), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mdmListener != null) {
					mdmListener.MDMStatusNoPass();
				}
			}
		});

		AlertDialog dialog = builder.create();
		dialog.getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		dialog.setCancelable(false);
		dialog.show();
	}

	/**
	 * 设备检查
	 */
	public void deviceCheck() {
		if (MDMUtils.isNetworkConnected(context)) {
			MDMApiService apiServices = new MDMApiService(context);
			apiServices.setAPIInterface(MDM.this);
			apiServices.deviceCheck(tanentId, userCode);
		}else if (mdmListener != null) {
			mdmListener.MDMStatusNoPass();
		}
	}

	@Override
	public void returnDeviceCheckSuccess(
			GetDeviceCheckResult getDeviceCheckResult) {
		// TODO Auto-generated method stub
		this.getDeviceCheckResult = getDeviceCheckResult;
		if (!TextUtils.isEmpty(getDeviceCheckResult.getError())) {
			showRegisterFailDlg(getDeviceCheckResult.getError());
		} else {
			requireFieldList = getDeviceCheckResult.getRequiredFieldList();
			handCheckResult(getDeviceCheckResult.getState());
		}

	}

	@Override
	public void returnDeviceCheckFail(String error) {
		// TODO Auto-generated method stub
		if (mdmListener != null) {
			mdmListener.MDMStatusNoPass();
		}
		Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
	}

}
