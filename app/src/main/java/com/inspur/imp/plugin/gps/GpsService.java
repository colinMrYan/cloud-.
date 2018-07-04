package com.inspur.imp.plugin.gps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.inspur.emmcloud.util.common.LogUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.imp.api.Res;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.plugin.amaplocation.ECMLoactionTransformUtils;
import com.inspur.imp.util.DialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


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
    private List<AMapLocation> aMapLocationList = new ArrayList<>();
    private int locationCount = 0;
    private  AlertDialog dialog;
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
        }else{
            DialogUtil.getInstance(getActivity()).show();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        DialogUtil.getInstance(getActivity()).show();
        return "";
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
            if (!paramsObject.isNull("callback"))
                functName = paramsObject.getString("callback");
            if (!paramsObject.isNull("coordinateType")) {
                this.coordinateType = paramsObject.getString("coordinateType");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        boolean isOpen = Settings.Secure.getInt(context.getContentResolver(),Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0;
        if (isOpen) {
            if (dialog == null){
                final AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());

                builder.setTitle(Res.getStringID("msg_title")).setMessage("请在开发者选项中关闭【允许模拟位置】选项");

                builder.setPositiveButton(Res.getStringID("file_ok"),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    context.startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                        });
                // 禁止取消按钮
                builder.setCancelable(false);
                dialog = builder.create();
            }
            if (context != null &&  !dialog.isShowing()) {
                dialog.show();
            }
            // 绑定监听状态
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("longitude","0");
                jsonObject.put("latitude","0");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsCallback(functName, jsonObject.toString());
        }else{
            startLocation();
        }

    }

    /**
     * 初始化定位
     */
    private void startLocation() {
        if (aMapLocationList == null) {
            aMapLocationList = new ArrayList<>();
        }
        aMapLocationList.clear();
        locationCount = 0;
        if (mlocationClient == null) {
            // 初始化定位，
            mlocationClient = new AMapLocationClient(getActivity().getApplicationContext());
        }
        // 初始化定位参数 默认连续定位
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setInterval(1000);
        // 设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        mLocationOption.setWifiScan(true);
        // 设置定位回调监听
        mlocationClient.setLocationListener(this);
        mlocationClient.startLocation();

    }

    @Override
    public void onDestroy() {
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
            mlocationClient = null;
        }
        if (aMapLocationList != null) {
            aMapLocationList = null;
        }
        locationCount = 0;
    }


    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        locationCount++;
        if (amapLocation != null && (amapLocation.getErrorCode() == 0)) {
            aMapLocationList.add(amapLocation);
        }
        if (locationCount > 2 || (amapLocation != null && (amapLocation.getErrorCode() == 0) && amapLocation.getAccuracy()<60)) {
            mlocationClient.stopLocation();
            String latitude = "0.0", longtitude = "0.0";
            if (aMapLocationList.size() > 0) {
                amapLocation = Collections.min(aMapLocationList, new ComparatorValues());
                double[] location = {0, 0};
                if (!StringUtils.isBlank(coordinateType)) {
                    location = coordinateTrans(amapLocation);
                    longtitude = String.valueOf(location[0]);
                    latitude = String.valueOf(location[1]);
                } else {
                    longtitude = String.valueOf(amapLocation.getLongitude());
                    latitude = String.valueOf(amapLocation.getLatitude());
                }

            }
            aMapLocationList = null;
            locationCount = 0;
            // 绑定监听状态
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("longitude", longtitude);
                jsonObject.put("latitude", latitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsCallback(functName, jsonObject.toString());
            // 设置回调js页面函数
            LogUtils.debug("yfcLog", "GPSLocation:" + jsonObject.toString());
            // 设置回调js页面函数
            GpsService.this.onDestroy();
        }
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
    private class ComparatorValues implements Comparator<AMapLocation> {
        @Override
        public int compare(AMapLocation o1, AMapLocation o2) {
            float locationAccuracy1 = o1.getAccuracy();
            float locationAccuracy2 = o2.getAccuracy();
            int result = 0;
            if (locationAccuracy1 > locationAccuracy2) {
                result = 1;
            } else if (locationAccuracy1 < locationAccuracy2) {
                result = -1;
            }
            return result;
        }
    }

}
