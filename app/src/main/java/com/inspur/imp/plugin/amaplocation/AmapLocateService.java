package com.inspur.imp.plugin.amaplocation;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 设置GPS类
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class AmapLocateService extends ImpPlugin implements
		AMapLocationListener {

	// 设置回调函数
	private String functName;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;

	@Override
	public void execute(String action, JSONObject paramsObject) {
		LogUtils.debug("jason", "action="+action);
		// 获取经纬度地址
		if ("getInfo".equals(action)) {
			getInfo(paramsObject);
		}
	}

	/**
	 * 获得位置信息
	 * 
	 * @param paramsObject
	 */
	private void getInfo(JSONObject paramsObject) {
		// 解析json串获取到传递过来的参数和回调函数
		try {
			if (!paramsObject.isNull("callback"))
				functName = paramsObject.getString("callback");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		initLocation();
		mlocationClient.startLocation();

	}
	
	/**
	 * 初始化定位
	 */
	private void initLocation() {
		// 初始化定位，
		mlocationClient = new AMapLocationClient(getActivity());
		// 初始化定位参数
		mLocationOption = new AMapLocationClientOption();
		mLocationOption.setOnceLocation(true);
		// 设置定位模式为低功耗定位
		mLocationOption.setLocationMode(AMapLocationMode.Battery_Saving);
		// 设置定位回调监听
		mlocationClient.setLocationListener(this);
		// 设置定位参数
		mlocationClient.setLocationOption(mLocationOption);
	}

	@Override
	public void onDestroy() {
		mlocationClient.stopLocation();
		mlocationClient.onDestroy();
	}


	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		
		// 绑定监听状态
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("latitude",
					String.valueOf(amapLocation.getLatitude()));
			jsonObject.put("longitude",
					String.valueOf(amapLocation.getLongitude()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 设置回调js页面函数
		LogUtils.debug("yfcLog", "amapLocation:"+jsonObject.toString());
		jsCallback(functName, jsonObject.toString());
		AmapLocateService.this.onDestroy();
	}
}
