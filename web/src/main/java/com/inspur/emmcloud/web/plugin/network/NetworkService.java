package com.inspur.emmcloud.web.plugin.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;

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

    private String getType(NetworkInfo info) {
        if (info != null) {
            String type = info.getTypeName();

            if (type.toLowerCase().equals(WIFI)) {
                return TYPE_WIFI;
            } else if (type.toLowerCase().equals(MOBILE)) {
                type = info.getSubtypeName();
                if (type.toLowerCase().equals(GSM) ||
                        type.toLowerCase().equals(GPRS) ||
                        type.toLowerCase().equals(EDGE)) {
                    return TYPE_2G;
                } else if (type.toLowerCase().startsWith(CDMA) ||
                        type.toLowerCase().equals(UMTS) ||
                        type.toLowerCase().equals(ONEXRTT) ||
                        type.toLowerCase().equals(EHRPD) ||
                        type.toLowerCase().equals(HSUPA) ||
                        type.toLowerCase().equals(HSDPA) ||
                        type.toLowerCase().equals(HSPA)) {
                    return TYPE_3G;
                } else if (type.toLowerCase().equals(LTE) ||
                        type.toLowerCase().equals(UMB) ||
                        type.toLowerCase().equals(HSPA_PLUS)) {
                    return TYPE_4G;
                }
            }
        } else {
            return TYPE_NONE;
        }
        return TYPE_UNKNOWN;
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
