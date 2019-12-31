package com.inspur.emmcloud.web.plugin.amaplocation;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


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
    private String coordinateType = "";
    private List<AMapLocation> aMapLocationList = new ArrayList<>();
    private int locationCount = 0;
    private AlertDialog dialog;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        LogUtils.YfcDebug("paramsObject:" + paramsObject.toString());
        LogUtils.debug("jason", "action=" + action);
        // 获取经纬度地址
        if ("getInfo".equals(action)) {
            getInfo(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
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
            if (!paramsObject.isNull("coordinateType")) {
                this.coordinateType = paramsObject.getString("coordinateType");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getFragmentContext(), Permissions.LOCATION, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                if (AppUtils.isLocationEnabled(getActivity())) {
                    startLocation();
                } else {
                    new CustomDialog.MessageDialogBuilder(getActivity())
                            .setMessage(getActivity().getString(R.string.imp_location_enable, AppUtils.getAppName(getFragmentContext())))
                            .setCancelable(false)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(R.string.go_setting, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AppUtils.openLocationSetting(getActivity());
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(getFragmentContext(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getFragmentContext(), permissions));
            }
        });
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
            mlocationClient = new AMapLocationClient(getFragmentContext());
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
        if (locationCount > 2 || (amapLocation != null && (amapLocation.getErrorCode() == 0) && amapLocation.getAccuracy() < 60)) {
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
                jsonObject.put("addr", amapLocation.getAddress());
                jsonObject.put("country", amapLocation.getCountry());
                jsonObject.put("province", amapLocation.getProvince());
                jsonObject.put("city", amapLocation.getCity());
                jsonObject.put("district", amapLocation.getDistrict());
                jsonObject.put("street", amapLocation.getStreet());
                jsonObject.put("streetNum", amapLocation.getStreetNum());
            } catch (Exception e) {
                e.printStackTrace();
            }
            jsCallback(functName, jsonObject.toString());
            // 设置回调js页面函数
            LogUtils.debug("yfcLog", "amapLocation:" + jsonObject.toString());
            // 设置回调js页面函数
            AmapLocateService.this.onDestroy();
        }
    }

    /**
     * 坐标类型转化
     *
     * @param amapLocation
     * @return
     */
    private double[] coordinateTrans(AMapLocation amapLocation) {
        double[] location = {0, 0};
        double longitudeD = amapLocation.getLongitude();
        double latitudeD = amapLocation.getLatitude();
        coordinateType = coordinateType.toUpperCase();
        if (coordinateType.equals("WGS84")) {
            location = ECMLoactionTransformUtils.gcj02towgs84(longitudeD, latitudeD);
        } else if (coordinateType.equals("BD09")) {
            location = ECMLoactionTransformUtils.gcj02tobd09(longitudeD, latitudeD);
        } else {
            location[0] = amapLocation.getLongitude();
            location[1] = amapLocation.getLatitude();
        }
        return location;
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
