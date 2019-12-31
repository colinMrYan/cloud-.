package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: yufuchang
 * Date: 2019/12/27
 */
public class GetBaseStationUtils {
    public static final int SIM_YIDONG = 1;
    public static final int SIM_LIANTONG = 2;
    public static final int SIM_DIANXIN = 3;
    private static final String TAG = "yfcLog";
    TelephonyManager mTelephonyManager;
    private Context mContext;

    public GetBaseStationUtils(Context context) {
        this.mContext = context;
    }

    /**
     * 获取SIM卡类型
     * 获取SIM卡的IMSI码
     * SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile Subscriber Identification Number）是区别移动用户的标志，
     * 储存在SIM卡中，可用于区别移动用户的有效信息。IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成，
     * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成，
     * 用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。
     * 唯一地识别国内GSM移动通信网中移动客户。所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可
     */
    public int getSIMType() {
        TelephonyManager telManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (!PermissionRequestManagerUtils.getInstance().isHasPermission(mContext, Permissions.ACCESS_COARSE_LOCATION)) {
            return -1;
        }
//        return telManager.getPhoneType(); 原生提供了此方法  以后可以验证是否可用
        //因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
        String imsi = telManager.getSubscriberId();
        if (imsi != null) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
                //中国移动
                return SIM_YIDONG;
            } else if (imsi.startsWith("46001")) {
                //中国联通
                return SIM_LIANTONG;
            } else if (imsi.startsWith("46003")) {
                //中国电信
                return SIM_DIANXIN;
            }
        }
        return 0;
    }

    /**
     * 打印相邻基站信息
     */
    public void printNeighboringCellInfo() {
        List<NeighboringCellInfo> infos = mTelephonyManager.getNeighboringCellInfo();
        StringBuffer sb = new StringBuffer("总数 : " + infos.size() + "\n");
        for (NeighboringCellInfo info1 : infos) {
            sb.append(" LAC : " + info1.getLac());
            sb.append("\n CID : " + info1.getCid());
            sb.append("\n coord" + info1.getPsc());
            // 获取邻区基站信号强度
            sb.append("\n BSSS : " + (-113 + 2 * info1.getRssi()) + "\n");
        }
        LogUtils.YfcDebug("相邻基站信息：" + sb.toString());
    }

    /**
     * 获取 基站 信息
     *
     * @return
     */
    public Map getBaseStationInformation() {
        HashMap stationMap = new HashMap();
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        }
        if (!PermissionRequestManagerUtils.getInstance().isHasPermission(mContext, Permissions.ACCESS_COARSE_LOCATION)) {
            return stationMap;
        }
        int type = mTelephonyManager.getNetworkType();

        //需要判断网络类型，因为获取数据的方法不一样
//        || type == TelephonyManager.NETWORK_TYPE_LTE
        // 电信cdma网
        if (type == TelephonyManager.NETWORK_TYPE_CDMA
                || type == TelephonyManager.NETWORK_TYPE_1xRTT
                || type == TelephonyManager.NETWORK_TYPE_EVDO_0
                || type == TelephonyManager.NETWORK_TYPE_EVDO_A
                || type == TelephonyManager.NETWORK_TYPE_EVDO_B
                ) {
            CdmaCellLocation cdma = (CdmaCellLocation) mTelephonyManager.getCellLocation();
            if (cdma != null) {
                stationMap.put("baseStationLatitude", cdma.getBaseStationLatitude() / 14400);
                stationMap.put("baseStationLongitude", cdma.getBaseStationLongitude() / 14400);
                stationMap.put("cid", cdma.getBaseStationId());
                stationMap.put("lac", cdma.getNetworkId());
                stationMap.put("mnc", cdma.getSystemId());
            }
        } else if (type == TelephonyManager.NETWORK_TYPE_GPRS         // 移动和联通GSM网
                || type == TelephonyManager.NETWORK_TYPE_EDGE
                || type == TelephonyManager.NETWORK_TYPE_HSDPA
                || type == TelephonyManager.NETWORK_TYPE_UMTS
                || type == TelephonyManager.NETWORK_TYPE_LTE) {
            GsmCellLocation gsm = (GsmCellLocation) mTelephonyManager.getCellLocation();
            if (gsm != null) {
                stationMap.put("cid", gsm.getCid());
                stationMap.put("lac", gsm.getLac());
                stationMap.put("psc", gsm.getPsc());
            }
        } else if (type == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
            Toast.makeText(mContext, "电话卡不可用！", Toast.LENGTH_LONG).show();
        }
        return stationMap;
    }
}
