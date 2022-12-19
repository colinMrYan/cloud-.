package com.inspur.emmcloud.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.AppAPIService;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppConfigCacheUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.PVCollectModelCacheUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;

import org.json.JSONObject;

import java.util.List;

public class LocationService extends Service implements AMapLocationListener {

    private int interval = 0;
    private Handler handler;
    private Runnable runnable;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private AppAPIService apiService;
    //针对Android O，启动前台服务通知，否则报错 Context.startForegroundService() did not then call Service.startForeground()
    public static final String CHANNEL_ID_STRING = "service_location";
    public static final String CHANNEL_ID_NAME = "emmCloud";
    public static final int NOTIFICATION_ID = 3;
    @Override
    public void onCreate() {
        super.onCreate();
        startForeground();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        String posReportTimeInterval = AppConfigCacheUtils.getAppConfigValue(getApplicationContext(), Constant.CONCIG_POS_REPORT_TIME_INTERVAL_, "0");
        if (posReportTimeInterval.equals("0") || StringUtils.isBlank(MyApplication.getInstance().getToken())) {
            stopSelf();
        } else {
            interval = Integer.valueOf(posReportTimeInterval) * 60000;
            if (handler != null) {
                handler.removeCallbacks(runnable);
                handler = null;
            }
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (NetUtils.isNetworkConnected(getApplicationContext(), false)) {
                        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(LocationService.this, Permissions.LOCATION, new PermissionRequestCallback() {
                            @Override
                            public void onPermissionRequestSuccess(List<String> permissions) {
                                startLocation();
                            }

                            @Override
                            public void onPermissionRequestFail(List<String> permissions) {
                                LocationService.this.stopSelf();
                            }
                        });

                    } else {
                        continueLocation();
                        PVCollectModelCacheUtils.saveCollectModel("LocationService: onStartCommand", "continueLocation");
                    }
                }
            };
            handler.postDelayed(runnable, 0);// 开启Service的时候即执行一次
            PVCollectModelCacheUtils.saveCollectModel("LocationService: onStartCommand", "startLocation");

        }

        return START_STICKY;
    }

    /**
     * 启动定位
     */
    private void startLocation() {
        if (mlocationClient == null) {
            initLocation();
        }
        mlocationClient.startLocation();
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        // 初始化定位，
        mlocationClient = new AMapLocationClient(this);
        // 初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setOnceLocation(true);
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
        mLocationOption.setWifiScan(true);
        mLocationOption.setOnceLocationLatest(true);
        // 设置定位回调监听
        mlocationClient.setLocationListener(this);
        // 设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
    }

    /**
     * 继续定位
     */
    private void continueLocation() {
        if (handler != null) {
            handler.postDelayed(runnable, interval);// 设置每隔一段时间秒执行一次
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            PVCollectModelCacheUtils.saveCollectModel("LocationService: onDestroy", "stopLocation");
            mlocationClient.onDestroy();
            mlocationClient = null;
        }
        stopForeground(true);
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        // TODO Auto-generated method stub
        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
            JSONObject locationObj = new JSONObject();
            try {
                locationObj.put("Lng", amapLocation.getLongitude());
                locationObj.put("Lat", amapLocation.getLatitude());
            } catch (Exception e) {
                e.printStackTrace();
            }
            String locationObjJson = locationObj.toString();
            uploadPosition(locationObjJson);

        } else {
            continueLocation();
            PVCollectModelCacheUtils.saveCollectModel("LocationService: onLocationChanged : else", "continueLocation");

        }
    }

    /**
     * 删除位置信息
     *
     * @param locationObjJson
     */
    private void uploadPosition(String locationObjJson) {
        if (NetUtils.isNetworkConnected(this, false)) {
            if (apiService == null) {
                apiService = new AppAPIService(getApplicationContext());
                apiService.setAPIInterface(new WebService());
            }
            apiService.uploadPosition(locationObjJson);
            LogUtils.jasonDebug("locationObjJson=" + locationObjJson);
        } else {
            continueLocation();
            PVCollectModelCacheUtils.saveCollectModel("LocationService: uploadPosition: else", "continueLocation");

        }
    }

    void startForeground() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        NotificationChannel mChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID_STRING, CHANNEL_ID_NAME,
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID_STRING).build();
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnUploadPositionSuccess() {
            continueLocation();
            PVCollectModelCacheUtils.saveCollectModel("LocationService: returnUploadPositionSuccess", "continueLocation");

        }
    }
}
