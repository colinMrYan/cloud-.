package com.inspur.emmcloud.web.plugin.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.telephony.TelephonyManager;

import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * 网络连接&流量监测
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class NetworkService extends ImpPlugin {

    public static final String WIFI = "wifi";
    public static final String WIMAX = "wimax";
    // mobile
    public static final String MOBILE = "mobile";
    // 2G network types
    public static final String GSM = "gsm";
    public static final String GPRS = "gprs";
    public static final String EDGE = "edge";
    // 3G network types
    public static final String CDMA = "cdma";
    public static final String UMTS = "umts";
    public static final String HSPA = "hspa";
    public static final String HSUPA = "hsupa";
    public static final String HSDPA = "hsdpa";
    public static final String ONEXRTT = "1xrtt";
    public static final String EHRPD = "ehrpd";
    // 4G network types
    public static final String LTE = "lte";
    public static final String UMB = "umb";
    public static final String HSPA_PLUS = "hspa+";
    // return type
    public static final String TYPE_UNKNOWN = "unknown";
    public static final String TYPE_ETHERNET = "ethernet";
    public static final String TYPE_WIFI = "wifi";
    public static final String TYPE_2G = "2g";
    public static final String TYPE_3G = "3g";
    public static final String TYPE_4G = "4g";
    public static final String TYPE_NONE = "无网络";
    public static int NOT_REACHABLE = 0;
    public static int REACHABLE_VIA_CARRIER_DATA_NETWORK = 1;
    public static int REACHABLE_VIA_WIFI_NETWORK = 2;
    ConnectivityManager sockMan;
    //获取网络流量服务的参数
    private long data;
    private String packageName;

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        String res = "";
        // 打开系统发送短信的界面，根据传入参数自动填写好相关信息
        if ("getConnInfo".equals(action)) {
            //检查网络连接
            res = getConnInfo(paramsObject);
            return res;
        }
        //流量监测服务
        else if ("getTotalRxBytes".equals(action)) {
            data = getTotalRxBytes();
        } else if ("getMobileRxBytes".equals(action)) {
            data = getMobileRxBytes();
        }
//		else if ("getAppRecive".equals(action)){
//			try {
//				data = getAppRecive(paramsObject);
//			} catch (NameNotFoundException e) {
//				e.printStackTrace();
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//		else if("getAppSend".equals(action)){
//			try {
//				data = (paramsObject);
//			} catch (NameNotFoundException e) {
//				e.printStackTrace();
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
        else {
            showCallIMPMethodErrorDlg();
        }
        return Long.toString(data) + "MB";
    }

    /**
     * 检查网络的连接情况
     *
     * @param paramsObject
     */
    private String getConnInfo(JSONObject paramsObject) {
        this.sockMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = sockMan.getActiveNetworkInfo();
        String connectionType = this.getConnectionInfo(info);
        return connectionType;
    }

    private String getConnectionInfo(NetworkInfo info) {
        String type = TYPE_NONE;
        if (info != null) {
            if (!info.isConnected()) {
                type = TYPE_NONE;
            } else {
                type = getType(info);
            }
        }
        return type;
    }


    /**
     * 获取网络2G/3G/4G/wifi
     */
    public String getType(NetworkInfo networkInfo) {
        String netWorkType = null;
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                netWorkType = TYPE_WIFI;
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();
                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        netWorkType = TYPE_2G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        netWorkType = TYPE_3G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        netWorkType = TYPE_4G;
                        break;
                    default:
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            netWorkType = "3G";
                        } else {
                            netWorkType = "移动网络";
                        }
                        break;
                }
            }
        }
        return netWorkType;
    }

    /**
     * 获得系统传入的总流量
     *
     * @return 单位是MB
     */
    public long getTotalRxBytes() {
        return TrafficStats.getTotalRxBytes() == TrafficStats.UNSUPPORTED ? 0
                : (TrafficStats.getTotalRxBytes() / 1024 / 1024);
    }

    /**
     * 获取通过Mobile连接收到的字节总数，不包含WiFi
     *
     * @return 单位是MB
     */
    public long getMobileRxBytes() {
        return TrafficStats.getMobileRxBytes() == TrafficStats.UNSUPPORTED ? 0
                : (TrafficStats.getMobileRxBytes() / 1024 / 1024);
    }

//	/**
//	 * 通过包名查看对应应用的流量上行数据
//	 * @param paramsObject
//	 * @return 单位是MB
//	 * @throws NameNotFoundException
//	 * @throws JSONException
//	 */
//	public long getAppSend(JSONObject paramsObject) throws NameNotFoundException, JSONException{
//		if(!paramsObject.isNull("packageName")){
//			packageName = paramsObject.getString("packageName");
//		}
//		int uid = 0;
//		PackageManager pm = getFragmentContext().getPackageManager();
//		ApplicationInfo ai = pm.getApplicationInfo(packageName,PackageManager.GET_ACTIVITIES);
//		uid = ai.uid;
//		return TrafficStats.getUidTxBytes(uid) / 1024 / 1024;
//	}

//	/**
//	 * 通过包名查看对应应用的流量下行数据
//	 * @param paramsObject
//	 * @return 单位是MB
//	 * @throws NameNotFoundException
//	 * @throws JSONException
//	 */
//
//	public long getAppRecive(JSONObject paramsObject) throws NameNotFoundException, JSONException{
//		if(!paramsObject.isNull("packageName")){
//			packageName = paramsObject.getString("packageName");
//		}
//		int uid = 0;
//		PackageManager pm = getFragmentContext().getPackageManager();
//		ApplicationInfo ai = pm.getApplicationInfo(packageName,PackageManager.GET_ACTIVITIES);
//		uid = ai.uid;
//		return TrafficStats.getUidRxBytes(uid) / 1024 / 1024;
//	}


    @Override
    public void onDestroy() {

    }

    @Override
    public void execute(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
    }
}
