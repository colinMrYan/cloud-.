package com.inspur.emmcloud.web.plugin.map;

import android.content.Intent;
import android.net.Uri;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.plugin.amaplocation.ECMLoactionTransformUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * 设备信息
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class MapService extends ImpPlugin {
    private final String MAP_BAIDU_APPID = "com.baidu.BaiduMap";
    private final String MAP_AUTONAVI_APPID = "com.autonavi.minimap";
    // 当前地址描述
    public String addressInfo;
    // 经度
    public String longitude;
    // 纬度
    public String latitude;
    // 错误号码
    public String errCode;

    // 回调方法
    public String successCb, failCb;
    String res = "";
    @Override
    public void execute(String action, JSONObject jsonObject) {
        switch (action) {
            case "openByMapApp":
                openByMapApp(jsonObject);
                break;
            case "doNaviByMapId":
                navigationToDestination(jsonObject);
                break;
            case "navigationByAutonavi":
                navigationByAutonavi(jsonObject);
                break;
            default:
                showCallIMPMethodErrorDlg();
                break;

        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        // TODO Auto-generated method stub
        if (action.equals("getAllMapApps")) {
            String result = getAllMapApps();
            return result;
        } else {
            showCallIMPMethodErrorDlg();
        }
        return "";
    }

    /**
     * 获取客户端已安装的所有地图类应用
     *
     * @return
     */
    private String getAllMapApps() {
        // TODO Auto-generated method stub
        JSONArray array = new JSONArray();
        try {
            if (isAppInstall(MAP_AUTONAVI_APPID)) {
                JSONObject object = new JSONObject();
                object.put("mapId", "map_autonavi");
                object.put("mapName", "高德地图");
                array.put(object);

            }
            if (isAppInstall(MAP_BAIDU_APPID)) {
                JSONObject object = new JSONObject();
                object.put("mapId", "map_baidu");
                object.put("mapName", "百度地图");
                array.put(object);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return array.toString();
    }

    public void navigationByAutonavi(JSONObject jsonObject) {
        successCb = JSONUtils.getString(jsonObject, "success", "");
        failCb = JSONUtils.getString(jsonObject, "fail", "");
        if (!isAppInstall(MAP_AUTONAVI_APPID)) {
            callbackFail("未安装此地图");
            return;
        }
        try {
            Double fromLongitude = JSONUtils.getDouble(jsonObject, "srclng", null);
            Double fromLatitude = JSONUtils.getDouble(jsonObject, "srclat", null);
            Double toLongitude = JSONUtils.getDouble(jsonObject, "dstlng", 0);
            Double toLatitude = JSONUtils.getDouble(jsonObject, "dstlat", 0);
            String coordType = JSONUtils.getString(jsonObject, "coordtype", "WGS84");
            if (coordType.equals("WGS84")) {
                if (fromLatitude != null && fromLatitude != null) {
                    double[] fromLocation = ECMLoactionTransformUtils.wgs84togcj02(fromLongitude, fromLatitude);
                    fromLongitude = fromLocation[0];
                    fromLatitude = fromLocation[1];
                }
                double[] toLocation = ECMLoactionTransformUtils.wgs84togcj02(toLongitude, toLatitude);
                toLongitude = toLocation[0];
                toLatitude = toLocation[1];
            } else if (coordType.equals("BD09")) {
                if (fromLatitude != null && fromLatitude != null) {
                    double[] fromLocation = ECMLoactionTransformUtils.bd09togcj02(fromLongitude, fromLatitude);
                    fromLongitude = fromLocation[0];
                    fromLatitude = fromLocation[1];
                }
                double[] toLocation = ECMLoactionTransformUtils.bd09togcj02(toLongitude, toLatitude);
                toLongitude = toLocation[0];
                toLatitude = toLocation[1];
            }
            StringBuilder builder = new StringBuilder("amapuri://route/plan?sourceApplication=");
            builder.append(getFragmentContext().getPackageName());
            if (fromLatitude != null && fromLatitude != null) {
                builder.append("&slat=").append(fromLatitude).append("&slon").append(fromLongitude);
            }
            builder.append("&dlat=").append(toLatitude).append("&dlon").append(toLongitude);
            Intent intent = getFragmentContext().getPackageManager()
                    .getLaunchIntentForPackage("com.autonavi.minimap");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(builder.toString()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
            callbackSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            callbackFail(e.getMessage());
        }

    }

    private void callbackSuccess() {
        if (!StringUtils.isBlank(successCb)) {
            this.jsCallback(successCb, "");
        }
    }

    private void callbackFail(String errorMessage) {
        if (!StringUtils.isBlank(failCb)) {
            this.jsCallback(failCb, errorMessage);
        }

    }

    public void navigationToDestination(JSONObject jsonObject) {

        try {
            String destination = "";
            String mapId = "";
            if (!jsonObject.isNull("success"))
                successCb = jsonObject.getString("success");
            if (!jsonObject.isNull("fail"))
                failCb = jsonObject.getString("fail");
            if (!jsonObject.isNull("mapId")) {
                mapId = jsonObject.getString("mapId");
            } else {
                mapId = "map_baidu";
            }
            if (!jsonObject.isNull("address")) {
                destination = jsonObject.getString("address");
            }
            if (mapId.equals("map_baidu")) {
                String packageName = getFragmentContext().getPackageName();
                Intent intent = Intent
                        .parseUri(
                                "intent://map/direction?destination="
                                        + destination
                                        + "&src="
                                        + packageName
                                        + "#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end",
                                0);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                this.jsCallback(successCb, "地图打开成功");
            } else if (mapId.equals("map_autonavi")) {
                String packageName = getFragmentContext().getPackageName();
                Intent intent = getFragmentContext().getPackageManager()
                        .getLaunchIntentForPackage("com.autonavi.minimap");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setAction(Intent.ACTION_VIEW);
                String uri = "androidamap://keywordNavi?sourceApplication=" + packageName + "&keyword=" + destination + "&style=2";
                intent.setData(Uri.parse(uri));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                this.jsCallback(successCb, "地图打开成功");
            } else {
                this.jsCallback(failCb, "未安装此地图");
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            this.jsCallback(failCb, "未安装此地图");
        }
    }

    /**
     * 获取地址信息
     */
    public void getInfo() {
        // 检查网络连接
        JSONObject jsonObject = new JSONObject();
        try {
            // 地理位置
            jsonObject.put("addressInfo", addressInfo);
            // 具体地址的地图
            jsonObject.put("longitude", longitude);
            jsonObject.put("latitude", latitude);
            jsonObject.put("errCode", errCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        res = jsonObject.toString();
        this.jsCallback(successCb, res);
    }

    /**
     * 用地图app打开目前所在地
     */
    public void openByMapApp(JSONObject jsonObject) {
        String longitude = "";
        String latitude = "";
        try {
            if (!jsonObject.isNull("success"))
                successCb = jsonObject.getString("success");
            if (!jsonObject.isNull("fail"))
                failCb = jsonObject.getString("fail");
            if (!jsonObject.isNull("longitude"))
                longitude = jsonObject.getString("longitude");
            if (!jsonObject.isNull("latitude"))
                latitude = jsonObject.getString("latitude");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (isAppInstall("com.baidu.BaiduMap")) {
            Uri uri = Uri.parse("geo:" + longitude + "," + latitude + "");
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            getActivity().startActivity(it);
        } else {
            this.jsCallback(failCb, "没有安装地图客户端");
        }
    }

    /**
     * 查看周边酒店信息
     */
    public void aroundHotel(String jingdu, String weidu) {

    }

    /**
     * 判断是否安装目标应用
     *
     * @param packageName 目标应用安装后的包名
     * @return 是否已安装目标应用
     */
    public boolean isAppInstall(String packageName) {
        return new File("/data/data/" + packageName).exists();
    }

    @Override
    public void onDestroy() {
    }

}
