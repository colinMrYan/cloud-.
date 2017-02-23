package com.inspur.emmcloud.bean;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yufuchang on 2017/2/21.
 */

public class ReactNativeUpdateBean {

    @Override
    public String toString() {
        return "ReactNativeUpdateBean{" +
                "url='" + url + '\'' +
                ", digest='" + digest + '\'' +
                ", method='" + method + '\'' +
                ", version=" + version +
                ", creationDate=" + creationDate +
                '}';
    }

    /**
     * url : EG5S7H.zip
     * digest : bcbe3365e6ac95ea2c0343a2395834dd
     * method : MD5
     * version : 2
     * creationDate : 1487037600000
     */

    private String url;
    private String digest;
    private String method;
    private int version;
    private long creationDate;
    private String state;



    public ReactNativeUpdateBean(String reactNativeUpdateJson){
        try {
            JSONObject jsonObject = new JSONObject(reactNativeUpdateJson);
            if(jsonObject.has("url")){
                this.url = jsonObject.getString("url");
            }
            if(jsonObject.has("digest")){
                this.digest = jsonObject.getString("digest");
            }
            if(jsonObject.has("method")){
                this.digest = jsonObject.getString("method");
            }
            if(jsonObject.has("version")){
                this.digest = jsonObject.getString("version");
            }
            if(jsonObject.has("creationDate")){
                this.digest = jsonObject.getString("creationDate");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getState() {return state;}
}
