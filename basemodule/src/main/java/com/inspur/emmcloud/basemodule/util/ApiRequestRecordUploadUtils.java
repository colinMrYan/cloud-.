package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.os.Build;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.ApiRequestRecord;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by chenmch on 2019/10/12.
 */

public class ApiRequestRecordUploadUtils extends BaseModuleAPIInterfaceInstance implements AMapLocationListener {
    private Context context;
    private List<ApiRequestRecord> apiRequestRecordList;
    private AMapLocationClient mLocationClient;

    public ApiRequestRecordUploadUtils() {
        context = BaseApplication.getInstance();
    }

    public void start() {
        if (!NetUtils.isNetworkConnected(BaseApplication.getInstance(), false)) {
            return;
        }
        if (!BaseApplication.getInstance().isHaveLogin()) {
            return;
        }
        apiRequestRecordList = ApiRequestRecordCacheUtils.getApiRequestRecordList(500);
        if (apiRequestRecordList.size() == 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 23 && PermissionRequestManagerUtils.getInstance().isHasPermission(BaseApplication.getInstance(), Permissions.LOCATION)) {
            startLocation();
        } else {
            onLocationChanged(null);
        }
    }

    private void startLocation() {
        mLocationClient = new AMapLocationClient(context);
        // 初始化定位参数
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setOnceLocation(true);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        mLocationOption.setWifiScan(true);
        mLocationOption.setOnceLocationLatest(true);
        // 设置定位回调监听
        mLocationClient.setLocationListener(this);
        // 设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
        PVCollectModelCacheUtils.saveCollectModel("ApiRequestRecordUploadUtils: start", "startLocation");
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        String lon = "";
        String lat = "";
        if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
            lon = aMapLocation.getLongitude() + "";
            lat = aMapLocation.getLatitude() + "";
        }
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            PVCollectModelCacheUtils.saveCollectModel("ApiRequestRecordUploadUtils: onLocationChanged", "stopLocation");
            mLocationClient.onDestroy();
            mLocationClient = null;
        }
        String content = getUploadContent(lon, lat);
        upload(content);
    }

    private String getUploadContent(String lon, String lat) {
        JSONObject obj = new JSONObject();
        try {
            JSONArray array = new JSONArray();
            for (ApiRequestRecord apiRequestRecord : apiRequestRecordList) {
                array.put(apiRequestRecord.toJSONObject());
            }
            obj.put("userContent", array);
            obj.put("userID", BaseApplication.getInstance().getUid());
            obj.put("clientType", "Android");
            obj.put("appVersion", AppUtils.getReleaseVersion());
            obj.put("enterpriseID", BaseApplication.getInstance().getCurrentEnterprise().getId());
            String userName = PreferencesUtils.getString(BaseApplication.getInstance(), "userRealName", "");
            obj.put("userName", userName);
            obj.put("lon", lon);
            obj.put("lat", lat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    private void upload(String content) {
        if (NetUtils.isNetworkConnected(context, false)) {
            BaseModuleApiService apiService = new BaseModuleApiService(context);
            apiService.setAPIInterface(this);
            apiService.uploadApiRequestRecord(content, apiRequestRecordList);
        }
    }

    @Override
    public void returnUploadApiRequestRecordSuccess(List<ApiRequestRecord> apiRequestRecordList) {
        ApiRequestRecordCacheUtils.deleteApiRequestRecordList(apiRequestRecordList);
    }

    @Override
    public void returnUploadApiRequestRecordFail() {
        super.returnUploadApiRequestRecordFail();
    }
}
