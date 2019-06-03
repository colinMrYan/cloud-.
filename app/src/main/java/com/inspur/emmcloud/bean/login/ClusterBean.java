package com.inspur.emmcloud.bean.login;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/2/2.
 */

public class ClusterBean {
    private String clusterId = "";
    private String serviceName = "";//作为ID使用，服务端不确定，客户端无法解析
    private String baseUrl = "";
    private String serviceVersion = "";

    public ClusterBean() {
    }

    public ClusterBean(JSONObject clusterObj) {
        clusterId = JSONUtils.getString(clusterObj, "cluster_id", "");
        serviceName = JSONUtils.getString(clusterObj, "service_name", "");
        baseUrl = JSONUtils.getString(clusterObj, "base_url", "");
        serviceVersion = JSONUtils.getString(clusterObj, "service_version", "");
    }

    public ClusterBean(String serviceName) {
        this.serviceName = serviceName;
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

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof ClusterBean)) {
            return false;
        }
        ClusterBean clusterBean = (ClusterBean) other;
        //此处从==判断是否相等  改为equals
        return getServiceName().equals(clusterBean.getServiceName());
    }
}
