package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/4/2.
 */

public class SplashPayloadBean {
    /**
     * version : v1.0.0
     * state : ACTIVED
     * effectiveDate : 1495393588000
     * expireDate : 1495825594000
     * res1xHash : 1
     * res2xHash : 1
     * res3xHash : 1
     * mdpiHash : 1
     * hdpiHash : 1
     * xhdpiHash : 1
     * xxhdpiHash : 1
     * xxxhdpiHash : 1
     * resource : {"default":{"res1xHash":"1","mdpi":"IZHJ301KAYD.png","xxhdpi":"JQBJ301LZB0.png","hdpi":"UP7J301KYCA.png","xhdpi":"YQ4J301LFRX.png","mdpiHash":"1","res2x":"K3ZJ301JFB1.png","xxhdpiHash":"1","xxxhdpi":"U6PJ301MD1G.png","res1x":"CJBJ301IWU2.png","res3xHash":"1","hdpiHash":"1","res3x":"G0RJ301JU13.png","res2xHash":"1","xhdpiHash":"1","xxxhdpiHash":"1"}}
     */
    private String version = "";
    private String state = "";
    private long effectiveDate = 0;
    private long expireDate = 0;
    private String res1xHash = "";
    private String res2xHash = "";
    private String res3xHash = "";
    private String mdpiHash = "";
    private String hdpiHash = "";
    private String xhdpiHash = "";
    private String xxhdpiHash = "";
    private String xxxhdpiHash = "";
    private SplashResourceBean resource;

    public SplashPayloadBean(String payloadBean) {
        this.version = JSONUtils.getString(payloadBean, "version", "");
        this.state = JSONUtils.getString(payloadBean, "state", "");
        this.effectiveDate = JSONUtils.getLong(payloadBean, "effectiveDate", 0);
        this.expireDate = JSONUtils.getLong(payloadBean, "expireDate", 0);
        this.res1xHash = JSONUtils.getString(payloadBean, "res1xHash", "");
        this.res2xHash = JSONUtils.getString(payloadBean, "res2xHash", "");
        this.res3xHash = JSONUtils.getString(payloadBean, "res3xHash", "");
        this.mdpiHash = JSONUtils.getString(payloadBean, "mdpiHash", "");
        this.hdpiHash = JSONUtils.getString(payloadBean, "hdpiHash", "");
        this.xhdpiHash = JSONUtils.getString(payloadBean, "xhdpiHash", "");
        this.xxhdpiHash = JSONUtils.getString(payloadBean, "xxhdpiHash", "");
        this.xxxhdpiHash = JSONUtils.getString(payloadBean, "xxxhdpiHash", "");
        String resourceBean = JSONUtils.getString(payloadBean, "resource", "");
        this.resource = new SplashResourceBean(resourceBean);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(long effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public long getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(long expireDate) {
        this.expireDate = expireDate;
    }

    public String getRes1xHash() {
        return res1xHash;
    }

    public void setRes1xHash(String res1xHash) {
        this.res1xHash = res1xHash;
    }

    public String getRes2xHash() {
        return res2xHash;
    }

    public void setRes2xHash(String res2xHash) {
        this.res2xHash = res2xHash;
    }

    public String getRes3xHash() {
        return res3xHash;
    }

    public void setRes3xHash(String res3xHash) {
        this.res3xHash = res3xHash;
    }

    public String getMdpiHash() {
        return mdpiHash;
    }

    public void setMdpiHash(String mdpiHash) {
        this.mdpiHash = mdpiHash;
    }

    public String getHdpiHash() {
        return hdpiHash;
    }

    public void setHdpiHash(String hdpiHash) {
        this.hdpiHash = hdpiHash;
    }

    public String getXhdpiHash() {
        return xhdpiHash;
    }

    public void setXhdpiHash(String xhdpiHash) {
        this.xhdpiHash = xhdpiHash;
    }

    public String getXxhdpiHash() {
        return xxhdpiHash;
    }

    public void setXxhdpiHash(String xxhdpiHash) {
        this.xxhdpiHash = xxhdpiHash;
    }

    public String getXxxhdpiHash() {
        return xxxhdpiHash;
    }

    public void setXxxhdpiHash(String xxxhdpiHash) {
        this.xxxhdpiHash = xxxhdpiHash;
    }

    public SplashResourceBean getResource() {
        return resource;
    }

    public void setResource(SplashResourceBean resource) {
        this.resource = resource;
    }
}
