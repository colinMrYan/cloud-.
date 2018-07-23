package com.inspur.imp.plugin.device;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.Settings;

import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.util.DialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;

/**
 * 设备信息
 * 
 * @author 浪潮移动应用平台(IMP)产品组
 * 
 */
public class DeviceService extends ImpPlugin {

	public static final String TAG = "Device";
	// Device OS
	public static String platform;
	// Device UUID
	public static String uuid;

	private static final String ANDROID_PLATFORM = "Android";
	private static final String AMAZON_PLATFORM = "amazon-fireos";
	private static final String AMAZON_DEVICE = "Amazon";

	// beep组件的响的次数
	private int count;

	@Override
	public String executeAndReturn(String action, JSONObject paramsObject) {
		String res = "";
		JSONObject jsonObject = new JSONObject();
		if ("getInfo".equals(action)) {
			// 检查网络连接
			try {
				// 设备操作系统版本
				jsonObject
						.put("version", String.valueOf(this.getOSVersion()));

				// 获取操作系统
				jsonObject.put("platform", String.valueOf(this.getPlatform()));

				// 获取设备国际唯一标识码
				jsonObject.put("uuid", String.valueOf(this.getUuid()));
				jsonObject.put("model", getModel());
			} catch (Exception e) {
				e.printStackTrace();
			}
			res = jsonObject.toString();
		}else {
			showCallIMPMethodErrorDlg();
		}
		return res;
	}

	@Override
	public void execute(String action, JSONObject jsonObject) {
		// 使用notification中的beep组件
		if ("beep".equals(action)) {
			beep(jsonObject);
		}
		// 震动
		else if (action.equals("vibrate")) {
			vibrate(jsonObject);
		}else{
			showCallIMPMethodErrorDlg();
		}

	}

	/**
	 * 得到设备的platform
	 * 
	 * @return 平台信息
	 */
	public String getPlatform() {
		String platform;
		if (isAmazonDevice()) {
			platform = AMAZON_PLATFORM;
		} else {
			platform = ANDROID_PLATFORM;
		}
		return platform;
	}

	/**
	 * 得到设备的系统名称
	 * 
	 * @return
	 */
	public String getUuid() {
		String uuid = Settings.Secure.getString(getActivity()
				.getContentResolver(),
				Settings.Secure.ANDROID_ID);
		return uuid;
	}

	public String getModel() {
		String model = android.os.Build.MODEL;
		return model;
	}

	public String getProductName() {
		String productname = android.os.Build.PRODUCT;
		return productname;
	}

	/**
	 * 得到设备的系统版本
	 * 
	 * @return
	 */
	public String getOSVersion() {
		String osversion = android.os.Build.VERSION.RELEASE;
		return osversion;
	}

	public String getSDKVersion() {
		@SuppressWarnings("deprecation")
		String sdkversion = android.os.Build.VERSION.SDK;
		return sdkversion;
	}

	/**
	 * 检查设备是否由Amzaon生产
	 * 
	 * @return
	 */
	public boolean isAmazonDevice() {
		if (android.os.Build.MANUFACTURER.equals(AMAZON_DEVICE)) {
			return true;
		}
		return false;
	}

	private String getTimeZone() {
		return TimeZone.getDefault().getID();
	}

	/**
	 * Description notification中的beep实现与设定
	 * 
	 * @param paramsObject
	 *            JSON串
	 */
	private void beep(JSONObject paramsObject) {
		try {
			if (!paramsObject.isNull("count"))
				count = paramsObject.getInt("count");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 获得铃声的uri
		Uri ringtone = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		// 调用系统功能中的蜂鸣
		Ringtone notification = RingtoneManager.getRingtone(getActivity()
				.getBaseContext(), ringtone);
		// 如果不能调到系统蜂鸣则使用默认蜂鸣
		if (notification == null)
			notification = RingtoneManager.getRingtone(getFragmentContext(),
					RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
		if (notification != null)
			for (long i = 0L; i < count; i++) {
				notification.play();
				long timeout = 5000L;
				// 将蜂鸣提示线程分离，实现暂停（这是一种实现均衡间隔的设计）
				while ((notification.isPlaying()) && (timeout > 0L)) {
					timeout -= 100L;
					try {
						Thread.sleep(100L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
	}

	/**
	 * Description vibration中的vibrate实现与设定
	 * 
	 * @param
	 * 
	 */
	private void vibrate(JSONObject jsonObject) {
		long time = 0l;
		try {
			if (!jsonObject.isNull("time")) {

				time = jsonObject.getLong("time");

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (time == 0L)
			time = 500L;
		// 调用系统服务的vibrator组件
		Vibrator vibrator = (Vibrator) this.getActivity().getSystemService(
				"vibrator");
		vibrator.vibrate(time);
	}

	@Override
	public void onDestroy() {
	}

}
