package com.inspur.emmcloud.util.common;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.system.SimpleEventMessage;
import com.inspur.emmcloud.config.Constant;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class NetUtils {
	public static final String NETWORK_TYPE_WIFI = "wifi";
	public static final String NETWORK_TYPE_3G = "eg";
	public static final String NETWORK_TYPE_2G = "2g";
	public static final String NETWORK_TYPE_WAP = "wap";
	public static final String NETWORK_TYPE_UNKNOWN = "unknown";
	public static final String NETWORK_TYPE_DISCONNECT = "disconnect";
	public static final String NETWORK_TYPE_VPN = "vpn";
	public static final String NETWORK_TYPE_4G = "4g";
	public static final String NETWORK_TYPE_MOBILE = "mobile";
	private static final String TAG = "PingNet";

	/**
	 * 没有连接网络
	 */
	public static final int NETWORK_NONE = -1;
	/**
	 * 移动网络
	 */
	public static final int NETWORK_MOBILE = 0;
	/**
	 * 无线网络
	 */
	public static final int NETWORK_WIFI = 1;
	/**
	 * 2G网络
	 */
	public static final int NETWORK_2G = 2;
	/**
	 * 3G网络
	 */
	public static final int NETWORK_3G = 3;
	/**
	 * 4G网络
	 */
	public static final int NETWORK_4G = 4;
	/**
	 * VPN连接
	 * */
	public static final int NETWORK_VPN= 5;
	/**
	 * 未知
	 */
	public static final int NETWORK_UNKNOW = -2;
	/**
	 * 定义电话管理器对象
	 */
	public static TelephonyManager mTelephonyManager;
	/**
	 * 定义连接管理器对象
	 */
	public static ConnectivityManager mConnectivityManager;
	/**
	 * 定义网络信息对象
	 */
	public static NetworkInfo mNetworkInfo;


	// 判断是否有网络连接
	public static boolean isNetworkConnected(Context context) {
			return isNetworkConnected(context,true);
		}


	// 判断是否有网络连接
	public static boolean isNetworkConnected(Context context,Boolean isShowToast) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
				return true;
			}else {
				if (isShowToast) {
					ToastUtils.show(context,
							context.getString(R.string.network_exception));
				}
				return false;
			}
	}

	/**
	 * Get network type
	 * 
	 * @param context
	 * @return
	 */
	public static int getNetworkType(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager == null ? null
				: connectivityManager.getActiveNetworkInfo();
		return networkInfo == null ? -1 : networkInfo.getType();
	}

	public static NetworkInfo.State getNetworkMobileState(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo.State mobile = connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).getState();
			return mobile;
	}

	public static NetworkInfo.State getNetworkWifiState(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo.State mobile = connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState();
		return mobile;
	}

	/**
	 * Get network type name
	 * 
	 * @param context
	 * @return
	 */
	public static String getNetworkTypeName(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo;
		String type = NETWORK_TYPE_DISCONNECT;
		if (manager == null
				|| (networkInfo = manager.getActiveNetworkInfo()) == null) {
			return type;
		}

        if (networkInfo.isConnected()) {
			String typeName = networkInfo.getTypeName();
			if ("WIFI".equalsIgnoreCase(typeName)) {
				type = NETWORK_TYPE_WIFI;
			} else if ("MOBILE".equalsIgnoreCase(typeName)) {
				String proxyHost = android.net.Proxy.getDefaultHost();
				type = TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(context) ? NETWORK_TYPE_3G
						: NETWORK_TYPE_2G)
						: NETWORK_TYPE_WAP;
			} else {
				type = NETWORK_TYPE_UNKNOWN;
			}
		}
		return type;
	}

	/**
	 * Whether is fast mobile network
	 * 
	 * @param context
	 * @return
	 */
	private static boolean isFastMobileNetwork(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager == null) {
			return false;
		}

		switch (telephonyManager.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return false;
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return false;
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return false;
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return true;
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return true;
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return false;
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return true;
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return true;
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return true;
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return true;
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			return true;
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			return true;
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return true;
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return false;
		case TelephonyManager.NETWORK_TYPE_LTE:
			return true;
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return false;
		default:
			return false;
		}
	}

	/**
	 * @param pingNetEntity 检测网络实体类
	 * @return 检测后的数据
	 */
	public static PingNetEntity ping(PingNetEntity pingNetEntity,Long WhileTime) {
		String line = null;
		Process process = null;
		BufferedReader successReader = null;
		String command = "ping -c " + pingNetEntity.getPingCount() + " -w " + pingNetEntity.getPingWtime() + " " + pingNetEntity.getIp();
		LogUtils.LbcDebug(command);
		long taegrtTime = System.currentTimeMillis()+WhileTime;
			try {
				process = Runtime.getRuntime().exec(command);
				if (process == null) {
					append(pingNetEntity.getResultBuffer(), "ping fail:process is null.");
					pingNetEntity.setPingTime(null);
					pingNetEntity.setResult(false);
					return pingNetEntity;
				}
				successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				while ((line = successReader.readLine()) != null) {
					append(pingNetEntity.getResultBuffer(), line);
					String time;
					if ((time = getTime(line)) != null) {
						pingNetEntity.setPingTime(time);
					}
				}
				int status = process.waitFor();
				if (status == 0) {
					append(pingNetEntity.getResultBuffer(), "exec cmd success:" + command);
					pingNetEntity.setResult(true);
				} else {
					append(pingNetEntity.getResultBuffer(), "exec cmd fail.");
					pingNetEntity.setPingTime(null);
					pingNetEntity.setResult(false);
				}
			} catch (IOException e) {
				Log.e(TAG, String.valueOf(e));
			} catch (InterruptedException e) {
				Log.e(TAG, String.valueOf(e));
			} finally {
				if (process != null) {
					process.destroy();
				}
				if (successReader != null) {
					try {
						successReader.close();
					} catch (IOException e) {
						Log.e(TAG, String.valueOf(e));
					}
				}
			}
			if(pingNetEntity.isResult()){
				return pingNetEntity;
			}

		return pingNetEntity;
	}

	private static void append(StringBuffer stringBuffer, String text) {
		if (stringBuffer != null) {
			stringBuffer.append(text + "\n");
		}
	}

	private static String getTime(String line) {
		String[] lines = line.split("\n");
		String time = null;
		for (String l : lines) {
			if (!l.contains("time="))
				continue;
			int index = l.indexOf("time=");
			time = l.substring(index + "time=".length());
			Log.i(TAG, time);
		}
		return time;
	}

	/**
	 * 得到网络类型
	 *
	 * @param context
	 * @return 网络类型
	 */
	public static List<Integer> getNetWrokState(Context context) {
		// 得到连接管理器对象
		List<Integer>  ResultData=new ArrayList<>();
		mConnectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null && mNetworkInfo.isConnected()) {
			if (mNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
				ResultData.add(NETWORK_WIFI);
			} else if (mNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
				ResultData.add(getMobileNetType(context));
			}
		}
		if(isVpnConnected()) {
			ResultData.add(NETWORK_VPN);
		}
		if(ResultData.size()<=0){
			ResultData.add(NETWORK_NONE);
		}
		return ResultData;
	}

	/**
	 * 获取移动网络的类型
	 *
	 * @param context
	 * @return 移动网络类型
	 */
	public static final int getMobileNetType(Context context) {

		mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = mTelephonyManager.getNetworkType();

		return getNetworkClass(networkType);
	}

	/**
	 * 判断移动网络的类型
	 *
	 * @param networkType
	 * @return 移动网络类型
	 */
	private static final int getNetworkClass(int networkType) {
		switch (networkType) {
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_IDEN:
				return NETWORK_2G;
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				return NETWORK_3G;
			case TelephonyManager.NETWORK_TYPE_LTE:
				return NETWORK_4G;
			default:
				return NETWORK_UNKNOW;
		}
	}

	/**
	 * 检测VPN
	 */
	public static boolean isVpnConnected() {
		try {
			Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
			if(niList != null) {
				for (NetworkInterface intf : Collections.list(niList)) {
					if(!intf.isUp() || intf.getInterfaceAddresses().size() == 0) {
						continue;
					}
					if ("tun0".equals(intf.getName()) || "ppp0".equals(intf.getName())){
						return true;
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * get connected str Name
	 * */
	public static List<String> getNetStateName(List<Integer> NetNames){
	 List<String> ConnectedNetNames = new ArrayList<>();
	     for (int i=0;i<NetNames.size();i++) {
	        switch (NetNames.get(i)) {
				case -2:
					ConnectedNetNames.add(NETWORK_TYPE_UNKNOWN);
					break;
				case -1:
					ConnectedNetNames.add(NETWORK_TYPE_DISCONNECT);
					break;
				case 0:
				    ConnectedNetNames.add(NETWORK_TYPE_MOBILE);
				case  1:
					ConnectedNetNames.add(NETWORK_TYPE_WIFI);
					break;
				case  2:
					ConnectedNetNames.add(NETWORK_TYPE_2G);
					break;
				case  3:
					ConnectedNetNames.add(NETWORK_TYPE_3G);
					break;
				case  4:
					ConnectedNetNames.add(NETWORK_TYPE_4G);
					break;
				case  5:
					ConnectedNetNames.add(NETWORK_TYPE_VPN);
					break;
				default:
					break;
			}
		 }
	 return ConnectedNetNames;
	}

	/**
	 *Ping 网络状态 "www.baidu.com"
	 * */
	public static void PingThreadStart(final  String  StrUrl,final int WaiteTime) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					PingNetEntity pingNetEntity=new PingNetEntity(StrUrl,1,WaiteTime,new StringBuffer());
					pingNetEntity=NetUtils.ping(pingNetEntity, (long) 4500);
					EventBus.getDefault().post(new SimpleEventMessage(Constant.EVENTBUS_TAG__NET_EXCEPTION_HINT, pingNetEntity.isResult()));
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}).start();
	}
}
