package com.inspur.emmcloud.bean.login;

import com.inspur.emmcloud.util.common.JSONUtils;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/2/2.
 */

public class ClusterBean {
    private String clusterId = "";
    private String serviceName = "";
    private String baseUrl = "";
    public ClusterBean(JSONObject clusterObj){
        clusterId = JSONUtils.getString(clusterObj,"cluster_id","");
        serviceName = JSONUtils.getString(clusterObj,"service_name","");
        baseUrl = JSONUtils.getString(clusterObj,"base_url","");
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
