package com.inspur.emmcloud.bean.mine;

import com.facebook.react.bridge.WritableNativeMap;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.bean.login.ClusterBean;
import com.inspur.reactnative.ReactNativeWritableNativeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/24.
 */

public class Enterprise implements Serializable {
    private String code;
    private String id;
    private String name;
    private String creationDate;
    private String entLicenseCopy;
    private String entLicenseSn;
    private String lastUpdate;
    private ArrayList<ClusterBean> clusterBeanList = new ArrayList<>();

    public Enterprise() {
    }

    public Enterprise(JSONObject object) {
        code = JSONUtils.getString(object, "code", "");
        id = JSONUtils.getString(object, "id", "0");
        name = JSONUtils.getString(object, "name", "");
        creationDate = JSONUtils.getString(object, "creation_date", "0");
        entLicenseCopy = JSONUtils.getString(object, "ent_license_copy", "");
        entLicenseSn = JSONUtils.getString(object, "ent_license_sn", "");
        lastUpdate = JSONUtils.getString(object, "last_update", "0");
        JSONArray jsonArray = JSONUtils.getJSONArray(object, "clusters", new JSONArray());
        for (int i = 0; i < jsonArray.length(); i++) {
            clusterBeanList.add(new ClusterBean(JSONUtils.getJSONObject(jsonArray, i, new JSONObject())));
        }
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("code", code);
            obj.put("id", id);
            obj.put("name", name);
            obj.put("creation_date", creationDate);
            obj.put("ent_license_copy", entLicenseCopy);
            obj.put("ent_license_sn", entLicenseSn);
            obj.put("last_update", lastUpdate);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }

    /**
     * 为初始化RN bundle准备的，需要序列化
     *
     * @return
     */
    public ReactNativeWritableNativeMap enterPrise2ReactNativeWritableNativeMap() {
        ReactNativeWritableNativeMap map = new ReactNativeWritableNativeMap();
        map.putString("code", code);
        map.putInt("id", Integer.valueOf(id));
        map.putString("name", name);
        map.putDouble("creation_date", Double.valueOf(creationDate));
        map.putString("ent_license_copy", entLicenseCopy);
        map.putString("ent_license_sn", entLicenseSn);
        map.putDouble("last_update", Double.valueOf(lastUpdate));
        return map;
    }

    /**
     * 为RN内部自己调用准备的，不能序列化，否则报异常
     *
     * @return
     */
    public WritableNativeMap enterPrise2WritableNativeMap() {
        WritableNativeMap map = new WritableNativeMap();
        map.putString("code", code);
        map.putInt("id", Integer.valueOf(id));
        map.putString("name", name);
        map.putDouble("creation_date", Double.valueOf(creationDate));
        map.putString("ent_license_copy", entLicenseCopy);
        map.putString("ent_license_sn", entLicenseSn);
        map.putDouble("last_update", Double.valueOf(lastUpdate));
        return map;
    }

    public ArrayList<ClusterBean> getClusterBeanList() {
        return clusterBeanList;
    }

    public void setClusterBeanList(ArrayList<ClusterBean> clusterBeanList) {
        this.clusterBeanList = clusterBeanList;
    }
}
