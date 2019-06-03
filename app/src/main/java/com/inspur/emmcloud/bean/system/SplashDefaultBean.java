package com.inspur.emmcloud.bean.system;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/4/2.
 */

public class SplashDefaultBean {
    /**
     * res1xHash : 1
     * mdpi : IZHJ301KAYD.png
     * xxhdpi : JQBJ301LZB0.png
     * hdpi : UP7J301KYCA.png
     * xhdpi : YQ4J301LFRX.png
     * mdpiHash : 1
     * res2x : K3ZJ301JFB1.png
     * xxhdpiHash : 1
     * xxxhdpi : U6PJ301MD1G.png
     * res1x : CJBJ301IWU2.png
     * res3xHash : 1
     * hdpiHash : 1
     * res3x : G0RJ301JU13.png
     * res2xHash : 1
     * xhdpiHash : 1
     * xxxhdpiHash : 1
     */

    private String res1xHash = "";
    private String mdpi = "";
    private String xxhdpi = "";
    private String hdpi = "";
    private String xhdpi = "";
    private String mdpiHash = "";
    private String res2x = "'";
    private String xxhdpiHash = "";
    private String xxxhdpi = "";
    private String res1x = "";
    private String res3xHash = "";
    private String hdpiHash = "";
    private String res3x = "";
    private String res2xHash = "";
    private String xhdpiHash = "";
    private String xxxhdpiHash = "";

    public SplashDefaultBean(String defaultBean) {
        this.res1xHash = JSONUtils.getString(defaultBean, "res1xHash", "");
        this.mdpi = JSONUtils.getString(defaultBean, "mdpi", "");
        this.xxhdpi = JSONUtils.getString(defaultBean, "xxhdpi", "");
        this.hdpi = JSONUtils.getString(defaultBean, "hdpi", "");
        this.xhdpi = JSONUtils.getString(defaultBean, "xhdpi", "");
        this.mdpiHash = JSONUtils.getString(defaultBean, "mdpiHash", "");
        this.res2x = JSONUtils.getString(defaultBean, "res2x", "");
        this.xxhdpiHash = JSONUtils.getString(defaultBean, "xxhdpiHash", "");
        this.xxxhdpi = JSONUtils.getString(defaultBean, "xxxhdpi", "");
        this.res1x = JSONUtils.getString(defaultBean, "res1x", "");
        this.res3xHash = JSONUtils.getString(defaultBean, "res3xHash", "");
        this.hdpiHash = JSONUtils.getString(defaultBean, "hdpiHash", "");
        this.res3x = JSONUtils.getString(defaultBean, "res3x", "");
        this.res2xHash = JSONUtils.getString(defaultBean, "res2xHash", "");
        this.xhdpiHash = JSONUtils.getString(defaultBean, "xhdpiHash", "");
        this.xxxhdpiHash = JSONUtils.getString(defaultBean, "xxxhdpiHash", "");
    }

    public String getRes1xHash() {
        return res1xHash;
    }

    public void setRes1xHash(String res1xHash) {
        this.res1xHash = res1xHash;
    }

    public String getMdpi() {
        return mdpi;
    }

    public void setMdpi(String mdpi) {
        this.mdpi = mdpi;
    }

    public String getXxhdpi() {
        return xxhdpi;
    }

    public void setXxhdpi(String xxhdpi) {
        this.xxhdpi = xxhdpi;
    }

    public String getHdpi() {
        return hdpi;
    }

    public void setHdpi(String hdpi) {
        this.hdpi = hdpi;
    }

    public String getXhdpi() {
        return xhdpi;
    }

    public void setXhdpi(String xhdpi) {
        this.xhdpi = xhdpi;
    }

    public String getMdpiHash() {
        return mdpiHash;
    }

    public void setMdpiHash(String mdpiHash) {
        this.mdpiHash = mdpiHash;
    }

    public String getRes2x() {
        return res2x;
    }

    public void setRes2x(String res2x) {
        this.res2x = res2x;
    }

    public String getXxhdpiHash() {
        return xxhdpiHash;
    }

    public void setXxhdpiHash(String xxhdpiHash) {
        this.xxhdpiHash = xxhdpiHash;
    }

    public String getXxxhdpi() {
        return xxxhdpi;
    }

    public void setXxxhdpi(String xxxhdpi) {
        this.xxxhdpi = xxxhdpi;
    }

    public String getRes1x() {
        return res1x;
    }

    public void setRes1x(String res1x) {
        this.res1x = res1x;
    }

    public String getRes3xHash() {
        return res3xHash;
    }

    public void setRes3xHash(String res3xHash) {
        this.res3xHash = res3xHash;
    }

    public String getHdpiHash() {
        return hdpiHash;
    }

    public void setHdpiHash(String hdpiHash) {
        this.hdpiHash = hdpiHash;
    }

    public String getRes3x() {
        return res3x;
    }

    public void setRes3x(String res3x) {
        this.res3x = res3x;
    }

    public String getRes2xHash() {
        return res2xHash;
    }

    public void setRes2xHash(String res2xHash) {
        this.res2xHash = res2xHash;
    }

    public String getXhdpiHash() {
        return xhdpiHash;
    }

    public void setXhdpiHash(String xhdpiHash) {
        this.xhdpiHash = xhdpiHash;
    }

    public String getXxxhdpiHash() {
        return xxxhdpiHash;
    }

    public void setXxxhdpiHash(String xxxhdpiHash) {
        this.xxxhdpiHash = xxxhdpiHash;
    }
}
