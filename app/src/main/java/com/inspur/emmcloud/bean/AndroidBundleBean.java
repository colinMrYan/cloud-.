package com.inspur.emmcloud.bean;

import org.json.JSONException;
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

    private String domain;
    private String version;
    private long creationDate;
    private String platform;
    private String type;

    public AndroidBundleBean(String androidBundleBean){
        try {
            JSONObject jsonAndroid = new JSONObject(androidBundleBean);
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
        } catch (JSONException e) {
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
}
