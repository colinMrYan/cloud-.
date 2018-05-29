package com.inspur.imp.plugin.telephone;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.util.DialogUtil;
import com.inspur.imp.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 拨打电话服务
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class TelephoneService extends ImpPlugin {

	private String tel;

	@Override
	public void execute(String action, JSONObject paramsObject) {
		// 打开手机拨号界面
		if ("dial".equals(action)) {
			dial(paramsObject);
		}
		// 直接拨打电话
		else if ("call".equals(action)) {
			call(paramsObject);
		}else{
			DialogUtil.getInstance(getActivity()).show();
		}
	}

	/**
	 * 跳转到拨号界面
	 * 
	 * @param paramsObject
	 */
	private void dial(JSONObject paramsObject) {
		// 解析json串获取到传递过来的参数和回调函数
		try {
			if (!paramsObject.isNull("tel"))
				tel = paramsObject.getString("tel");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (!StrUtil.strIsNotNull(tel)) {
			Toast.makeText(this.context, "电话号码不能为空！", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent("android.intent.action.DIAL",
				Uri.parse("tel:" + tel));
		this.context.startActivity(intent);
	}

	/**
	 * 直接拨打电话
	 * 
	 * @param paramsObject
	 */
	private void call(JSONObject paramsObject) {
		// 解析json串获取到传递过来的参数和回调函数
		try {
			if (!paramsObject.isNull("tel"))
				tel = paramsObject.getString("tel");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (!StrUtil.strIsNotNull(tel)) {
			Toast.makeText(this.context, "电话号码不能为空！", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		intent.setAction("android.intent.action.CALL");
		// intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("tel:" + tel));
		// 方法内部会自动为Intent添加类别：android.intent.category.DEFAULT
		this.context.startActivity(intent);
	}

	@Override
	public void onDestroy() {

	}
}
