package com.inspur.mdm.api;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.emmcloud.util.AppUtils;
import com.inspur.mdm.bean.GetDeviceCheckResult;
import com.inspur.mdm.utils.MDMResUtils;
import com.inspur.mdm.utils.MDMUtils;

import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.ex.HttpException;
import org.xutils.http.RequestParams;
import org.xutils.x;

public class MDMApiService {
	private Activity context;
	private APIInterface apiInterface;
	private String baseUrl = "https://emm.inspur.com/api?";

	public MDMApiService(Activity context) {
		this.context = context;

	}

	public void setAPIInterface(APIInterface apiInterface) {
		this.apiInterface = apiInterface;
	}

	/**
	 * 设备检查
	 * @param tenantId
	 * @param userCode
	 */
	public void deviceCheck(String tenantId, String userCode) {
		// TODO Auto-generated method stub
		String module = "mdm";
		String method = "check_state";
		String completeUrl = baseUrl + "module=" + module + "&method=" + method;
		String uuid = AppUtils.getMyUUID(context);
		RequestParams params = new RequestParams(completeUrl);
		params.addBodyParameter("udid", uuid);
		params.addBodyParameter("tenant_id", tenantId);
		params.addBodyParameter("mdm_user_auth_type", "IDMUser");
		params.addBodyParameter("user_code", userCode);
		// params.addBodyParameter("app_mdm_id", "imp"); // 和ios约定的appid
		x.http().post(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				// TODO Auto-generated method stub
				Log.d("HttpUtil","result="+result);
					apiInterface.returnDeviceCheckSuccess(new GetDeviceCheckResult(
							result));
				
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				// TODO Auto-generated method stub
				String error = getError(ex);
				Log.d("HttpUtil","result="+error);
				apiInterface.returnDeviceCheckFail(error);
			}

			@Override
			public void onCancelled(CancelledException cex) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub

			}
		});

	}

	/**
	 * 注册设备
	 * @param userCode
	 * @param phoneNum
	 * @param mail
	 * @param remark
	 * @param deviceName
	 * @param tanentId
	 */
	public void deviceRegister(String userCode, String phoneNum, String mail,
			String remark, String deviceName, String tanentId) {
		String module = "mdm";
		String method = "device_register";
		String completeUrl = baseUrl + "module=" + module + "&method=" + method;
		String uuid = AppUtils.getMyUUID((Activity) context);
		RequestParams params = new RequestParams(completeUrl);
		// phone:1 pad:2
		int deviceType = 1;
		if (MDMUtils.isTablet(context)) {
			deviceType = 2;
		}
		params.addBodyParameter("user_code", userCode);
		params.addBodyParameter("udid", uuid);
		params.addBodyParameter("device_type", deviceType + "");
		params.addBodyParameter("device_model", android.os.Build.MODEL);
		params.addBodyParameter("os", "Android");
		params.addBodyParameter("os_version", android.os.Build.VERSION.RELEASE);
		params.addBodyParameter("resolution",
				MDMUtils.getDeviceResolution(context));
		params.addBodyParameter("device_name", deviceName);
		params.addBodyParameter("phone_number", phoneNum);
		params.addBodyParameter("email", mail);
		params.addBodyParameter("remark", remark);
		params.addBodyParameter("app_id", "Yun Plus");
		params.addBodyParameter("app_version",
				MDMUtils.getAppVersionCode(context));
		params.addBodyParameter("tenant_id", tanentId);
		params.addBodyParameter("mdm_user_auth_type", "IDMUser");
		x.http().post(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				// TODO Auto-generated method stub
					apiInterface
					.returnDeviceRegisterSuccess(new GetDeviceCheckResult(
							result));
				
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				// TODO Auto-generated method stub
				String error = getError(ex);
				apiInterface.returnDeviceRegisterFail(error);
			}

			@Override
			public void onCancelled(CancelledException cex) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub

			}
		});

	}

	private String getError(Throwable ex) {
		String errorMsg = "";
		String error = "";
		if (ex instanceof HttpException) {
			HttpException httpEx = (HttpException) ex;
			errorMsg = httpEx.getResult();
		}
		try {
			JSONObject jsonObject = new JSONObject(errorMsg);
			if (jsonObject.has("msg")) {
				error = jsonObject.getString("msg");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (TextUtils.isEmpty(error)) {
			error = context.getString(MDMResUtils
					.getStringID("net_request_failed"));
		}
		return error;
	}

}
