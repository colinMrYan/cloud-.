package com.inspur.imp.plugin.gps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * 设置GPS类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 *
 */
public class GpsService extends ImpPlugin {

	// 设置回调函数
	private String functName;
	// 声明LocationManager对象
	private LocationManager locationManager;
	private String provider;
	// 位置信息
	private Location location;
	// 纬度
	private double latitude = 0;
	// 经度
	private double longitude = 0;
	// 卫星数量
	private int satelliteNum = 0;

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
	private void getInfo(JSONObject paramsObject) {
		// 解析json串获取到传递过来的参数和回调函数
		try {
			if (!paramsObject.isNull("callback"))
				functName = paramsObject.getString("callback");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (locationManager == null) {
			// 通过系统服务，取得LocationManager对象
			locationManager = (LocationManager) (this.context
					.getSystemService(Context.LOCATION_SERVICE));
		}
		// 判断GPS模块是否开启，如果没有则开启
		if (!locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// 转到手机设置界面，用户设置GPS
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			this.context.startActivity(intent);
		}
		// 如果GPS或网络定位开启，呼叫locationServiceInitial()更新位置
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);// 高精度
		criteria.setAltitudeRequired(false);// 不要求海拔
		criteria.setBearingRequired(false);// 不要求方位
		criteria.setCostAllowed(true);// 允许有花费
		criteria.setPowerRequirement(Criteria.POWER_LOW);// 低功耗
		provider = locationManager.getBestProvider(criteria, true);
		if (provider != null && locationManager.isProviderEnabled(provider)) {
			if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				location = locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location != null) {
					getAddress(location);
				} else {
					location = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						getAddress(location);
					} else {
						updateWithNewLocation(location);
						// 监听位置变化，2秒一次，距离10米以上
						locationManager.requestLocationUpdates(provider, 2000, 10,
								listener);
					}
				}
				return;
			}

		}
		// 绑定监听状态
		locationManager.addGpsStatusListener(gpsListener);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("latitude", String.valueOf(latitude));
			jsonObject.put("longitude", String.valueOf(longitude));
			jsonObject.put("satelliteNum", String.valueOf(satelliteNum));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 设置回调js页面函数
		jsCallback(functName, jsonObject.toString());
	}

	/**
	 * 设置监听，隔段时间定位下，如果第一次就取得了地址信息，就返回地址；如果没取到，就设置监听
	 */
	private LocationListener listener = new LocationListener() {

		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	// 状态监听
	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			switch (event) {
				// 第一次定位
				case GpsStatus.GPS_EVENT_FIRST_FIX:
					break;
				// 卫星状态改变
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					// 获取当前状态
					if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
						// TODO: Consider calling
						//    ActivityCompat#requestPermissions
						// here to request the missing permissions, and then overriding
						//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
						//                                          int[] grantResults)
						// to handle the case where the user grants the permission. See the documentation
						// for ActivityCompat#requestPermissions for more details.
						GpsStatus gpsStatus = locationManager.getGpsStatus(null);

						// 获取卫星颗数的默认最大值
						int maxSatellites = gpsStatus.getMaxSatellites();
						// 创建一个迭代器保存所有卫星
						Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
								.iterator();
						int count = 0;
						while (iters.hasNext() && count <= maxSatellites) {
							GpsSatellite s = iters.next();
							count++;
						}
						satelliteNum = count;
						return;
					}

					break;
				// 定位启动
				case GpsStatus.GPS_EVENT_STARTED:
					break;
				// 定位结束
				case GpsStatus.GPS_EVENT_STOPPED:
					break;
			}
		}

		;
	};

	/**
	 * 位置更新
	 *
	 * @param location
	 */
	private void updateWithNewLocation(Location location) {
		// 隔段时间定位下
		if (location != null) {
			getAddress(location);
		}
	}

	/**
	 * 获得位置信息
	 *
	 * @param locationNew
	 */
	private void getAddress(Location locationNew) {
		latitude = locationNew.getLatitude();
		longitude = locationNew.getLongitude();
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
		// 在此只关闭监听器
		if (locationManager != null) {
			if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				locationManager.removeUpdates(listener);
				locationManager.removeGpsStatusListener(gpsListener);
				return;
			}


		}
	}

	@Override
	public void onDestroy() {
		// 在此关闭监听器
		if (locationManager != null) {
			if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				locationManager.removeUpdates(listener);
				locationManager.removeGpsStatusListener(gpsListener);
				return;
			}

		}
	}

}
