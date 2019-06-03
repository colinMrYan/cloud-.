package com.inspur.imp.plugin.map;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.imp.plugin.ImpPlugin;

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

    // 当前地址描述
    public String addressInfo;
    // 经度
    public String longitude;
    // 纬度
    public String latitude;
    // 错误号码
    public String errCode;

    // 回掉方法
    public String successCb, failCb;
    String res = "";

    @Override
    public void execute(String action, JSONObject jsonObject) {
      if (action.equals("openByMapApp")) {
            String jindu = "";
            String weidu = "";
            try {
                if (!jsonObject.isNull("success"))
                    successCb = jsonObject.getString("success");
                if (!jsonObject.isNull("fail"))
                    failCb = jsonObject.getString("fail");
                if (!jsonObject.isNull("longitude"))
                    jindu = jsonObject.getString("longitude");
                if (!jsonObject.isNull("latitude"))
                    weidu = jsonObject.getString("latitude");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            openByMapApp(jindu, weidu);
        } else if (action.equals("doNaviByMapId")) {
            String address = "";
            String mapId = "";
            try {
                if (!jsonObject.isNull("success"))
                    successCb = jsonObject.getString("success");
                if (!jsonObject.isNull("fail"))
                    failCb = jsonObject.getString("fail");
                if (!jsonObject.isNull("mapId")) {
                    mapId = jsonObject.getString("mapId");
                }
                if (!jsonObject.isNull("address")) {
                    address = jsonObject.getString("address");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            doNaviByMapId(mapId, address);
        } else {
            showCallIMPMethodErrorDlg();
        }

    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        // TODO Auto-generated method stub
        Log.d("jason", "action=" + action);
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
            if (isInstallByread("com.autonavi.minimap")) {
                JSONObject object = new JSONObject();
                object.put("mapId", "map_autonavi");
                object.put("mapName", "高德地图");
                array.put(object);

            }
            if (isInstallByread("com.baidu.BaiduMap")) {
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

    public void doNaviByMapId(String mapId, String destination) {
        LogUtils.debug("jason", "mapId=" + mapId + "000");
        try {
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
    public void openByMapApp(String jindu, String weidu) {
        if (isInstallByread("com.baidu.BaiduMap")) {
            Uri uri = Uri.parse("geo:" + jindu + "," + weidu + "");
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
    public boolean isInstallByread(String packageName) {
        return new File("/data/data/" + packageName).exists();
    }

    @Override
    public void onDestroy() {
    }

}
