package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONObject;

import lombok.Data;

/**
 * Created by yufuchang on 2017/8/21.
 */

@Data
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

    public AppAdsBean(){}
    public AppAdsBean(JSONObject obj){
        id = JSONUtils.getString(obj,"id","");
        ads_name = JSONUtils.getString(obj,"ads_name","");
        ads_loc_id = JSONUtils.getString(obj,"ads_loc_id","");
        url = JSONUtils.getString(obj,"url","");
        status = JSONUtils.getInt(obj,"status",0);
        ads_category = JSONUtils.getString(obj,"ads_category","");
        sort_order = JSONUtils.getInt(obj,"sort_order",0);
        legend = JSONUtils.getString(obj,"legend","");
    }

}
