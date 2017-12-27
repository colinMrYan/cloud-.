package com.inspur.imp.plugin.gps;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.plugin.amaplocation.ECMLoactionTransformUtils;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 设置GPS类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 *
 */
public class GpsService extends ImpPlugin implements
        AMapLocationListener{

    // 设置回调函数
    private String functName;
    // 声明LocationManager对象
    private LocationManager locationManager;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private String coordinateType = "";

    @Override
    public void execute(String action, JSONObject paramsObject) {
        // 开启GPS监控
        if ("open".equals(action)) {
            open();
        }
        // 关闭GPS监控
        else if ("close".equals(action)) {
            close();
        }
        // 获取经纬度地址
        else if ("getInfo".equals(action)) {
            getInfo(paramsObject);
        }
    }

    /**
     * 开启GPS
     *
     * @param
     */
    private void open() {
        // 通过系统服务，取得LocationManager对象
        locationManager = (LocationManager) (this.context
                .getSystemService(Context.LOCATION_SERVICE));
        // 判断GPS模块是否开启，如果没有则开启
        if (!locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 转到手机设置界面，用户设置GPS
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            this.context.startActivity(intent);
            // 设置完成后返回到原来的界面
        } else {
            // 弹出Toast
            Toast.makeText(this.context, "GPS已经开启", Toast.LENGTH_LONG).show();
        }
    }




    /**
     * 获得位置信息
     *
     * @param paramsObject
     */
    public void getInfo(JSONObject paramsObject) {
        // 解析json串获取到传递过来的参数和回调函数
        try {
//            paramsObject.put("coordinateType","WGS84");
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
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
        mLocationOption.setWifiScan(true);
        mLocationOption.setOnceLocationLatest(true);
        // 设置定位回调监听
        mlocationClient.setLocationListener(this);
        // 设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
    }

    @Override
    public void onDestroy() {
        if (mlocationClient  != null){
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
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
        GpsService.this.onDestroy();
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


    /**
     * 关闭GPS
     */
    private void close() {
        // 通过系统服务，取得LocationManager对象
        locationManager = (LocationManager) (this.context
                .getSystemService(Context.LOCATION_SERVICE));
        // 判断GPS模块是否开启，如果已经开启了
        if (locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 转到手机设置界面，用户设置GPS
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            this.context.startActivity(intent);
            // 设置完成后返回到原来的界面
        } else {
            // 弹出Toast
            Toast.makeText(this.context, "GPS已经关闭", Toast.LENGTH_LONG).show();
        }
    }


}
