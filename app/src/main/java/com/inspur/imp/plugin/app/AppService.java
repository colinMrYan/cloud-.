package com.inspur.imp.plugin.app;

import android.app.Activity;

import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONObject;


/**
 * 应用相关本地功能类
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class AppService extends ImpPlugin {

	@Override
	public void execute(String action, JSONObject paramsObject) {
		if ("close".equals(action)) {
			close();
		}else{
			((ImpActivity)getActivity()).showImpDialog();
		}
	}

	@Override
	public String executeAndReturn(String action, JSONObject paramsObject) {
		// TODO Auto-generated method stub
		// 退出系统
		if ("close".equals(action)) {
			close();
		}else{
			((ImpActivity)getActivity()).showImpDialog();
		}
		return "";
	}
	

	// 退出系统
	private void close() {

		((Activity) this.context).finish();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
	};

}
