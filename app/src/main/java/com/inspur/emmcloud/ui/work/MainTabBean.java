package com.inspur.emmcloud.ui.work;

/**
 * Created by yufuchang on 2017/4/28.
 */

public class MainTabBean {

    private int idx;
    private int resName;
    private int resIcon;
    private String  configureName = "";
    private String  configureIcon = "";
    private Class<?> clz;
    private String commpant = "";

    public MainTabBean(int idx, int resName, int resIcon, Class<?> clz) {
        this.idx = idx;
        this.resName = resName;
        this.resIcon = resIcon;
        this.clz = clz;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getResName() {
        return resName;
    }

    public void setResName(int resName) {
        this.resName = resName;
    }

    public int getResIcon() {
        return resIcon;
    }

    public void setResIcon(int resIcon) {
        this.resIcon = resIcon;
    }

    public String getConfigureName() {
        return configureName;
    }

    public void setConfigureName(String configureName) {
        this.configureName = configureName;
    }

    public String getConfigureIcon() {
        return configureIcon;
    }

    public void setConfigureIcon(String configureIcon) {
        this.configureIcon = configureIcon;
    }

    public Class<?> getClz() {
        return clz;
    }

    public void setClz(Class<?> clz) {
        this.clz = clz;
    }

    public String getCommpant() {
        return commpant;
    }

    public void setCommpant(String commpant) {
        this.commpant = commpant;
    }
}
