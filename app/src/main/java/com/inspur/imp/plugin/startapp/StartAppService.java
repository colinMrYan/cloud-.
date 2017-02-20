/**
 * 
 * StartAppService.java
 * classes : com.inspur.imp.plugin.startapp.StartAppService
 * V 1.0.0
 * Create at 2016年9月18日 上午9:57:30
 */
package com.inspur.imp.plugin.startapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.inspur.emmcloud.util.LogUtils;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.util.StrUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * com.inspur.imp.plugin.startapp.StartAppService create at 2016年9月18日 上午9:57:30
 */
public class StartAppService extends ImpPlugin {

	@Override
	public String executeAndReturn(String action, JSONObject paramsObject) {
		// TODO Auto-generated method stub
		if ("getIsAppInstall".equals(action)) {
			return getIsAppInstall(paramsObject);
		}
		return "";
	}

	/**
	 * 检查应用是否已安装
	 * 
	 * @param paramsObject
	 * @return
	 */
	private String getIsAppInstall(JSONObject paramsObject) {
		// TODO Auto-generated method stub
		Log.d("jason", "getIsAppInstall--------------");
		String result = "";
		try {
			Log.d("jason", "paramsObject="+paramsObject.toString());
			JSONArray packageNameArray = paramsObject.getJSONArray("packageNameArray");
			JSONObject obj = new JSONObject();
			for (int i = 0; i < packageNameArray.length(); i++) {
				String packageName = packageNameArray.getString(i);
				boolean isAppInstall = isAppInstall(packageName);
				obj.put(packageName, isAppInstall);
			}
			result = obj.toString();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.d("jason", "result="+result);
		return result;
		
	}
	
	/**
	 * 验证app是否已经安装
	 *
	 * @param packageName
	 * @return
	 */
	private boolean isAppInstall(String packageName){
		PackageManager packageManager = context.getPackageManager();
		// 获取所有已安装程序的包信息
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
		for (int i = 0; i < pinfo.size(); i++) {
			if (pinfo.get(i).packageName.equalsIgnoreCase(packageName))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.inspur.imp.plugin.ImpPlugin#execute(java.lang.String,
	 * org.json.JSONObject)
	 */
	@Override
	public void execute(String action, JSONObject paramsObject) {
		// TODO Auto-generated method stub
		if ("startApp".equals(action)) {
			startApp(paramsObject);
		}
	}

	/**
	 * 打开一个App
	 * 
	 * @param paramsObject
	 */
	private void startApp(JSONObject paramsObject) {
		// TODO Auto-generated method stub
		String targetActivity = "";
		String packageName = "";
		String action = "";
		String dataUri = "";
		String intentUri = "";
		Bundle bundle = new Bundle();
		Intent intent = new Intent();
		try {
			if (!paramsObject.isNull("packageName")) {
				packageName = paramsObject.getString("packageName");// 打开的目标Activity
			}
			if (!paramsObject.isNull("activityName")) {
				targetActivity = paramsObject.getString("activityName");// 打开的目标Activity
			}
			if (!paramsObject.isNull("action")) {
				action = paramsObject.getString("action");// 打开的目标Activity
			}
			if (!paramsObject.isNull("intentUri")) {
				intentUri =  paramsObject.getString("intentUri");
				
			}
			if (!paramsObject.isNull("dataUri")) {
				dataUri =  paramsObject.getString("dataUri");
				
			}
			if (!paramsObject.isNull("intentParam")) {
				JSONObject obj = paramsObject.getJSONObject("intentParam");
				Iterator<String> keys = obj.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					bundle.putSerializable(key, (Serializable) obj.get(key));
				}
			}
			if (StrUtil.strIsNotNull(packageName)) {
				//intent.setPackage(packageName);
				if (StrUtil.strIsNotNull(targetActivity)) {
					ComponentName componet = new ComponentName(packageName,
							targetActivity);
					intent.setComponent(componet);
				}else {
					intent = getActivity().getPackageManager().getLaunchIntentForPackage(packageName);
				} 
				  
			}
			
			
			if (StrUtil.strIsNotNull(intentUri)) {
				intent = Intent.parseUri(intentUri, 0);
				LogUtils.debug("jason", "intentUri---");
			}
			
			if (StrUtil.strIsNotNull(dataUri)) {
				intent.setData(Uri.parse(dataUri));
			}
		
			
			if (StrUtil.strIsNotNull(action)) {
				intent.setAction(action);
			} 
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtras(bundle);
			this.context.startActivity(intent);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see com.inspur.imp.plugin.ImpPlugin#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}

}
