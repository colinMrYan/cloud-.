package com.inspur.emmcloud.bean.schedule;

import java.io.Serializable;

/**
 * 各种Scheme解析的实体类
 * Created by yufuchang on 2019/5/11.
 */

public class Scheme implements Serializable {
    private String schemeNativeModuleType = "";
    private String schemeNativeModuleName = "";

    public String getSchemeNativeModuleType() {
        return schemeNativeModuleType;
    }

    public void setSchemeNativeModuleType(String schemeNativeModuleType) {
        this.schemeNativeModuleType = schemeNativeModuleType;
    }

    public String getSchemeNativeModuleName() {
        return schemeNativeModuleName;
    }

    public void setSchemeNativeModuleName(String schemeNativeModuleName) {
        this.schemeNativeModuleName = schemeNativeModuleName;
    }
}
