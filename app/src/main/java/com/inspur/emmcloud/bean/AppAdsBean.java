package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONObject;


/**
 * Created by yufuchang on 2017/8/21.
 */


public class AppAdsBean {

    /**
     * id : bb24bc90-861d-11e7-9d0e-59f9f22b2862
     * ads_name : 应用中心banner
     * ads_loc_id : top
     * url : https://ecm.inspur.com
     * status : 1
     * ads_category : ads_app
     * sort_order : 1
     * legend : https://emm.inspur.com:443/img/headimg/bb174f10-861d-11e7-a296-91fc4ed6daaa
     */

    private String id = "";
    private String ads_name = "";
    private String ads_loc_id = "";
    private String url = "";
    private int status = 0;
    private String ads_category = "";
    private int sort_order = 0;
    private String legend = "";

    public AppAdsBean() {
    }

    public AppAdsBean(JSONObject obj) {
        id = JSONUtils.getString(obj, "id", "");
        ads_name = JSONUtils.getString(obj, "ads_name", "");
        ads_loc_id = JSONUtils.getString(obj, "ads_loc_id", "");
        url = JSONUtils.getString(obj, "url", "");
        status = JSONUtils.getInt(obj, "status", 0);
        ads_category = JSONUtils.getString(obj, "ads_category", "");
        sort_order = JSONUtils.getInt(obj, "sort_order", 0);
        legend = JSONUtils.getString(obj, "legend", "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAds_name() {
        return ads_name;
    }

    public void setAds_name(String ads_name) {
        this.ads_name = ads_name;
    }

    public String getAds_loc_id() {
        return ads_loc_id;
    }

    public void setAds_loc_id(String ads_loc_id) {
        this.ads_loc_id = ads_loc_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAds_category() {
        return ads_category;
    }

    public void setAds_category(String ads_category) {
        this.ads_category = ads_category;
    }

    public int getSort_order() {
        return sort_order;
    }

    public void setSort_order(int sort_order) {
        this.sort_order = sort_order;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }
}
