package com.inspur.emmcloud.web.plugin.gps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPICallback;
import com.inspur.emmcloud.basemodule.api.CloudHttpMethod;
import com.inspur.emmcloud.basemodule.api.HttpUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.plugin.amaplocation.ECMLoactionTransformUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 设置GPS类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class GpsService extends ImpPlugin implements
        AMapLocationListener {

    public String successCb, failCb, traceCallBack;
    // 设置回调函数
    private String functName;
    // 声明LocationManager对象
    private LocationManager locationManager;
    private AMapLocationClient mlocationClient;
    private String coordinateType = "";
    private List<AMapLocation> aMapLocationList = new ArrayList<>();
    private HashMap<String, String> uploadMapLocationList = new HashMap<>();
    private int locationCount = 0;
    private int openLocationStatus = 0;//0代表初始状态，1代表点击了设置,2，代表点了设置之后已经重新定位
    // 上传地址
    private String uploadUri;
    private boolean requestingUri = false;
    private Timer uploadTimer;
    private boolean uploadTrace = false;

    private TimerTask timerTask;
    private String headerObj;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        switch (action) {
            case "open":
                open();
                break;
            case "close":
                close();
                break;
            case "getInfo":
                getInfo(paramsObject);
                break;
            case "getAddress":
                getAddress(paramsObject);
                break;
            case "uploadTrace":
                uploadTraceInfo(paramsObject);
                break;
            default:
                showCallIMPMethodErrorDlg();
                break;
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    /**
     * 开启GPS
     *
     * @param
     */
    private void open() {
        // 通过系统服务，取得LocationManager对象
        locationManager = (LocationManager) (getFragmentContext()
                .getSystemService(Context.LOCATION_SERVICE));
        // 判断GPS模块是否开启，如果没有则开启
        if (!locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 转到手机设置界面，用户设置GPS
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            getActivity().startActivity(intent);
            // 设置完成后返回到原来的界面
        } else {
            // 弹出Toast
            ToastUtils.show(getFragmentContext(), R.string.web_gps_opened);
        }
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
        if (openLocationStatus == 1 && AppUtils.isLocationEnabled(getFragmentContext())) {
            startLocation();
            PVCollectModelCacheUtils.saveCollectModel("GpsService: onActivityResume", "startLocation");
            openLocationStatus = 2;
        }
    }

    private void requestUploadLocations() {
        if (requestingUri) return;
        requestingUri = true;
        RequestParams params = BaseApplication.getInstance().getHttpRequestParams(uploadUri);
        if (!uploadMapLocationList.containsKey("uid")) {
            uploadMapLocationList.put("uid", BaseApplication.getInstance().getUid());
        }
        params.addBodyParameter("data", JSONUtils.map2Json(uploadMapLocationList));
        params.addBodyParameter("code", "0000");
        params.addBodyParameter("errMsg", "");
        if (!StringUtils.isEmpty(headerObj)) {
            JSONObject header = JSONUtils.getJSONObject(headerObj);
            if (header != null) {
                Iterator<String> keys = header.keys();
                while (keys.hasNext()) {
                    try {
                        String key = keys.next();
                        params.addHeader(key, header.getString(key));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        HttpUtils.request(getActivity(), CloudHttpMethod.POST, params, new BaseModuleAPICallback(getActivity(), uploadUri) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                if (traceCallBack != null) {
                    uploadTraceCallback(true, null);
                }
                requestingUri = false;
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                if (traceCallBack != null) {
                    uploadTraceCallback(false, error);
                }
                requestingUri = false;
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                requestingUri = false;
            }

        });
    }

    private void uploadTraceCallback(boolean success, String errorInfo) {
        JSONObject json = new JSONObject();
        try {
            json.put("state", success ? 1 : 0);
            json.put("status", success ? 1 : 0);
            JSONObject result = new JSONObject();
            json.put("result", result);
            if (!success) {
                result.put("data", errorInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsCallback(traceCallBack, json);
    }

    // 上传位置信息
    private void uploadTraceInfo(JSONObject jsonObject) {
        if (uploadMapLocationList == null) {
            uploadMapLocationList = new HashMap<>();
        }
        try {
            if (!jsonObject.isNull("success")) {
                traceCallBack = jsonObject.getString("success");
            } else {
                traceCallBack = null;
            }
            final JSONObject optionsObj = jsonObject.getJSONObject("options");
            if (optionsObj.has("uri")) {
                uploadUri = optionsObj.getString("uri");
            } else {
                uploadUri = null;
            }
            if (optionsObj.has("action")) {
                uploadTrace = optionsObj.getBoolean("action");
            }
            if (optionsObj.has("headers")) {
                headerObj = JSONUtils.getString(optionsObj, "headers", null);
            }else {
                headerObj = null;
            }
            if (uploadTrace && StringUtils.isEmpty(uploadUri)) {
                uploadTraceCallback(false, "invalid parameter");
                return;
            }
            PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getActivity(), Permissions.LOCATION, new PermissionRequestCallback() {
                @Override
                public void onPermissionRequestSuccess(List<String> permissions) {
                    if (uploadTrace && uploadUri != null) {
                        open();
                        startLocation();
                        uploadTimer = new Timer();
                        timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                if (uploadUri == null) {
                                    uploadMapLocationList.clear();
                                    if (uploadTimer != null) {
                                        uploadTimer.cancel();
                                        uploadTimer.purge();
                                    }
                                    return;
                                }
                                requestUploadLocations();
                            }
                        };
                        uploadTimer.schedule(timerTask, 500, 4000);

                    } else {
                        closeUploadPosition();
                    }
                }

                @Override
                public void onPermissionRequestFail(List<String> permissions) {
                    ToastUtils.show(getFragmentContext(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getFragmentContext(), permissions));
                }

            });
        } catch (JSONException e) {
            e.printStackTrace();
            uploadTraceCallback(false, e.getMessage());
        }
    }

    private void closeUploadPosition() {
        if (traceCallBack != null) {
            traceCallBack = null;
        }
        if (uploadUri != null) {
            uploadUri = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (uploadTimer != null) {
            uploadTimer.cancel();
            uploadTimer.purge();
        }
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
        }
        if (aMapLocationList !=null){
            aMapLocationList.clear();
        }
        if (uploadMapLocationList != null) {
            uploadMapLocationList.clear();
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
            jsCallback(functName, getErrorJson(e.getMessage()));
            e.printStackTrace();
        }
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getActivity(), Permissions.LOCATION, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                if (AppUtils.isLocationEnabled(getActivity())) {
                    startLocation();
                    PVCollectModelCacheUtils.saveCollectModel("GpsService: getInfo", "startLocation");
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
                                    openLocationStatus = 1;
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
     * 组装错误信息
     *
     * @param message
     * @return
     */
    private JSONObject getErrorJson(String message) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("errorMessage", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void getAddress(JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        if (!NetUtils.isNetworkConnected(getFragmentContext(), false)) {
            callbackFail("网络异常！");
            return;
        }
        JSONObject optionsObj = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        Double latitude = JSONUtils.getDouble(optionsObj, "latitude", null);
        Double longitude = JSONUtils.getDouble(optionsObj, "longitude", null);
        if (latitude == null || longitude == null) {
            callbackFail("参数传递不正确！");
            return;
        }
        String coordType = JSONUtils.getString(optionsObj, "coordType", "GCJ02");
        if (coordType.equals("WGS84")) {
            double[] toLocation = ECMLoactionTransformUtils.wgs84togcj02(longitude, latitude);
            longitude = toLocation[0];
            latitude = toLocation[1];
        } else if (coordType.equals("BD09")) {
            double[] toLocation = ECMLoactionTransformUtils.bd09togcj02(longitude, latitude);
            longitude = toLocation[0];
            latitude = toLocation[1];
        }
        longitude = new BigDecimal(longitude).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        latitude = new BigDecimal(latitude).setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
        getAdressFromNet(longitude, latitude);
    }


    private void getAdressFromNet(Double longitude, Double latitude) {
        String url = "https://restapi.amap.com/v3/geocode/regeo?parameters";
        RequestParams params = new RequestParams(url);
        params.addParameter("key", "cfbacd7a8024980f6b4e6e85c08b0376");
        params.addParameter("location", longitude + "," + latitude);
        x.http().request(HttpMethod.GET, params, new BaseModuleAPICallback(getFragmentContext(), url) {
            @Override
            public void callbackSuccess(byte[] arg0) {
                GetRegeoResult getRegeoResult = new GetRegeoResult(new String(arg0));
                if (getRegeoResult.getStatus() == 0) {
                    callbackFail(getRegeoResult.getInfo(), -1);
                } else {
                    JSONObject resultObject = new JSONObject();
                    try {
                        resultObject.put("addr", getRegeoResult.getAddr());
                        resultObject.put("country", getRegeoResult.getCountry());
                        resultObject.put("province", getRegeoResult.getProvince());
                        resultObject.put("city", getRegeoResult.getCity());
                        resultObject.put("district", getRegeoResult.getDistrict());
                        resultObject.put("street", getRegeoResult.getStreet());
                        resultObject.put("streetNum", getRegeoResult.getStreetNum());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    GpsService.this.callbackSuccess(resultObject);

                }
            }

            @Override
            public void callbackFail(String error, int responseCode) {
                GpsService.this.callbackFail("");
            }

            @Override
            public void callbackTokenExpire(long requestTime) {
                callbackFail("位置信息获取失败！", -1);

            }
        });
    }

    private void callbackSuccess(JSONObject resultObject) {
        if (!StringUtils.isBlank(successCb)) {
            this.jsCallback(successCb, resultObject);
        }
    }

    private void callbackFail(String errorMessage) {
        if (!StringUtils.isBlank(failCb)) {
            JSONObject object = new JSONObject();
            try {
                object.put("errorMessage", errorMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.jsCallback(failCb, object);
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
            PVCollectModelCacheUtils.saveCollectModel("GpsService: onDestroy", "stopLocation");
            mlocationClient = null;
        }
        if (aMapLocationList != null) {
            aMapLocationList = null;
        }
        if (uploadMapLocationList != null) {
            uploadMapLocationList = null;
        }
        if (traceCallBack != null) {
            traceCallBack = null;
        }
        if (uploadUri != null) {
            uploadUri = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (uploadTimer != null) {
            uploadTimer.cancel();
            uploadTimer.purge();
            uploadTimer = null;
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
            if (!uploadTrace) {
                mlocationClient.stopLocation();
            }
            PVCollectModelCacheUtils.saveCollectModel("GpsService: onLocationChanged", "stopLocation");
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
            aMapLocationList.clear();
            locationCount = 0;
            // 绑定监听状态s
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
                jsonObject.put("speed", amapLocation.getSpeed());
            } catch (Exception e) {
                if (uploadTrace) {
                    uploadTraceCallback(false, e.getMessage());
                } else {
                    jsCallback(functName, getErrorJson(e.getMessage()));
                }
                e.printStackTrace();
            }
            if (!uploadTrace) {
                jsCallback(functName, jsonObject.toString());
            }
            ArrayList<String> positionList = new ArrayList<>();
            if (uploadMapLocationList != null) {
                String locationList = uploadMapLocationList.get("locations");
                if (locationList != null) {
                    positionList = JSONUtils.JSONArray2List(locationList, new ArrayList<String>());
                    if (positionList != null) {
                        positionList.add(jsonObject.toString());
                    }
                }
                uploadMapLocationList.put("locations", JSONUtils.toJSONArray(positionList).toString());
            }
            // 设置回调js页面函数
            LogUtils.debug("yfcLog", "GPSLocation:" + jsonObject.toString());
            // 设置回调js页面函数
            if (!uploadTrace) {
                GpsService.this.onDestroy();
            }
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


    /**
     * 关闭GPS
     */
    private void close() {
        // 通过系统服务，取得LocationManager对象
        locationManager = (LocationManager) (getFragmentContext()
                .getSystemService(Context.LOCATION_SERVICE));
        // 判断GPS模块是否开启，如果已经开启了
        if (locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 转到手机设置界面，用户设置GPS
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            getActivity().startActivity(intent);
            // 设置完成后返回到原来的界面
        } else {
            // 弹出Toast
            ToastUtils.show(this.getFragmentContext(), R.string.web_gps_closed);
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
