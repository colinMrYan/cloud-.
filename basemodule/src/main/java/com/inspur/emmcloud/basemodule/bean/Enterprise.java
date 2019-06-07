package com.inspur.emmcloud.basemodule.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

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

    public String getCreationDate() {
        return creationDate;
    }

    public String getEntLicenseCopy() {
        return entLicenseCopy;
    }

    public String getEntLicenseSn() {
        return entLicenseSn;
    }

    public String getLastUpdate() {
        return lastUpdate;
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


    public ArrayList<ClusterBean> getClusterBeanList() {
        return clusterBeanList;
    }

    public void setClusterBeanList(ArrayList<ClusterBean> clusterBeanList) {
        this.clusterBeanList = clusterBeanList;
    }
}
