package com.inspur.emmcloud.bean.system;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2018/4/2.
 */

public class AppTabIdBean {
    /**
     * namespace : com.inspur.ecc.core.preferences
     * domain : main-tab
     * version : v1.0.0
     */

    private String namespace = "";
    private String domain = "";
    private String version = "";

    public AppTabIdBean(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("namespace")) {
                this.namespace = jsonObject.getString("namespace");
            }
            if (jsonObject.has("domain")) {
                this.domain = jsonObject.getString("domain");
            }
            if (jsonObject.has("version")) {
                this.version = jsonObject.getString("version");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
