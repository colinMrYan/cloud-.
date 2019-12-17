package com.inspur.emmcloud.basemodule.bean.badge;

import com.inspur.emmcloud.baselib.util.JSONUtils;

/**
 * Created by yufuchang on 2018/11/26.
 */

public class BadgeHeadersModel {

    /**
     * enterprise : string
     * tracer : string
     */

    private String enterprise;
    private String tracer;

    public BadgeHeadersModel(String headers) {
        enterprise = JSONUtils.getString(headers, "enterprise", "");
        tracer = JSONUtils.getString(headers, "tracer", "");
    }

    public String getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(String enterprise) {
        this.enterprise = enterprise;
    }

    public String getTracer() {
        return tracer;
    }

    public void setTracer(String tracer) {
        this.tracer = tracer;
    }
}
