package com.inspur.emmcloud.bean.appcenter;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;


/**
 * Created by yufuchang on 2017/8/21.
 */


public class AppAdsBean {

    /**
     * id : bb24bc90-861d-11e7-9d0e-59f9f22b2862
     * adsName : 应用中心banner
     * adsLocId : top
     * uri : https://ecm.inspur.com
     * status : 1
     * adsCategory : ads_app
     * sortOrder : 1
     * legend : https://emm.inspur.com:443/img/headimg/bb174f10-861d-11e7-a296-91fc4ed6daaa
     */

    private String id = "";
    private String adsName = "";
    private String adsLocId = "";
    private String uri = "";
    private int status = 0;
    private String adsCategory = "";
    private int sortOrder = 0;
    private String legend = "";

    public AppAdsBean() {
    }

    public AppAdsBean(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "");
        adsName = JSONUtils.getString(obj, "adsName", "");
        adsLocId = JSONUtils.getString(obj, "adsLocId", "");
        uri = JSONUtils.getString(obj, "uri", "");
        status = JSONUtils.getInt(obj, "status", 0);
        adsCategory = JSONUtils.getString(obj, "adsCategory", "");
        sortOrder = JSONUtils.getInt(obj, "sortOrder", 0);
        legend = JSONUtils.getString(obj, "legend", "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdsName() {
        return adsName;
    }

    public void setAdsName(String adsName) {
        this.adsName = adsName;
    }

    public String getAdsLocId() {
        return adsLocId;
    }

    public void setAdsLocId(String adsLocId) {
        this.adsLocId = adsLocId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAdsCategory() {
        return adsCategory;
    }

    public void setAdsCategory(String adsCategory) {
        this.adsCategory = adsCategory;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }
}
