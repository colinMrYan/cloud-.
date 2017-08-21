package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.AppException;
import com.inspur.emmcloud.bean.GetExceptionResult;
import com.inspur.emmcloud.util.AppExceptionCacheUtils;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.UriUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2017/5/3.
 */

public class AppExceptionService extends Service {
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		uploadException();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void uploadException() {
		if (NetUtils.isNetworkConnected(AppExceptionService.this,false) && !AppUtils.isApkDebugable(AppExceptionService.this)) {
			List<AppException> appExceptionList = AppExceptionCacheUtils.getAppExceptionList(AppExceptionService.this);
			if (appExceptionList.size() != 0) {
				JSONObject uploadContentJSONObj = getUploadContentJSONObj(appExceptionList);
				AppAPIService apiService = new AppAPIService(AppExceptionService.this);
				apiService.setAPIInterface(new WebService());
				apiService.uploadException(uploadContentJSONObj);
				return;
			}
		}
		stopSelf();
	}

	/**
	 * 组织异常数据
	 *
	 * @param appExceptionList
	 * @return
	 */
	private JSONObject getUploadContentJSONObj(List<AppException> appExceptionList) {
		JSONObject contentObj = new JSONObject();
		try {
			contentObj.put("AppID", 1);
			contentObj.put("UserCode", PreferencesUtils.getString(AppExceptionService.this, "userID", ""));
			if (UriUtils.tanent != null) {
				contentObj.put("EnterpriseCode", UriUtils.tanent);
			} else {
				contentObj.put("EnterpriseCode", "");
			}
			contentObj.put("DeviceOS", "Android");
			contentObj.put("DeviceOSVersion ", android.os.Build.VERSION.RELEASE);
			contentObj.put("DeviceModel", android.os.Build.MODEL);

			JSONArray errorDataArray = new JSONArray();
			for (int i = 0; i < appExceptionList.size(); i++) {
				errorDataArray.put(appExceptionList.get(i).toJSONObject());
			}
			contentObj.put("ErrorData", errorDataArray);
		} catch (Exception e) {
			e.printStackTrace();
		}


		return contentObj;
	}

	private class WebService extends APIInterfaceInstance {
		@Override
		public void returnUploadExceptionSuccess(
				GetExceptionResult getExceptionResult) {
			AppExceptionCacheUtils.clearAppException(AppExceptionService.this);
			stopSelf();
		}

		@Override
		public void returnUploadExceptionFail(String error,int errorCode) {
			stopSelf();
		}
	}
}
