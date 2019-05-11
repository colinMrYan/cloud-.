package com.inspur.emmcloud.bean.schedule;

import java.io.Serializable;

/**
 * 各种Scheme解析的实体类
 * Created by yufuchang on 2019/5/11.
 */

public class Scheme implements Serializable {
    private String schemeContent = "";
    private String schemeId = "";

    public String getSchemeContent() {
        return schemeContent;
    }

    public void setSchemeContent(String schemeContent) {
        this.schemeContent = schemeContent;
    }

    public String getSchemeId() {
        return schemeId;
    }

    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }
}
