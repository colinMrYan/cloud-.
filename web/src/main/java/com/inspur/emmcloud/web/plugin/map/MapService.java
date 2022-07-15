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
    private final String MAP_TENCENT_APPID = "com.tencent.map";
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
            case "navigationByAutoNavi":
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

    private void navigationByAutonavi(JSONObject jsonObject) {
        try {
            if (!jsonObject.isNull("success"))
                successCb = jsonObject.getString("success");
            if (!jsonObject.isNull("fail"))
                failCb = jsonObject.getString("fail");
            final JSONObject optionsObj = jsonObject.getJSONObject("options");
            int mapType = 0;
            if (optionsObj.has("openType")) {
                mapType = optionsObj.getInt("openType");
            }
            switch (mapType) {
                case 1:
                    navigationByBaiduAutonavi(jsonObject);
                    break;
                case 2:
                    navigationByTencentAutonavi(jsonObject);
                    break;
                case 0:
                default:
                    navigationByGaodeAutonavi(jsonObject);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void navigationByGaodeAutonavi(JSONObject jsonObject) {
        if (!isAppInstall(MAP_AUTONAVI_APPID)) {
            callbackFail("未安装此地图");
            return;
        }
        try {
            JSONObject optionsObj = JSONUtils.getJSONObject(jsonObject, "options", new JSONObject());
            Double fromLongitude = JSONUtils.getDouble(optionsObj, "srclng", null);
            Double fromLatitude = JSONUtils.getDouble(optionsObj, "srclat", null);
            Double toLongitude = JSONUtils.getDouble(optionsObj, "dstlng", 0);
            Double toLatitude = JSONUtils.getDouble(optionsObj, "dstlat", 0);
            String coordType = JSONUtils.getString(optionsObj, "coordType", "GCJ02");
            String fromName = JSONUtils.getString(optionsObj, "sName", "");
            String toName = JSONUtils.getString(optionsObj, "dName", "");
            int transportation = JSONUtils.getInt(optionsObj, "mode", 1);
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
                builder.append("&slat=").append(fromLatitude).append("&slon=").append(fromLongitude);
            }
            if (!StringUtils.isBlank(fromName)) {
                builder.append("&sname=").append(fromName);
            }
            builder.append("&dlat=").append(toLatitude).append("&dlon=").append(toLongitude);
            if (!StringUtils.isBlank(toName)) {
                builder.append("&dname=").append(toName);
            }
            builder.append("&dev=0&t=").append(transportation);
            Intent intent = getFragmentContext().getPackageManager()
                    .getLaunchIntentForPackage("com.autonavi.minimap");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(builder.toString()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            callbackFail(e.getMessage());
        }

    }

    public void navigationByBaiduAutonavi(JSONObject jsonObject) {
        if (!isAppInstall(MAP_BAIDU_APPID)) {
            callbackFail("未安装此地图");
            return;
        }
        try {
            JSONObject optionsObj = JSONUtils.getJSONObject(jsonObject, "options", new JSONObject());
            Double fromLongitude = JSONUtils.getDouble(optionsObj, "srclng", null);
            Double fromLatitude = JSONUtils.getDouble(optionsObj, "srclat", null);
            Double toLongitude = JSONUtils.getDouble(optionsObj, "dstlng", 0);
            Double toLatitude = JSONUtils.getDouble(optionsObj, "dstlat", 0);
            String coordType = JSONUtils.getString(optionsObj, "coordType", "GCJ02");
            String fromName = JSONUtils.getString(optionsObj, "sName", "");
            String toName = JSONUtils.getString(optionsObj, "dName", "");
            int transportation = JSONUtils.getInt(optionsObj, "mode", 1);
            if (coordType.equals("BD09")) {
                if (fromLatitude != null && fromLatitude != null) {
                    double[] fromLocation = ECMLoactionTransformUtils.bd09togcj02(fromLongitude, fromLatitude);
                    fromLongitude = fromLocation[0];
                    fromLatitude = fromLocation[1];
                }
                double[] toLocation = ECMLoactionTransformUtils.bd09togcj02(toLongitude, toLatitude);
                toLongitude = toLocation[0];
                toLatitude = toLocation[1];
            } else if (coordType.equals("WGS84")) {
                if (fromLatitude != null && fromLatitude != null) {
                    double[] fromLocation = ECMLoactionTransformUtils.wgs84togcj02(fromLongitude, fromLatitude);
                    fromLongitude = fromLocation[0];
                    fromLatitude = fromLocation[1];
                }
                double[] toLocation = ECMLoactionTransformUtils.wgs84togcj02(toLongitude, toLatitude);
                toLongitude = toLocation[0];
                toLatitude = toLocation[1];
            }
            StringBuilder builder = new StringBuilder("baidumap://map/direction");
            builder.append("?region=").append(" ");
            builder.append("&coord_type=gcj02");
            if (fromLatitude != null && fromLatitude != null) {
                builder.append("&origin=").append(fromLatitude).append(",").append(fromLongitude);
            }
            builder.append("&destination=").append(toLatitude).append(",").append(toLongitude);
            builder.append("&mode=").append(transportation == 0 ? "driving" : transportation == 2? "walking" : "transit");
            builder.append("&src=").append(getFragmentContext().getPackageName());
            Intent intent = getFragmentContext().getPackageManager()
                    .getLaunchIntentForPackage(MAP_BAIDU_APPID);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(builder.toString()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            callbackFail(e.getMessage());
        }

    }

    public void navigationByTencentAutonavi(JSONObject jsonObject) {
        if (!isAppInstall(MAP_TENCENT_APPID)) {
            callbackFail("未安装此地图");
            return;
        }
        try {
            JSONObject optionsObj = JSONUtils.getJSONObject(jsonObject, "options", new JSONObject());
            Double fromLongitude = JSONUtils.getDouble(optionsObj, "srclng", null);
            Double fromLatitude = JSONUtils.getDouble(optionsObj, "srclat", null);
            Double toLongitude = JSONUtils.getDouble(optionsObj, "dstlng", 0);
            Double toLatitude = JSONUtils.getDouble(optionsObj, "dstlat", 0);
            String coordType = JSONUtils.getString(optionsObj, "coordType", "GCJ02");
            String fromName = JSONUtils.getString(optionsObj, "sName", "");
            String toName = JSONUtils.getString(optionsObj, "dName", "");
            int transportation = JSONUtils.getInt(optionsObj, "mode", 1);
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
            StringBuilder builder = new StringBuilder("qqmap://map/routeplan");
            builder.append("?type=").append(transportation == 0 ? "drive" : transportation == 2? "walk" : "bus");
            if (!StringUtils.isBlank(fromName)) {
                builder.append("&from=").append(fromName);
            }
            if (fromLatitude != null && fromLatitude != null) {
                builder.append("&fromcoord=").append(fromLatitude).append(",").append(fromLongitude);
            }
            if (!StringUtils.isBlank(toName)) {
                builder.append("&to=").append(toName);
            }
            builder.append("&tocoord=").append(toLatitude).append(",").append(toLongitude);
            //注册开发者账号，获取key
            builder.append("&referer=").append("EQRBZ-VABWX-LPI4J-TH5ZF-HMDGQ-AYFN2");
            String str = builder.toString();
            Intent intent = getFragmentContext().getPackageManager()
                    .getLaunchIntentForPackage(MAP_TENCENT_APPID);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(builder.toString()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
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
            JSONObject object = new JSONObject();
            try {
                object.put("errorMessage", errorMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.jsCallback(failCb, object);
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
