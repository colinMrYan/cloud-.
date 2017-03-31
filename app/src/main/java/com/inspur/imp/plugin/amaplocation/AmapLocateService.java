package com.inspur.imp.plugin.amaplocation;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 设置GPS类（高德定位提供），可指定您所需要的坐标系类型，<br/>
 * 指定方式为在excute方法里的Json参数上添加键值为coordinateType的参数<br/>
 * 参数不指定返回的坐标系为GCJ02类型<br/>
 * 支持的坐标系类型有<br/>
 * 百度坐标（BD09）<br/>
 * 国测局坐标（火星坐标，gcj02）<br/>
 * WGS84坐标系（HCM产品使用）<br/>
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class AmapLocateService extends ImpPlugin implements
        AMapLocationListener {

    // 设置回调函数
    private String functName;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private String coordinateType = "";

    @Override
    public void execute(String action, JSONObject paramsObject) {
        LogUtils.debug("jason", "action=" + action);
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
//            paramsObject.put("coordinateType","WGS84");
            LogUtils.YfcDebug("参数信息："+paramsObject.toString());
            if (!paramsObject.isNull("callback"))
                functName = paramsObject.getString("callback");
            if (!paramsObject.isNull("coordinateType")) {
                this.coordinateType = paramsObject.getString("coordinateType");
            }
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
        String latitude = "", longtitude = "";
        double[] location = {0,0};
        if (!StringUtils.isBlank(coordinateType)) {
            location = coordinateTrans(amapLocation);
            longtitude = String.valueOf(location[0]);
            latitude = String.valueOf(location[1]);
        } else {
            longtitude = String.valueOf(amapLocation.getLongitude());
            latitude = String.valueOf(amapLocation.getLatitude());
        }
        // 绑定监听状态
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("longitude", longtitude);
            jsonObject.put("latitude", latitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 设置回调js页面函数
        LogUtils.debug("yfcLog", "amapLocation:" + jsonObject.toString());
        jsCallback(functName, jsonObject.toString());
        AmapLocateService.this.onDestroy();
    }

    /**
     * 坐标类型转化
     * @param amapLocation
     * @return
     */
    private double[] coordinateTrans(AMapLocation amapLocation) {
        double[] location = {0,0};
        double longitudeD = amapLocation.getLongitude();
        double latitudeD = amapLocation.getLatitude();
        coordinateType = coordinateType.toUpperCase();
        if(coordinateType.equals("WGS84")){
            location = ECMLoactionTransformUtils.gcj02towgs84(longitudeD, latitudeD);
        }else if(coordinateType.equals("BD09")){
            location = ECMLoactionTransformUtils.gcj02tobd09(longitudeD, latitudeD);
        }else{
            location[0] = amapLocation.getLongitude();
            location[1] = amapLocation.getLatitude();
        }
        return location;
    }
}
