package com.inspur.emmcloud.util;

import android.content.Context;

import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.bean.AppException;
import com.inspur.emmcloud.bean.GetExceptionResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2017/5/3.
 */

public class UploadExceptionUtils {
	private Context mContext;

	public UploadExceptionUtils(Context context) {
		this.mContext = context;
	}

	public void upload() {
		if (NetUtils.isNetworkConnected(mContext,false)&&!AppUtils.isApkDebugable(mContext)) {
			List<AppException> appExceptionList = AppExceptionCacheUtils.getAppExceptionList(mContext);
			if (appExceptionList.size() == 0) {
				return;
			}
			JSONObject uploadContentJSONObj= getUploadContentJSONObj(appExceptionList);
			AppAPIService apiService = new AppAPIService(mContext);
			apiService.setAPIInterface(new WebService());
			apiService.uploadException(uploadContentJSONObj);
		}
	}

	/**
	 * 组织异常数据
	 * @param appExceptionList
	 * @return
	 */
	private JSONObject getUploadContentJSONObj(List<AppException> appExceptionList) {
		JSONObject contentObj = new JSONObject();
		try {
			contentObj.put("AppID", 1);
			contentObj.put("UserCode", PreferencesUtils.getString(mContext, "userID",""));
			if (UriUtils.tanent != null){
				contentObj.put("EnterpriseCode",UriUtils.tanent);
			}else {
				contentObj.put("EnterpriseCode","");
			}
			contentObj.put("DeviceOS","Android");
			contentObj.put("DeviceOSVersion ",android.os.Build.VERSION.RELEASE);
			contentObj.put("DeviceModel",android.os.Build.MODEL);

			JSONArray errorDataArray = new JSONArray();
			for (int i=0;i<appExceptionList.size();i++){
				errorDataArray.put(appExceptionList.get(i).toJSONObject());
			}
			contentObj.put("ErrorData",errorDataArray);
		} catch (Exception e) {
			e.printStackTrace();
		}


		return contentObj;
	}

	private class WebService extends APIInterfaceInstance {
		@Override
		public void returnUploadExceptionSuccess(
				GetExceptionResult getExceptionResult) {
			AppExceptionCacheUtils.clearAppException(mContext);
		}

		@Override
		public void returnUploadExceptionFail(String error,int errorCode) {
		}
	}

}
