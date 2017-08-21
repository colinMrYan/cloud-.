package com.inspur.emmcloud.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;

public class LocationService extends Service implements AMapLocationListener {

	private int microSeconds = 0;
	private Handler handler;
	private Runnable runnable;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		String minutes = PreferencesUtils.getString(LocationService.this,
				"GPSInterval", "5");
		microSeconds = Integer.valueOf(minutes) * 60000;
		if (handler != null) {
			handler.removeCallbacks(runnable);
			handler = null;
		}
		handler = new Handler();
		runnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				if (NetUtils.isNetworkConnected(LocationService.this)
						) {
					initLocation();
					mlocationClient.startLocation();
				} else {
					if (handler != null) {
						handler.postDelayed(runnable, microSeconds);
					}
				}
			}
		};
		handler.postDelayed(runnable, 0);// 开启Service的时候即执行一次
		// flags = START_REDELIVER_INTENT;
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 初始化定位
	 */
	private void initLocation() {
		// 初始化定位，
		mlocationClient = new AMapLocationClient(LocationService.this);
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
		// TODO Auto-generated method stub
		if (handler != null) {
			handler.removeCallbacks(runnable);
			handler = null;
		}
		if (mlocationClient.isStarted()) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		super.onDestroy();
	}

	@Override
	public void onLocationChanged(AMapLocation location) {
		// TODO Auto-generated method stub
		if (location != null) {
		} else {
			if (handler != null) {
				handler.postDelayed(runnable, microSeconds);// 设置每隔一段时间秒执行一次
			}
		}
		mlocationClient.stopLocation();
		mlocationClient.onDestroy();
	}
}
