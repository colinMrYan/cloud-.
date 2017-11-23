package com.inspur.emmcloud.bean.Volume;

import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.LogUtils;

import org.json.JSONObject;

/**
 * Created by chenmch on 2017/11/20.
 */

public class GetVolumeFileUploadSTSTokenResult {
    private String accessKeySecret;
    private String accessKeyId;
    private String expiration;
    private String securityToken;
    private String callbackUrl;
    private String callbackBody;
    private String storage;
    private String fileName;
    private String url;
    private String bucket;
    private String region;
    private String strategy;
    private String endpoint;

    public GetVolumeFileUploadSTSTokenResult(String response) {
        JSONObject obj = JSONUtils.getJSONObject(response);
        JSONObject credentialObj = JSONUtils.getJSONObject(obj, "credential", new JSONObject());
        accessKeySecret = JSONUtils.getString(credentialObj, "accessKeySecret", "");
        accessKeyId = JSONUtils.getString(credentialObj, "accessKeyId", "");
        expiration = JSONUtils.getString(credentialObj, "expiration", "");
        securityToken = JSONUtils.getString(credentialObj, "securityToken", "");
        JSONObject callbackObj = JSONUtils.getJSONObject(credentialObj, "callback", new JSONObject());
        callbackUrl = JSONUtils.getString(callbackObj, "callbackUrl", "");
        callbackBody = JSONUtils.getString(callbackObj, "callbackBody", "");
        storage = JSONUtils.getString(obj, "storage", "");
        fileName = JSONUtils.getString(obj, "filename", "");
        url = JSONUtils.getString(obj, "url", "");
        bucket = JSONUtils.getString(obj, "bucket", "");
        region = JSONUtils.getString(obj, "region", "");
        strategy = JSONUtils.getString(obj, "strategy", "");
        endpoint = url.replace(bucket + ".", "");
        LogUtils.jasonDebug("endpoint="+endpoint);
        LogUtils.jasonDebug("callbackUrl="+callbackUrl);
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getCallbackBody() {
        return callbackBody;
    }

    public void setCallbackBody(String callbackBody) {
        this.callbackBody = callbackBody;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
