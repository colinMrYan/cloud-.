package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/4/2.
 */

public class SplashIdBean {
    /**
     * namespace : com.inspur.ecc.core.preferences
     * domain : launch-screen
     * version : v1.0.0
     */
    private String namespace = "";
    private String domain = "";
    private String version = "";

    public SplashIdBean(String response) {
        this.namespace = JSONUtils.getString(response, "namespace", "");
        this.domain = JSONUtils.getString(response, "domain", "");
        this.version = JSONUtils.getString(response, "version", "");

    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
