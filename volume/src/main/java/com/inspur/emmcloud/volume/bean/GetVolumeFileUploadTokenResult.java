package com.inspur.emmcloud.volume.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenmch on 2017/11/20.
 */

public class GetVolumeFileUploadTokenResult {
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

    public GetVolumeFileUploadTokenResult(String response) {
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

    public String getCallbackBodyEncode() {
        StringBuffer buffer = new StringBuffer(callbackBody);
        Pattern pattern = Pattern.compile("x:path=(.*?)\\|\\d+");
        Matcher matcher = pattern.matcher(callbackBody);
        if (matcher.find()) {
            String path = matcher.group(1);
            if (!StringUtils.isBlank(path)) {
                int start = matcher.start(1);
                int end = matcher.end(1);
                String pathAfterEncode = StringUtils.encodeURIComponent(path);
                buffer.replace(start, end, pathAfterEncode);
                return buffer.toString();
            }
        }

        return callbackBody;
    }

    public String getCallbackBody() {
        return callbackBody;
    }

    public void setCallbackBody(String callbackBody) {
        this.callbackBody = callbackBody;
    }

    public String getXPath() {
        String xPath = "";
        Pattern pattern = Pattern.compile("x:path=(.*?\\|\\d+)");
        Matcher matcher = pattern.matcher(callbackBody);
        if (matcher.find()) {
            xPath = matcher.group(1);
        }
        return xPath;
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
