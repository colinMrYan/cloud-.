package com.inspur.imp.plugin.loadingdialog;

import com.inspur.emmcloud.util.LogUtils;
import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONObject;


/**
 * web loading显示类
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class LoadingDialogService extends ImpPlugin {


	@Override
	public void execute(String action, JSONObject paramsObject) {
		if ("show".equals(action)) {
			LogUtils.jasonDebug("paramsObject="+paramsObject.toString());
			showDlg(paramsObject);
		}else if ("hide".equals(action)) {
			hideDlg();
		}
	}

	private void showDlg(JSONObject paramsObject){
		String content = null;
		if (paramsObject != null && !paramsObject.isNull("content")) {
			content = "";
		}
		((ImpActivity)getActivity()).showLoadingDlg(content);
	}

	private void hideDlg(){
		((ImpActivity)getActivity()).dimissLoadingDlg();
	}

	@Override
	public void onDestroy() {

	}
}
