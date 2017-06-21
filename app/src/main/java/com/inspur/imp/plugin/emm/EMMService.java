package com.inspur.imp.plugin.emm;

import android.os.Build;

import com.inspur.emmcloud.bean.GetDeviceCheckResult;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.MDM.MDM;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ResolutionUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONObject;


/**
 * 应用相关本地功能类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class EMMService extends ImpPlugin {

	@Override
	public void execute(String action, JSONObject paramsObject) {
		LogUtils.jasonDebug("action="+action);
		if ("returnEMMstate".equals(action)) {
			returnEMMstate(paramsObject);
		}

		if ("webviewReload".equals(action)){
			webviewReload();
		}
	}

	@Override
	public String executeAndReturn(String action, JSONObject paramsObject) {
		LogUtils.jasonDebug("action="+action);
		if ("getDeviceInfo".equals(action)) {
			return getDeviceInfo();
		}
		return super.executeAndReturn(action, paramsObject);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
	}


	/**
	 * 获取EMM设备信息
	 *
	 * @return
	 */
	private String getDeviceInfo() {
		JSONObject obj = new JSONObject();
		try {
			int deviceType = AppUtils.isTablet(context) ? 2 : 1;
			obj.put("device_type", deviceType);
			obj.put("device_model", Build.MODEL);
			obj.put("os", "Android");
			obj.put("os_version", Build.VERSION.RELEASE);
			obj.put("resolution", ResolutionUtils.getResolution(getActivity()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj.toString();
	}

	private void webviewReload(){
		webview.reload();
	}


	/**
	 * 返回设备检查状态
	 *
	 * @param paramsObject
	 */
	private void returnEMMstate(JSONObject paramsObject) {
		try {
			if (paramsObject.has("EMMState")) {
				String state = paramsObject.getString("EMMState");
				GetDeviceCheckResult getDeviceCheckResult = new GetDeviceCheckResult(state);
				String userName = PreferencesUtils.getString(getActivity(), "userRealName", "");
				String userCode = PreferencesUtils.getString(getActivity(), "userID", "");
				MDM mdm = new MDM(getActivity(), UriUtils.tanent, userCode,
						userName, getDeviceCheckResult);
				mdm.handCheckResult(getDeviceCheckResult.getState());
			}
		} catch (Exception e) {
			e.printStackTrace();
			ToastUtils.show(getActivity(), "设备检查信息异常");
		}

	}
}
