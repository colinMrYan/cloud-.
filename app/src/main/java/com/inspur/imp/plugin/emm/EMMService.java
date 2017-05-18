package com.inspur.imp.plugin.emm;

import android.os.Build;
import android.text.TextUtils;

import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.mdm.MDM;
import com.inspur.mdm.bean.GetDeviceCheckResult;
import com.inspur.mdm.utils.MDMUtils;

import org.json.JSONObject;


/**
 * 应用相关本地功能类
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class EMMService extends ImpPlugin {

	@Override
	public void execute(String action, JSONObject paramsObject) {
		if ("returnEMMstate".equals(action)){
			String state = "";
			returnEMMstate(state);
		}
	}

	@Override
	public String executeAndReturn(String action, JSONObject paramsObject) {
		if ("getEMMInfo".equals(action)){
			return getEMMInfo();
		}
		return super.executeAndReturn(action, paramsObject);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
	};

	/**
	 * 获取EMM设备信息
	 * @return
	 */
	private String getEMMInfo(){
		JSONObject obj = new JSONObject();
		try {
			int deviceType = AppUtils.isTablet(context)?2:1;
			obj.put("device_type",deviceType);
			obj.put("device_model", Build.MODEL);
			obj.put("os", "Android");
			obj.put("os_version", Build.VERSION.RELEASE);
			obj.put("resolution", MDMUtils.getDeviceResolution(context));
		}catch (Exception e){
			e.printStackTrace();
		}
		return  obj.toString();
	}


	private void returnEMMstate(String state){
		GetDeviceCheckResult getDeviceCheckResult = new GetDeviceCheckResult(state);
		String userName = PreferencesUtils.getString(getActivity(), "userRealName", "");
		String userCode = PreferencesUtils.getString(getActivity(), "userID", "");
		MDM mdm = new MDM(getActivity(), UriUtils.tanent, userCode,
				userName,getDeviceCheckResult);
		if (!TextUtils.isEmpty(getDeviceCheckResult.getError())) {
			mdm.showRegisterFailDlg(getDeviceCheckResult.getError());
		} else {
			mdm.handCheckResult(getDeviceCheckResult.getState());
		}
	}
}
