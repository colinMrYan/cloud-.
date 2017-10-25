package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.StringUtils;

import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/2/24.
 */

public class AndroidBundleBean {

    /**
     * domain : DISCOVER
     * version : v0.1.0
     * creationDate : 1487841280142
     * platform : ANDROID
     * type : ZIP
     */

    /**
     * {
     "source": "eac3eddd31f11e9327c443b9fcae731787a1cbf2",
     "namespace": "com.inspur.ecc.core.apps",
     "domain": "10000",
     "version": "v0.1.0",
     "mainComponent": "WhoseCar",
     "creationDate": 1490691586492,
     "platform": "ANDROID",
     "update": "https://ecm.inspur.com/inspur_esg/api/v0/app/10000/latest",
     "type": "ZIP"
     }
     */

    private String domain = "";
    private String version = "";
    private long creationDate;
    private String platform = "";
    private String type = "";
    private String source = "";
    private String namespace = "";
    private String mainComponent = "";
    private String update = "";


    public AndroidBundleBean(String androidBundleBean){
        try {
            JSONObject jsonAndroid = StringUtils.isBlank(androidBundleBean)? new JSONObject(): new JSONObject(androidBundleBean);
            if(jsonAndroid.has("domain")){
                this.domain = jsonAndroid.getString("domain");
            }
            if(jsonAndroid.has("version")){
                this.version = jsonAndroid.getString("version");
            }
            if(jsonAndroid.has("creationDate")){
                this.creationDate = jsonAndroid.getLong("creationDate");
            }
            if(jsonAndroid.has("platform")){
                this.platform = jsonAndroid.getString("platform");
            }
            if(jsonAndroid.has("type")){
                this.type = jsonAndroid.getString("type");
            }
            if(jsonAndroid.has("source")){
                this.source = jsonAndroid.getString("source");
            }
            if(jsonAndroid.has("namespace")){
                this.namespace = jsonAndroid.getString("namespace");
            }
            if(jsonAndroid.has("mainComponent")){
                this.mainComponent = jsonAndroid.getString("mainComponent");
            }
            if(jsonAndroid.has("update")){
                this.update = jsonAndroid.getString("update");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getMainComponent() {
        return mainComponent;
    }

    public void setMainComponent(String mainComponent) {
        this.mainComponent = mainComponent;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }
}
