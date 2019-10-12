package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.os.Build;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.ApiRequestRecord;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;

import java.util.List;

/**
 * Created by chenmch on 2019/10/12.
 */

public class ApiRequestRecordUploadUtils implements AMapLocationListener {
    private Context context;
    private List<ApiRequestRecord> apiRequestRecordList;

    public ApiRequestRecordUploadUtils() {
        context = BaseApplication.getInstance();
    }

    public void upload() {
        List<ApiRequestRecord> apiRequestRecordList = ApiRequestRecordCacheUtils.getApiRequestRecordList();
        if (apiRequestRecordList.size() == 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 23 && PermissionRequestManagerUtils.getInstance().isHasPermission(BaseApplication.getInstance(), Permissions.LOCATION)) {
            startLocation();
        } else {
            onLocationChanged(null;)
        }
    }

    private void startLocation() {
        AMapLocationClient mlocationClient = new AMapLocationClient(context);
        // 初始化定位参数
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setOnceLocation(true);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setWifiScan(true);
        mLocationOption.setOnceLocationLatest(true);
        // 设置定位回调监听
        mlocationClient.setLocationListener(this);
        // 设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        String lng = "";
        String Lat = "";
        if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
            lng = aMapLocation.getLongitude() + "";
            Lat = aMapLocation.getLatitude() + "";
        }

    }
}
